package sv.resguardo.api.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import sv.resguardo.api.entidad.CuentaBancaria;

import java.util.Optional;

public interface CuentaBancariaRepositorio extends JpaRepository<CuentaBancaria, Long> {

    Optional<CuentaBancaria> findByNumeroCuenta(String numeroCuenta);
}