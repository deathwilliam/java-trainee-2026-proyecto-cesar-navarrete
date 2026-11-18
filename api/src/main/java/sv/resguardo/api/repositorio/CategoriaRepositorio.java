package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.Categoria;

import java.util.Optional;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findByCodigo(String codigo);
}