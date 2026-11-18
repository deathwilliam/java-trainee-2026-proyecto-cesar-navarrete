package sv.resguardo.api.dto;

import sv.resguardo.api.entidad.BitacoraOperacion;

import java.time.LocalDateTime;

public record BitacoraRespuesta(String usuario, String contrato, String operacion,
                                String resultado, String detalle, LocalDateTime fechaHora) {

    public static BitacoraRespuesta de(BitacoraOperacion fila) {
        String contrato = fila.getContrato() == null ? null : fila.getContrato().getNumeroContrato();
        return new BitacoraRespuesta(fila.getUsuario().getNombreUsuario(), contrato,
                fila.getOperacion().name(), fila.getResultado().name(),
                fila.getDetalle(), fila.getFechaHora());
    }
}