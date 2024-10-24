import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ClientePaquetesSeguro {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    // Llave y IV deben coincidir con los del servidor (en una implementación real, se intercambiarían de manera segura)
    private static SecretKey llaveAES;
    private static IvParameterSpec iv;

    public static void main(String[] args) {
        try {
            llaveAES = CifradoAES.generarLlaveAES(); // Debe coincidir con la generada en el servidor
            iv = CifradoAES.generarIV(); // Debe coincidir con la generada en el servidor
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        ClientePaquetesSeguro cliente = new ClientePaquetesSeguro();
        cliente.enviarConsulta("cliente1", "paquete1");
    }

    public void enviarConsulta(String clienteId, String paqueteId) {
        try (Socket socket = new Socket(HOST, PUERTO);
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            // Enviar identificador de cliente y paquete al servidor
            salida.writeUTF(clienteId);
            salida.writeUTF(paqueteId);

            // Recibir respuesta cifrada del servidor
            String respuestaCifrada = entrada.readUTF();

            // Descifrar el mensaje
            String respuestaDescifrada = CifradoAES.descifrarMensaje(respuestaCifrada, llaveAES, iv);

            // Validar y mostrar la respuesta
            if (respuestaDescifrada.equals("DESCONOCIDO")) {
                System.out.println("Error en la consulta. El paquete no se encontró o no pertenece al cliente.");
            } else {
                System.out.println("Estado del paquete: " + respuestaDescifrada);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
