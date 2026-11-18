package sv.resguardo.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LiquidacionRespuesta(
        String numeroComprobante,
        String numeroContrato,
        LocalDateTime fechaHora,
        BigDecimal monto,
        BigDecimal comision,
        BigDecimal pagoVendedor) {
}