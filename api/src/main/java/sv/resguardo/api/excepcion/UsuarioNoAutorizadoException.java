package sv.resguardo.api.excepcion;

// R6: quien hace la operacion no es la parte que corresponde
public class UsuarioNoAutorizadoException extends OperacionRechazadaException {

    public UsuarioNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}