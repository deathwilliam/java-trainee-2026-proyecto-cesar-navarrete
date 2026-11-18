package sv.resguardo.api.servicio;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import sv.resguardo.api.dto.LiquidacionRespuesta;
import sv.resguardo.api.entidad.DetalleLiquidacion;
import sv.resguardo.api.entidad.enums.EstadoContrato;
import sv.resguardo.api.entidad.enums.ResultadoOperacion;
import sv.resguardo.api.entidad.enums.TipoOperacion;
import sv.resguardo.api.excepcion.CuentaInvalidaException;
import sv.resguardo.api.excepcion.EstadoInvalidoException;
import sv.resguardo.api.excepcion.UsuarioNoAutorizadoException;
import sv.resguardo.api.repositorio.BitacoraOperacionRepositorio;
import sv.resguardo.api.repositorio.ContratoGarantiaRepositorio;
import sv.resguardo.api.repositorio.DetalleLiquidacionRepositorio;
import sv.resguardo.api.repositorio.LiquidacionRepositorio;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockingDetails;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/resguardo_test",
        "spring.jpa.hibernate.ddl-auto=create",
        "jwt.clave=clave-solo-para-las-pruebas-de-resguardo-2026"
})
class LiberacionServicioTest {

    @Autowired
    private LiberacionServicio servicio;

    @Autowired
    private ContratoGarantiaRepositorio contratos;

    @Autowired
    private LiquidacionRepositorio liquidaciones;

    @Autowired
    private BitacoraOperacionRepositorio bitacora;

    @MockitoSpyBean
    private DetalleLiquidacionRepositorio detalles;

    @Test
    void siFallaALaMitadNoQuedaNada() {
        long liquidacionesAntes = liquidaciones.count();
        long detallesAntes = detalles.count();
        long rechazadasAntes = rechazadas();

        // El primer detalle (COMISION) se guarda de verdad; el segundo (PAGO_VENDEDOR) falla
        Answer<?> loDeSiempre = mockingDetails(detalles).getMockCreationSettings().getDefaultAnswer();
        doAnswer(loDeSiempre)
                .doThrow(new IllegalStateException("Falla simulada al grabar el PAGO_VENDEDOR"))
                .when(detalles).save(any(DetalleLiquidacion.class));

        assertThrows(IllegalStateException.class,
                () -> servicio.liberarFondos("GAR-2026-000005", "lmartinez"));

        assertEquals(EstadoContrato.ENVIADO, estado("GAR-2026-000005"), "el contrato cambio de estado");
        assertEquals(liquidacionesAntes, liquidaciones.count(), "quedo una liquidacion suelta");
        assertEquals(detallesAntes, detalles.count(), "quedo un detalle suelto");
        assertEquals(rechazadasAntes + 1, rechazadas(), "el rollback se llevo la RECHAZADA");
    }

    @Test
    void siUnaReglaDiceQueNoNoSeTocaNada() {
        long liquidacionesAntes = liquidaciones.count();
        long detallesAntes = detalles.count();
        long rechazadasAntes = rechazadas();

        // R4: el contrato 2 esta FONDEADO
        assertThrows(EstadoInvalidoException.class,
                () -> servicio.liberarFondos("GAR-2026-000002", "lmartinez"));
        // R6: Marta no es la compradora del contrato 5
        assertThrows(UsuarioNoAutorizadoException.class,
                () -> servicio.liberarFondos("GAR-2026-000005", "mlopez"));
        // R7: la cuenta de pago del contrato 4 esta inactiva
        assertThrows(CuentaInvalidaException.class,
                () -> servicio.liberarFondos("GAR-2026-000004", "arivas"));

        assertEquals(EstadoContrato.FONDEADO, estado("GAR-2026-000002"));
        assertEquals(liquidacionesAntes, liquidaciones.count());
        assertEquals(detallesAntes, detalles.count());
        assertEquals(rechazadasAntes + 3, rechazadas(), "no quedaron las tres RECHAZADAS");
    }

    @Test
    void siTodoSaleBienQuedanLasCuatroPartes() {
        long detallesAntes = detalles.count();
        long exitosasAntes = bitacora.countByOperacionAndResultado(
                TipoOperacion.LIBERAR_FONDOS, ResultadoOperacion.EXITOSA);

        LiquidacionRespuesta respuesta = servicio.liberarFondos("GAR-2026-000001", "lmartinez");

        // El ejemplo del README: 350.00 al 0.0300
        assertEquals("LIQ-2026-000001", respuesta.numeroComprobante());
        assertEquals(0, new BigDecimal("10.50").compareTo(respuesta.comision()));
        assertEquals(0, new BigDecimal("339.50").compareTo(respuesta.pagoVendedor()));
        assertEquals(0, new BigDecimal("350.00").compareTo(detalles.totalLiquidado("GAR-2026-000001")));

        assertEquals(EstadoContrato.LIBERADO, estado("GAR-2026-000001"));
        assertEquals(detallesAntes + 2, detalles.count(), "faltan detalles");
        assertEquals(exitosasAntes + 1, bitacora.countByOperacionAndResultado(
                TipoOperacion.LIBERAR_FONDOS, ResultadoOperacion.EXITOSA), "falta la bitacora");
    }

    private EstadoContrato estado(String numeroContrato) {
        return contratos.findByNumeroContrato(numeroContrato).orElseThrow().getEstado();
    }

    private long rechazadas() {
        return bitacora.countByOperacionAndResultado(TipoOperacion.LIBERAR_FONDOS, ResultadoOperacion.RECHAZADA);
    }
}