package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.UsuarioRol;

import java.util.List;

public interface UsuarioRolRepositorio extends JpaRepository<UsuarioRol, Long> {

    List<UsuarioRol> findByUsuarioId(Long usuarioId);
}