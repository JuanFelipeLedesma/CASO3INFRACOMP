public class Paquete {
    private String clienteId;
    private String paqueteId;
    private int estado;

    // Constantes para los estados de los paquetes
    public static final int ENOFICINA = 0;
    public static final int RECOGIDO = 1;
    public static final int ENCLASIFICACION = 2;
    public static final int DESPACHADO = 3;
    public static final int ENENTREGA = 4;
    public static final int ENTREGADO = 5;
    public static final int DESCONOCIDO = 6;

    // Constructor
    public Paquete(String clienteId, String paqueteId, int estado) {
        this.clienteId = clienteId;
        this.paqueteId = paqueteId;
        this.estado = estado;
    }

    // Getters y setters
    public String getClienteId() {
        return clienteId;
    }

    public String getPaqueteId() {
        return paqueteId;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
}

