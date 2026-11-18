package sv.resguardo.api.excepcion;

// R4: el contrato no esta en un estado que permita la operacion
public class EstadoInvalidoException extends OperacionRechazadaException {

    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }
}