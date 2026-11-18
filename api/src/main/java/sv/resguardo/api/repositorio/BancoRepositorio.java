package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.Banco;

public interface BancoRepositorio extends JpaRepository<Banco, Long> {
}