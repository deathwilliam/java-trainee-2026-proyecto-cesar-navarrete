package sv.resguardo.api.excepcion;

// No existe un contrato con ese numero
public class ContratoNoEncontradoException extends OperacionRechazadaException {

    public ContratoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}