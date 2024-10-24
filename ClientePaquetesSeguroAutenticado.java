import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ClientePaquetesSeguroAutenticado {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;
    private static PublicKey llavePublica;
    private static SecretKey llaveAES;
    private static IvParameterSpec iv;

    public static void main(String[] args) {
        try {
            llavePublica = cargarLlavePublica();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ClientePaquetesSeguroAutenticado cliente = new ClientePaquetesSeguroAutenticado();
        cliente.enviarConsulta("cliente1", "paquete1");
    }

    private static PublicKey cargarLlavePublica() throws Exception {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("llave_publica.pem"))) {
            return (PublicKey) ois.readObject();
        }
    }

    public void enviarConsulta(String clienteId, String paqueteId) {
        try (Socket socket = new Socket(HOST, PUERTO);
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            // Recibir la llave AES y el IV del servidor
            int longitudLlave = entrada.readInt();
            byte[] llaveBytes = new byte[longitudLlave];
            entrada.readFully(llaveBytes);
            llaveAES = new SecretKeySpec(llaveBytes, "AES");

            int longitudIV = entrada.readInt();
            byte[] ivBytes = new byte[longitudIV];
            entrada.readFully(ivBytes);
            iv = new IvParameterSpec(ivBytes);

            // Enviar identificador de cliente y paquete al servidor
            salida.writeUTF(clienteId);
            salida.writeUTF(paqueteId);

            // Recibir respuesta cifrada y firma del servidor
            String respuestaCifrada = entrada.readUTF();
            String firma = entrada.readUTF();

            // Verificar la firma
            if (verificarFirma(respuestaCifrada, firma)) {
                // Descifrar el mensaje si la firma es válida
                String respuestaDescifrada = CifradoAES.descifrarMensaje(respuestaCifrada, llaveAES, iv);

                if (respuestaDescifrada.equals("DESCONOCIDO")) {
                    System.out.println("Error en la consulta. El paquete no se encontró o no pertenece al cliente.");
                } else {
                    System.out.println("Estado del paquete: " + respuestaDescifrada);
                }
            } else {
                System.out.println("Error en la consulta. La firma no coincide.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean verificarFirma(String mensaje, String firma) throws Exception {
        Signature verificarFirma = Signature.getInstance("SHA1withRSA");
        verificarFirma.initVerify(llavePublica);
        verificarFirma.update(mensaje.getBytes());
        byte[] firmaBytes = Base64.getDecoder().decode(firma);
        return verificarFirma.verify(firmaBytes);
    }
}
