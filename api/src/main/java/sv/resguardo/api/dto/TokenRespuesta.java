package sv.resguardo.api.dto;

public record TokenRespuesta(String token, String tipo, long expiraEnSegundos) {
}