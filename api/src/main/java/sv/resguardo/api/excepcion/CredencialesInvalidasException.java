package sv.resguardo.api.excepcion;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o clave incorrectos");
    }
}