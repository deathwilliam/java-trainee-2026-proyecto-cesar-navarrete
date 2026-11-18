package sv.resguardo.api.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sv.resguardo.api.dto.LiquidacionRespuesta;
import sv.resguardo.api.entidad.Banco;
import sv.resguardo.api.entidad.Categoria;
import sv.resguardo.api.entidad.Cliente;
import sv.resguardo.api.entidad.ContratoGarantia;
import sv.resguardo.api.entidad.CuentaBancaria;
import sv.resguardo.api.entidad.DetalleLiquidacion;
import sv.resguardo.api.entidad.Liquidacion;
import sv.resguardo.api.entidad.Usuario;
import sv.resguardo.api.entidad.enums.ConceptoLiquidacion;
import sv.resguardo.api.entidad.enums.EstadoContrato;
import sv.resguardo.api.entidad.enums.TipoCuenta;
import sv.resguardo.api.entidad.enums.TipoOperacion;
import sv.resguardo.api.excepcion.ContratoNoEncontradoException;
import sv.resguardo.api.excepcion.CuentaInvalidaException;
import sv.resguardo.api.excepcion.EstadoInvalidoException;
import sv.resguardo.api.excepcion.UsuarioNoAutorizadoException;
import sv.resguardo.api.repositorio.ContratoGarantiaRepositorio;
import sv.resguardo.api.repositorio.DetalleLiquidacionRepositorio;
import sv.resguardo.api.repositorio.LiquidacionRepositorio;
import sv.resguardo.api.repositorio.UsuarioRepositorio;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiberacionReglasTest {

    private static final String NUMERO = "GAR-2026-000001";

    @Mock private ContratoGarantiaRepositorio contratos;
    @Mock private LiquidacionRepositorio liquidaciones;
    @Mock private DetalleLiquidacionRepositorio detalles;
    @Mock private UsuarioRepositorio usuarios;
    @Mock private BitacoraServicio bitacora;

    @InjectMocks private LiberacionServicio liberaciones;

    private Usuario usuarioVendedor;
    private Usuario usuarioComprador;
    private CuentaBancaria cuentaPago;
    private ContratoGarantia contrato;

    @BeforeEach
    void armarDatos() {
        usuarioVendedor = new Usuario("mlopez", "hash");
        usuarioComprador = new Usuario("lmartinez", "hash");
        ReflectionTestUtils.setField(usuarioVendedor, "id", 3L);
        ReflectionTestUtils.setField(usuarioComprador, "id", 4L);

        Cliente vendedor = new Cliente("01234567-8", "Marta", "Lopez",
                "marta.lopez@correo.com", "7123-4567", usuarioVendedor);
        Cliente comprador = new Cliente("02345678-9", "Luis", "Martinez",
                "luis.martinez@correo.com", "7234-5678", usuarioComprador);
        ReflectionTestUtils.setField(vendedor, "id", 1L);
        ReflectionTestUtils.setField(comprador, "id", 2L);

        cuentaPago = new CuentaBancaria(vendedor, new Banco("CUSCATLAN", "Banco Cuscatlan"),
                "100200300", TipoCuenta.AHORRO);
        Categoria electronica = new Categoria("ELECTRONICA", "Electronica",
                new BigDecimal("50.00"), new BigDecimal("5000.00"), new BigDecimal("0.0300"));

        contrato = new ContratoGarantia(NUMERO, vendedor, comprador, electronica, cuentaPago,
                "Laptop Lenovo IdeaPad usada", new BigDecimal("350.00"));
        ReflectionTestUtils.setField(contrato, "id", 1L);
        contrato.setEstado(EstadoContrato.ENVIADO);
    }

    @Test
    void liberacionBuenaPagaAlVendedorYCobraLaComision() {
        when(contratos.findByNumeroContrato(NUMERO)).thenReturn(Optional.of(contrato));
        when(usuarios.findByNombreUsuario("lmartinez")).thenReturn(Optional.of(usuarioComprador));
        when(liquidaciones.save(any(Liquidacion.class))).thenAnswer(inv -> inv.getArgument(0));
        when(detalles.totalLiquidado(NUMERO)).thenReturn(new BigDecimal("350.00"));

        LiquidacionRespuesta respuesta = liberaciones.liberarFondos(NUMERO, "lmartinez");

        assertEquals("LIQ-2026-000001", respuesta.numeroComprobante());
        assertEquals(new BigDecimal("10.50"), respuesta.comision());
        assertEquals(new BigDecimal("339.50"), respuesta.pagoVendedor());
        assertEquals(EstadoContrato.LIBERADO, contrato.getEstado());

        ArgumentCaptor<DetalleLiquidacion> grabados = ArgumentCaptor.forClass(DetalleLiquidacion.class);
        verify(detalles, times(2)).save(grabados.capture());
        List<DetalleLiquidacion> lista = grabados.getAllValues();
        assertEquals(ConceptoLiquidacion.COMISION, lista.get(0).getConcepto());
        assertEquals(new BigDecimal("10.50"), lista.get(0).getMonto());
        assertEquals(ConceptoLiquidacion.PAGO_VENDEDOR, lista.get(1).getConcepto());
        assertEquals(new BigDecimal("339.50"), lista.get(1).getMonto());

        verify(bitacora).registrarExitosa(eq(usuarioComprador), eq(contrato),
                eq(TipoOperacion.LIBERAR_FONDOS), anyString());
        verify(bitacora, never()).registrarRechazada(anyString(), anyString(), any(), anyString());
    }

    @Test
    void contratoQueNoExisteSeRechazaYQuedaEnLaBitacora() {
        when(contratos.findByNumeroContrato("GAR-2026-999999")).thenReturn(Optional.empty());

        ContratoNoEncontradoException e = assertThrows(ContratoNoEncontradoException.class,
                () -> liberaciones.liberarFondos("GAR-2026-999999", "lmartinez"));

        assertEquals("No existe el contrato GAR-2026-999999", e.getMessage());
        verifyNoInteractions(usuarios, liquidaciones, detalles);
        verify(bitacora).registrarRechazada("lmartinez", "GAR-2026-999999",
                TipoOperacion.LIBERAR_FONDOS, e.getMessage());
    }

    @Test
    void r4SoloSeLiberaUnContratoEnviado() {
        contrato.setEstado(EstadoContrato.FONDEADO);
        when(contratos.findByNumeroContrato(NUMERO)).thenReturn(Optional.of(contrato));
        when(usuarios.findByNombreUsuario("lmartinez")).thenReturn(Optional.of(usuarioComprador));

        EstadoInvalidoException e = assertThrows(EstadoInvalidoException.class,
                () -> liberaciones.liberarFondos(NUMERO, "lmartinez"));

        assertEquals("El contrato GAR-2026-000001 está FONDEADO: todavía no se puede liberar", e.getMessage());
        assertEquals(EstadoContrato.FONDEADO, contrato.getEstado());
        verifyNoInteractions(liquidaciones, detalles);
    }

    @Test
    void r6SoloElCompradorLibera() {
        when(contratos.findByNumeroContrato(NUMERO)).thenReturn(Optional.of(contrato));
        when(usuarios.findByNombreUsuario("mlopez")).thenReturn(Optional.of(usuarioVendedor));

        UsuarioNoAutorizadoException e = assertThrows(UsuarioNoAutorizadoException.class,
                () -> liberaciones.liberarFondos(NUMERO, "mlopez"));

        assertEquals("Solo el comprador puede liberar los fondos", e.getMessage());
        assertEquals(EstadoContrato.ENVIADO, contrato.getEstado());
        verifyNoInteractions(liquidaciones, detalles);
    }

    @Test
    void r7NoSePagaAUnaCuentaInactiva() {
        cuentaPago.setActivo(false);
        when(contratos.findByNumeroContrato(NUMERO)).thenReturn(Optional.of(contrato));
        when(usuarios.findByNombreUsuario("lmartinez")).thenReturn(Optional.of(usuarioComprador));

        CuentaInvalidaException e = assertThrows(CuentaInvalidaException.class,
                () -> liberaciones.liberarFondos(NUMERO, "lmartinez"));

        assertEquals("La cuenta no pertenece al vendedor o está inactiva", e.getMessage());
        verifyNoInteractions(liquidaciones, detalles);
    }

    @Test
    void r9NoSeLiquidaDosVeces() {
        when(contratos.findByNumeroContrato(NUMERO)).thenReturn(Optional.of(contrato));
        when(usuarios.findByNombreUsuario("lmartinez")).thenReturn(Optional.of(usuarioComprador));
        when(liquidaciones.existsByContratoId(1L)).thenReturn(true);

        EstadoInvalidoException e = assertThrows(EstadoInvalidoException.class,
                () -> liberaciones.liberarFondos(NUMERO, "lmartinez"));

        assertEquals("El contrato GAR-2026-000001 ya fue liquidado", e.getMessage());
        verify(liquidaciones, never()).save(any(Liquidacion.class));
        verifyNoInteractions(detalles);
    }
}