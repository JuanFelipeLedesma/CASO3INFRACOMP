import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServidorPaquetesSeguroAutenticado {
    private static final int PUERTO = 12345;
    private static final String LLAVE_PRIVADA = "llave_privada.pem";
    private static final String ARCHIVO_CSV = "resultados.csv";
    private Map<String, Paquete> tablaPaquetes;
    private SecretKey llaveAES;
    private IvParameterSpec iv;
    private PrivateKey llavePrivada;
    private ExecutorService pool;

    public ServidorPaquetesSeguroAutenticado(int numeroDelegados) {
        tablaPaquetes = new HashMap<>();
        inicializarPaquetes();
        pool = Executors.newFixedThreadPool(numeroDelegados);
        try {
            llaveAES = CifradoAES.generarLlaveAES();
            iv = CifradoAES.generarIV();
            llavePrivada = cargarLlavePrivada();
            inicializarCSV();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inicializarPaquetes() {
        for (int i = 1; i <= 32; i++) {
            String clienteId = "cliente" + i;
            String paqueteId = "paquete" + i;
            int estado = Paquete.ENOFICINA;
            tablaPaquetes.put(paqueteId, new Paquete(clienteId, paqueteId, estado));
        }
    }

    private PrivateKey cargarLlavePrivada() throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(LLAVE_PRIVADA))) {
            return (PrivateKey) ois.readObject();
        }
    }

    private void inicializarCSV() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_CSV, false))) {
            writer.println("Escenario,ClienteID,PaqueteID,Tiempo (ms)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor seguro autenticado iniciado en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                pool.execute(new DelegadoCliente(cliente));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class DelegadoCliente implements Runnable {
        private Socket cliente;

        public DelegadoCliente(Socket cliente) {
            this.cliente = cliente;
        }

        @Override
        public void run() {
            long inicioConsulta = System.nanoTime(); // Medir tiempo de inicio
            try (
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream());
                DataInputStream entrada = new DataInputStream(cliente.getInputStream())
            ) {
                // Enviar la llave AES y el IV al cliente
                salida.writeInt(llaveAES.getEncoded().length);
                salida.write(llaveAES.getEncoded());
                salida.writeInt(iv.getIV().length);
                salida.write(iv.getIV());

                // Recibir datos del cliente
                String clienteId = entrada.readUTF();
                String paqueteId = entrada.readUTF();

                // Consultar el estado del paquete
                String estadoPaquete = consultarPaquete(clienteId, paqueteId);

                // Cifrar el estado
                String estadoCifrado = CifradoAES.cifrarMensaje(estadoPaquete, llaveAES, iv);

                // Firmar el estado cifrado
                String firma = firmarMensaje(estadoCifrado);
                salida.writeUTF(estadoCifrado);
                salida.writeUTF(firma);

                long finConsulta = System.nanoTime(); // Medir tiempo de fin
                long tiempoRespuesta = (finConsulta - inicioConsulta) / 1_000_000; // Convertir a milisegundos
                registrarEnCSV("Escenario de Prueba", clienteId, paqueteId, tiempoRespuesta);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void registrarEnCSV(String escenario, String clienteId, String paqueteId, long tiempo) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCHIVO_CSV, true))) {
            writer.printf("%s,%s,%s,%d%n", escenario, clienteId, paqueteId, tiempo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String consultarPaquete(String clienteId, String paqueteId) {
        Paquete paquete = tablaPaquetes.get(paqueteId);
        if (paquete != null && paquete.getClienteId().equals(clienteId)) {
            return obtenerEstadoComoTexto(paquete.getEstado());
        }
        return "DESCONOCIDO";
    }

    private String obtenerEstadoComoTexto(int estado) {
        switch (estado) {
            case Paquete.ENOFICINA: return "EN OFICINA";
            case Paquete.RECOGIDO: return "RECOGIDO";
            case Paquete.ENCLASIFICACION: return "EN CLASIFICACION";
            case Paquete.DESPACHADO: return "DESPACHADO";
            case Paquete.ENENTREGA: return "EN ENTREGA";
            case Paquete.ENTREGADO: return "ENTREGADO";
            default: return "DESCONOCIDO";
        }
    }

    private String firmarMensaje(String mensaje) throws Exception {
        Signature firma = Signature.getInstance("SHA1withRSA");
        firma.initSign(llavePrivada);
        firma.update(mensaje.getBytes());
        byte[] firmaBytes = firma.sign();
        return Base64.getEncoder().encodeToString(firmaBytes);
    }

    public static void main(String[] args) {
        int numeroDelegados = 4; // Cambia este valor para las pruebas: 4, 8, o 32
        ServidorPaquetesSeguroAutenticado servidor = new ServidorPaquetesSeguroAutenticado(numeroDelegados);
        servidor.iniciar();
    }
}
