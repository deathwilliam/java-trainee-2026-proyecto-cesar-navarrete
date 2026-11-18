package sv.resguardo.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.resguardo.api.entidad.ContratoGarantia;
import sv.resguardo.api.entidad.CuentaBancaria;
import sv.resguardo.api.entidad.DetalleLiquidacion;
import sv.resguardo.api.entidad.Liquidacion;
import sv.resguardo.api.entidad.Usuario;
import sv.resguardo.api.entidad.enums.ConceptoLiquidacion;
import sv.resguardo.api.entidad.enums.EstadoContrato;
import sv.resguardo.api.entidad.enums.TipoLiquidacion;
import sv.resguardo.api.entidad.enums.TipoOperacion;
import sv.resguardo.api.excepcion.*;
import sv.resguardo.api.repositorio.ContratoGarantiaRepositorio;
import sv.resguardo.api.repositorio.DetalleLiquidacionRepositorio;
import sv.resguardo.api.repositorio.LiquidacionRepositorio;
import sv.resguardo.api.repositorio.UsuarioRepositorio;
import sv.resguardo.api.dto.LiquidacionRespuesta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LiberacionServicio {

    // El log
    private static final Logger log = LoggerFactory.getLogger(LiberacionServicio.class);

    private final ContratoGarantiaRepositorio contratos;
    private final LiquidacionRepositorio liquidaciones;
    private final DetalleLiquidacionRepositorio detalles;
    private final UsuarioRepositorio usuarios;
    private final BitacoraServicio bitacora;

    public LiberacionServicio(ContratoGarantiaRepositorio contratos, LiquidacionRepositorio liquidaciones,
                              DetalleLiquidacionRepositorio detalles, UsuarioRepositorio usuarios,
                              BitacoraServicio bitacora) {
        this.contratos = contratos;
        this.liquidaciones = liquidaciones;
        this.detalles = detalles;
        this.usuarios = usuarios;
        this.bitacora = bitacora;
    }

    @Transactional
    public LiquidacionRespuesta liberarFondos(String numeroContrato, String nombreUsuario) {
        try {
            return ejecutar(numeroContrato, nombreUsuario);
        } catch (RuntimeException e) {
            bitacora.registrarRechazada(nombreUsuario, numeroContrato, TipoOperacion.LIBERAR_FONDOS, e.getMessage());
            log.warn("Liberacion rechazada: contrato {} (usuario {}): {}", numeroContrato, nombreUsuario, e.getMessage());
            throw e;
        }
    }

    private LiquidacionRespuesta ejecutar(String numeroContrato, String nombreUsuario) {

        // Validaciones previas
        ContratoGarantia contrato = contratos.findByNumeroContrato(numeroContrato)
                .orElseThrow(() -> new ContratoNoEncontradoException(
                        "No existe el contrato " + numeroContrato));

        Usuario usuario = usuarios.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsuarioNoAutorizadoException(
                        "No existe el usuario " + nombreUsuario));

        // R4
        if (contrato.getEstado() != EstadoContrato.ENVIADO) {
            throw new EstadoInvalidoException("El contrato " + numeroContrato + " está "
                    + contrato.getEstado() + ": todavía no se puede liberar");
        }

        // R6
        if (!contrato.getComprador().getUsuario().getId().equals(usuario.getId())) {
            throw new UsuarioNoAutorizadoException("Solo el comprador puede liberar los fondos");
        }

        // R7
        CuentaBancaria cuentaPago = contrato.getCuentaPago();
        if (!cuentaPago.isActivo()
                || !cuentaPago.getCliente().getId().equals(contrato.getVendedor().getId())) {
            throw new CuentaInvalidaException("La cuenta no pertenece al vendedor o está inactiva");
        }

        // R9
        if (liquidaciones.existsByContratoId(contrato.getId())) {
            throw new EstadoInvalidoException("El contrato " + numeroContrato + " ya fue liquidado");
        }

        // 1. La liquidacion
        String comprobante = "LIQ" + numeroContrato.substring(3);
        Liquidacion liquidacion = liquidaciones.save(
                new Liquidacion(contrato, comprobante, TipoLiquidacion.LIBERACION));

        // 2. Los dos detalles (R3)
        BigDecimal monto = contrato.getMonto();
        BigDecimal comision = monto.multiply(contrato.getPorcentajeComision())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal pagoVendedor = monto.subtract(comision);

        detalles.save(new DetalleLiquidacion(liquidacion, ConceptoLiquidacion.COMISION, comision));
        detalles.save(new DetalleLiquidacion(liquidacion, ConceptoLiquidacion.PAGO_VENDEDOR, pagoVendedor));

        if (detalles.totalLiquidado(numeroContrato).compareTo(monto) != 0) {
            throw new IllegalStateException("R3: los detalles no suman el monto del contrato");
        }

        // 3. El contrato pasa a LIBERADO
        contrato.setEstado(EstadoContrato.LIBERADO);

        // 4. La bitacora
        bitacora.registrarExitosa(usuario, contrato, TipoOperacion.LIBERAR_FONDOS, "Comprobante " + comprobante);
        log.info("Liberacion realizada: contrato {} (usuario {}): comision {} y pago al vendedor {}",
                numeroContrato, nombreUsuario, comision, pagoVendedor);

        return new LiquidacionRespuesta(comprobante, numeroContrato, liquidacion.getFechaHora(),
                monto, comision, pagoVendedor);
    }

    @Transactional(readOnly = true)
    public LiquidacionRespuesta consultar(String numeroContrato) {
        ContratoGarantia contrato = contratos.findByNumeroContrato(numeroContrato)
                .orElseThrow(() -> new ContratoNoEncontradoException(
                        "No existe el contrato " + numeroContrato));

        Liquidacion liquidacion = liquidaciones.findByContratoNumeroContrato(numeroContrato)
                .orElseThrow(() -> new LiquidacionNoEncontradaException(
                        "El contrato " + numeroContrato + " todavía no tiene liquidación"));

        BigDecimal comision = detalles.findByLiquidacionIdAndConcepto(liquidacion.getId(), ConceptoLiquidacion.COMISION)
                .map(DetalleLiquidacion::getMonto)
                .orElse(BigDecimal.ZERO);
        BigDecimal pagoVendedor = detalles.findByLiquidacionIdAndConcepto(liquidacion.getId(), ConceptoLiquidacion.PAGO_VENDEDOR)
                .map(DetalleLiquidacion::getMonto)
                .orElse(BigDecimal.ZERO);

        return new LiquidacionRespuesta(liquidacion.getNumeroComprobante(), numeroContrato,
                liquidacion.getFechaHora(), contrato.getMonto(), comision, pagoVendedor);
    }

}