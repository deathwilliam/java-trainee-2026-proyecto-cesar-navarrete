package sv.resguardo.api.excepcion;

// R7: la cuenta no pertenece a quien corresponde o esta inactiva
public class CuentaInvalidaException extends OperacionRechazadaException {

    public CuentaInvalidaException(String mensaje) {
        super(mensaje);
    }
}