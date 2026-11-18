package sv.resguardo.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import sv.resguardo.api.dto.*;
import sv.resguardo.api.servicio.ContratoServicio;
import sv.resguardo.api.servicio.LiberacionServicio;

import java.util.List;

@Tag(name = "Contratos", description = "Contratos de garantia y la liberacion de sus fondos")
@RestController
@RequestMapping("/api/contratos")
public class ContratoControlador {

    private final ContratoServicio contratos;
    private final LiberacionServicio liberaciones;

    public ContratoControlador(ContratoServicio contratos, LiberacionServicio liberaciones) {
        this.contratos = contratos;
        this.liberaciones = liberaciones;
    }

    @Operation(summary = "Todos los contratos")
    @GetMapping
    public List<ContratoRespuesta> listar() {
        return contratos.listar();
    }

    @Operation(summary = "Un contrato por su numero", description = "200 si existe; 404 si no")
    @GetMapping("/{numero}")
    public ContratoRespuesta porNumero(@PathVariable String numero) {
        return contratos.porNumero(numero);
    }

    @Operation(summary = "Crear un contrato",
            description = "201 si se creo; 400 si faltan datos o una regla lo rechaza (R1, R7, misma persona). El vendedor sale del token")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping
    public ResponseEntity<ContratoRespuesta> crear(@Valid @RequestBody ContratoSolicitud solicitud,
                                                   @AuthenticationPrincipal Jwt token) {
        ContratoRespuesta respuesta = contratos.crear(solicitud, token.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Operation(summary = "Editar la descripcion de un contrato",
            description = "200 si se edito; 400 si no esta CREADO o faltan datos; 404 si no existe")
    @PreAuthorize("hasRole('CLIENTE')")
    @PatchMapping("/{numero}")
    public ContratoRespuesta editar(@PathVariable String numero,
                                    @Valid @RequestBody ContratoEdicion edicion) {
        return contratos.editar(numero, edicion);
    }

    @Operation(summary = "Cancelar un contrato",
            description = "204 si se cancelo; 409 si ya no esta CREADO; 404 si no existe")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMIN')")
    @DeleteMapping("/{numero}")
    public ResponseEntity<Void> cancelar(@PathVariable String numero) {
        contratos.cancelar(numero);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Liberar los fondos de un contrato",
            description = "201 si se liquido; 400 si una regla lo rechaza; 404 si el contrato no existe. El comprador sale del token")
    @PreAuthorize("hasRole('CLIENTE')")
    @PostMapping("/{numero}/liberacion")
    public ResponseEntity<LiquidacionRespuesta> liberar(@PathVariable String numero,
                                                        @AuthenticationPrincipal Jwt token) {
        LiquidacionRespuesta respuesta = liberaciones.liberarFondos(numero, token.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @Operation(summary = "La liquidacion de un contrato",
            description = "200 con el comprobante; 404 si el contrato no existe o todavia no se liquido")
    @GetMapping("/{numero}/liquidacion")
    public LiquidacionRespuesta liquidacion(@PathVariable String numero) {
        return liberaciones.consultar(numero);
    }

}