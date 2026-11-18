package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.Liquidacion;

import java.util.Optional;

public interface LiquidacionRepositorio extends JpaRepository<Liquidacion, Long> {

    boolean existsByContratoId(Long contratoId);

    Optional<Liquidacion> findByContratoNumeroContrato(String numeroContrato);
}