package sv.resguardo.api.dto;

import sv.resguardo.api.entidad.ContratoGarantia;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ContratoRespuesta(
        String numeroContrato,
        String vendedor,
        String comprador,
        String categoria,
        String descripcion,
        BigDecimal monto,
        BigDecimal porcentajeComision,
        String estado,
        LocalDateTime creadoEn) {

    public static ContratoRespuesta de(ContratoGarantia c) {
        return new ContratoRespuesta(
                c.getNumeroContrato(),
                c.getVendedor().getNombres() + " " + c.getVendedor().getApellidos(),
                c.getComprador().getNombres() + " " + c.getComprador().getApellidos(),
                c.getCategoria().getNombre(),
                c.getDescripcion(),
                c.getMonto(),
                c.getPorcentajeComision(),
                c.getEstado().name(),
                c.getCreadoEn());
    }
}