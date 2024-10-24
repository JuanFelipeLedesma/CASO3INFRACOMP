import java.io.*;
import java.net.*;

public class ClientePaquetes {
    private static final String HOST = "localhost";
    private static final int PUERTO = 12345;

    public static void main(String[] args) {
        ClientePaquetes cliente = new ClientePaquetes();
        cliente.enviarConsulta("cliente1", "paquete1");
    }

    public void enviarConsulta(String clienteId, String paqueteId) {
        try (Socket socket = new Socket(HOST, PUERTO);
             DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
             DataInputStream entrada = new DataInputStream(socket.getInputStream())) {

            // Enviar identificador de cliente y paquete al servidor
            salida.writeUTF(clienteId);
            salida.writeUTF(paqueteId);

            // Recibir respuesta del servidor
            String respuesta = entrada.readUTF();

            // Validar y mostrar la respuesta
            if (respuesta.equals("DESCONOCIDO")) {
                System.out.println("Error en la consulta. El paquete no se encontró o no pertenece al cliente.");
            } else {
                System.out.println("Estado del paquete: " + respuesta);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
