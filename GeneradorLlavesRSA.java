import java.io.*;
import java.security.*;

public class GeneradorLlavesRSA {
    private static final String LLAVE_PUBLICA = "llave_publica.pem";
    private static final String LLAVE_PRIVADA = "llave_privada.pem";

    public static void main(String[] args) {
        try {
            // Generar par de llaves RSA
            KeyPairGenerator generadorLlaves = KeyPairGenerator.getInstance("RSA");
            generadorLlaves.initialize(1024);
            KeyPair parLlaves = generadorLlaves.generateKeyPair();
            PrivateKey llavePrivada = parLlaves.getPrivate();
            PublicKey llavePublica = parLlaves.getPublic();

            // Guardar las llaves en archivos
            guardarLlave(LLAVE_PUBLICA, llavePublica);
            guardarLlave(LLAVE_PRIVADA, llavePrivada);
            System.out.println("Llaves generadas y guardadas correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para guardar la llave en un archivo
    private static void guardarLlave(String nombreArchivo, Key llave) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(nombreArchivo);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(llave);
        }
    }
}
