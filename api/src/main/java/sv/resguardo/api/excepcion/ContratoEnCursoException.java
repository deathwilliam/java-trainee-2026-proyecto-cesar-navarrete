package sv.resguardo.api.excepcion;

// El contrato ya tiene dinero o datos que dependen de el: no se puede cancelar
public class ContratoEnCursoException extends OperacionRechazadaException {

    public ContratoEnCursoException(String mensaje) {
        super(mensaje);
    }
}