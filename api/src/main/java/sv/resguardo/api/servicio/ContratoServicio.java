package sv.resguardo.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.resguardo.api.dto.ContratoEdicion;
import sv.resguardo.api.dto.ContratoRespuesta;
import sv.resguardo.api.dto.ContratoSolicitud;
import sv.resguardo.api.entidad.Categoria;
import sv.resguardo.api.entidad.Cliente;
import sv.resguardo.api.entidad.ContratoGarantia;
import sv.resguardo.api.entidad.CuentaBancaria;
import sv.resguardo.api.entidad.enums.EstadoContrato;
import sv.resguardo.api.entidad.enums.TipoOperacion;
import sv.resguardo.api.excepcion.*;
import sv.resguardo.api.repositorio.CategoriaRepositorio;
import sv.resguardo.api.repositorio.ClienteRepositorio;
import sv.resguardo.api.repositorio.ContratoGarantiaRepositorio;
import sv.resguardo.api.repositorio.CuentaBancariaRepositorio;

import java.time.Year;
import java.util.List;

@Service
public class ContratoServicio {

    private final ContratoGarantiaRepositorio contratos;
    private final ClienteRepositorio clientes;
    private final CategoriaRepositorio categorias;
    private final CuentaBancariaRepositorio cuentas;
    private final BitacoraServicio bitacora;

    public ContratoServicio(ContratoGarantiaRepositorio contratos, ClienteRepositorio clientes,
                            CategoriaRepositorio categorias, CuentaBancariaRepositorio cuentas,
                            BitacoraServicio bitacora) {
        this.contratos = contratos;
        this.clientes = clientes;
        this.categorias = categorias;
        this.cuentas = cuentas;
        this.bitacora = bitacora;
    }

    @Transactional(readOnly = true)
    public List<ContratoRespuesta> listar() {
        return contratos.findAll().stream()
                .map(ContratoRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContratoRespuesta porNumero(String numero) {
        return contratos.findByNumeroContrato(numero)
                .map(ContratoRespuesta::de)
                .orElseThrow(() -> new ContratoNoEncontradoException("No existe el contrato " + numero));
    }

    @Transactional
    public ContratoRespuesta crear(ContratoSolicitud solicitud, String nombreUsuario) {
        Cliente vendedor = clientes.findByUsuarioNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new OperacionRechazadaException(
                        "El usuario " + nombreUsuario + " no es un cliente"));

        Cliente comprador = clientes.findByDui(solicitud.duiComprador())
                .orElseThrow(() -> new OperacionRechazadaException(
                        "No existe un cliente con DUI " + solicitud.duiComprador()));

        if (vendedor.getId().equals(comprador.getId())) {
            throw new OperacionRechazadaException("El vendedor y el comprador no pueden ser la misma persona");
        }

        Categoria categoria = categorias.findByCodigo(solicitud.categoria())
                .filter(Categoria::isActivo)
                .orElseThrow(() -> new OperacionRechazadaException(
                        "No existe la categoria " + solicitud.categoria() + " o esta inactiva"));

        // R1
        if (solicitud.monto().compareTo(categoria.getMontoMinimo()) < 0
                || solicitud.monto().compareTo(categoria.getMontoMaximo()) > 0) {
            throw new OperacionRechazadaException("R1: el monto para " + categoria.getNombre()
                    + " debe estar entre " + categoria.getMontoMinimo() + " y " + categoria.getMontoMaximo());
        }

        // R7
        CuentaBancaria cuentaPago = cuentas.findByNumeroCuenta(solicitud.cuentaPago())
                .filter(c -> c.isActivo() && c.getCliente().getId().equals(vendedor.getId()))
                .orElseThrow(() -> new CuentaInvalidaException(
                        "La cuenta " + solicitud.cuentaPago() + " no pertenece al vendedor o esta inactiva"));

        String numero = String.format("GAR-%d-%06d", Year.now().getValue(), contratos.count() + 1);

        ContratoGarantia contrato = contratos.save(new ContratoGarantia(numero, vendedor, comprador,
                categoria, cuentaPago, solicitud.descripcion(), solicitud.monto()));

        bitacora.registrarExitosa(vendedor.getUsuario(), contrato, TipoOperacion.CREAR_CONTRATO,
                "Contrato " + numero + " por " + solicitud.monto());

        return ContratoRespuesta.de(contrato);
    }

    @Transactional
    public ContratoRespuesta editar(String numero, ContratoEdicion edicion) {
        ContratoGarantia contrato = contratos.findByNumeroContrato(numero)
                .orElseThrow(() -> new ContratoNoEncontradoException("No existe el contrato " + numero));

        if (contrato.getEstado() != EstadoContrato.CREADO) {
            throw new EstadoInvalidoException("El contrato " + numero + " está "
                    + contrato.getEstado() + ": solo se edita mientras está CREADO");
        }

        contrato.setDescripcion(edicion.descripcion());
        return ContratoRespuesta.de(contrato);
    }

    @Transactional
    public void cancelar(String numero) {
        ContratoGarantia contrato = contratos.findByNumeroContrato(numero)
                .orElseThrow(() -> new ContratoNoEncontradoException("No existe el contrato " + numero));

        if (contrato.getEstado() != EstadoContrato.CREADO) {
            throw new ContratoEnCursoException("El contrato " + numero + " está "
                    + contrato.getEstado() + ": solo se cancela mientras está CREADO");
        }

        contrato.setEstado(EstadoContrato.CANCELADO);
    }
}