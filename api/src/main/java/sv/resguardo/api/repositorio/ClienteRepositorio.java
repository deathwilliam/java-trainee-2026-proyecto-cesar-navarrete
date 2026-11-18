package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.Cliente;

import java.util.Optional;

public interface ClienteRepositorio extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByDui(String dui);

    Optional<Cliente> findByUsuarioNombreUsuario(String nombreUsuario);
}