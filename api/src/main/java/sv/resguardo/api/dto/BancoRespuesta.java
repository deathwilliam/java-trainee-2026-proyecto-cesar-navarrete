package sv.resguardo.api.dto;

import sv.resguardo.api.entidad.Banco;

public record BancoRespuesta(String codigo, String nombre) {

    public static BancoRespuesta de(Banco b) {
        return new BancoRespuesta(b.getCodigo(), b.getNombre());
    }
}