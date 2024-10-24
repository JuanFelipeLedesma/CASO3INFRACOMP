import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ServidorPaquetesSeguro {
    private static final int PUERTO = 12345;
    private Map<String, Paquete> tablaPaquetes;
    private SecretKey llaveAES;
    private IvParameterSpec iv;
    private ExecutorService pool;

    public ServidorPaquetesSeguro() {
        tablaPaquetes = new HashMap<>();
        inicializarPaquetes();
        pool = Executors.newCachedThreadPool();
        try {
            llaveAES = CifradoAES.generarLlaveAES();
            iv = CifradoAES.generarIV();
        } catch (NoSuchAlgorithmException e) {
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

    public void iniciar() {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor seguro iniciado en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado desde: " + cliente.getInetAddress());
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
            try (
                DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                DataOutputStream salida = new DataOutputStream(cliente.getOutputStream())
            ) {
                // Recibir datos del cliente
                String clienteId = entrada.readUTF();
                String paqueteId = entrada.readUTF();

                // Consultar el estado del paquete
                String estadoPaquete = consultarPaquete(clienteId, paqueteId);

                // Cifrar el estado antes de enviarlo
                String estadoCifrado = CifradoAES.cifrarMensaje(estadoPaquete, llaveAES, iv);
                salida.writeUTF(estadoCifrado);
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

    public static void main(String[] args) {
        ServidorPaquetesSeguro servidor = new ServidorPaquetesSeguro();
        servidor.iniciar();
    }
}