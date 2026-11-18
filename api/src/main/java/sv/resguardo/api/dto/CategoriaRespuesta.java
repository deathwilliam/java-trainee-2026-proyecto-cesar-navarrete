package sv.resguardo.api.dto;

import sv.resguardo.api.entidad.Categoria;

import java.math.BigDecimal;

public record CategoriaRespuesta(String codigo, String nombre, BigDecimal montoMinimo,
                                 BigDecimal montoMaximo, BigDecimal porcentajeComision) {

    public static CategoriaRespuesta de(Categoria c) {
        return new CategoriaRespuesta(c.getCodigo(), c.getNombre(), c.getMontoMinimo(),
                c.getMontoMaximo(), c.getPorcentajeComision());
    }
}