import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class CifradoAES {
    private static final String ALGORITMO = "AES/CBC/PKCS5Padding";
    private static final int TAMANO_LLAVE = 256;

    // Método para generar la llave AES
    public static SecretKey generarLlaveAES() throws NoSuchAlgorithmException {
        KeyGenerator generador = KeyGenerator.getInstance("AES");
        generador.init(TAMANO_LLAVE);
        return generador.generateKey();
    }

    // Método para generar un vector de inicialización (IV) aleatorio
    public static IvParameterSpec generarIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    // Método para cifrar un mensaje
    public static String cifrarMensaje(String mensaje, SecretKey llave, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.ENCRYPT_MODE, llave, iv);
        byte[] mensajeCifrado = cipher.doFinal(mensaje.getBytes());
        return Base64.getEncoder().encodeToString(mensajeCifrado);
    }

    // Método para descifrar un mensaje
    public static String descifrarMensaje(String mensajeCifrado, SecretKey llave, IvParameterSpec iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(Cipher.DECRYPT_MODE, llave, iv);
        byte[] mensajeDescifrado = cipher.doFinal(Base64.getDecoder().decode(mensajeCifrado));
        return new String(mensajeDescifrado);
    }
}
