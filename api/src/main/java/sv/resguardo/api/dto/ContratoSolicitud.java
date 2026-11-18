package sv.resguardo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ContratoSolicitud(
        @NotBlank(message = "El DUI del comprador es obligatorio") String duiComprador,
        @NotBlank(message = "La categoria es obligatoria") String categoria,
        @NotBlank(message = "La cuenta de pago es obligatoria") String cuentaPago,
        @NotBlank(message = "La descripcion es obligatoria")
        @Size(max = 255, message = "La descripcion admite hasta 255 caracteres") String descripcion,
        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor que cero") BigDecimal monto) {
}