package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.BitacoraOperacion;
import sv.resguardo.api.entidad.enums.ResultadoOperacion;
import sv.resguardo.api.entidad.enums.TipoOperacion;

import java.util.List;

public interface BitacoraOperacionRepositorio extends JpaRepository<BitacoraOperacion, Long> {

    long countByOperacionAndResultado(TipoOperacion operacion, ResultadoOperacion resultado);
    List<BitacoraOperacion> findTop20ByOrderByIdDesc();
}
