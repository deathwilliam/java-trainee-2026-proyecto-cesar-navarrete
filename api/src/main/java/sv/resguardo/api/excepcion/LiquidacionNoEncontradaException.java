package sv.resguardo.api.excepcion;

// El contrato existe pero todavia no tiene liquidacion
public class LiquidacionNoEncontradaException extends OperacionRechazadaException {

    public LiquidacionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}