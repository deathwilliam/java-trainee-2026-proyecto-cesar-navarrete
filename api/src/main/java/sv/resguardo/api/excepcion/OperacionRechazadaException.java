package sv.resguardo.api.excepcion;

public class OperacionRechazadaException extends RuntimeException {

    public OperacionRechazadaException(String mensaje) {
        super(mensaje);
    }
}