package sv.resguardo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContratoEdicion(
        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255, message = "La descripcion admite hasta 255 caracteres") String descripcion) {
}