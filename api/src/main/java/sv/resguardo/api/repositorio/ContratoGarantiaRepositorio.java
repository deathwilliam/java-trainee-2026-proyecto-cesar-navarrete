package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sv.resguardo.api.entidad.ContratoGarantia;

import java.util.Optional;

public interface ContratoGarantiaRepositorio extends JpaRepository<ContratoGarantia, Long> {

    Optional<ContratoGarantia> findByNumeroContrato(String numeroContrato);

    @Query("select c from ContratoGarantia c "
            + "join fetch c.vendedor join fetch c.comprador join fetch c.cuentaPago "
            + "where c.numeroContrato = :numero")
    Optional<ContratoGarantia> conPartes(@Param("numero") String numero);
}