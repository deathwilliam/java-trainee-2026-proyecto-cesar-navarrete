package sv.resguardo.api.repositorio;
import sv.resguardo.api.entidad.enums.ConceptoLiquidacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.resguardo.api.entidad.DetalleLiquidacion;

import java.util.Optional;
import java.math.BigDecimal;

public interface DetalleLiquidacionRepositorio extends JpaRepository<DetalleLiquidacion, Long> {

    @Query("select coalesce(sum(d.monto), 0) from DetalleLiquidacion d "
            + "where d.liquidacion.contrato.numeroContrato = :numero")
    BigDecimal totalLiquidado(@Param("numero") String numero);

    Optional<DetalleLiquidacion> findByLiquidacionIdAndConcepto(Long liquidacionId, ConceptoLiquidacion concepto);
}