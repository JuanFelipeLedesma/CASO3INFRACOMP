import java.util.HashMap;
import java.util.Map;

public class ServidorPaquetes {
    // Mapa para almacenar los paquetes
    private Map<String, Paquete> tablaPaquetes;

    public ServidorPaquetes() {
        tablaPaquetes = new HashMap<>();
        inicializarPaquetes();
    }

    // Método para inicializar la tabla con datos predefinidos
    private void inicializarPaquetes() {
        // Se agregan 32 paquetes de ejemplo (puedes personalizar los IDs y estados)
        for (int i = 1; i <= 32; i++) {
            String clienteId = "cliente" + i;
            String paqueteId = "paquete" + i;
            int estado = Paquete.ENOFICINA; // Estado inicial para todos los paquetes
            tablaPaquetes.put(paqueteId, new Paquete(clienteId, paqueteId, estado));
        }
    }

    // Método para consultar el estado de un paquete
    public String consultarPaquete(String clienteId, String paqueteId) {
        Paquete paquete = tablaPaquetes.get(paqueteId);
        if (paquete != null && paquete.getClienteId().equals(clienteId)) {
            return obtenerEstadoComoTexto(paquete.getEstado());
        }
        return "DESCONOCIDO";
    }

    // Método para convertir el estado numérico a texto
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
        ServidorPaquetes servidor = new ServidorPaquetes();
        // Ejemplo de consulta
        String resultado = servidor.consultarPaquete("cliente1", "paquete1");
        System.out.println("Estado del paquete: " + resultado);
    }
}
