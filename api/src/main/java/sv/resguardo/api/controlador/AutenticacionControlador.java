package sv.resguardo.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.resguardo.api.dto.LoginSolicitud;
import sv.resguardo.api.dto.TokenRespuesta;
import sv.resguardo.api.servicio.AutenticacionServicio;

import java.util.Map;

@Tag(name = "Autenticacion", description = "Pedir un token y ver de quien es")
@RestController
@RequestMapping("/api/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacion;

    public AutenticacionControlador(AutenticacionServicio autenticacion) {
        this.autenticacion = autenticacion;
    }

    @Operation(summary = "Entrar: usuario y clave a cambio de un token",
            description = "200 con el token; 401 si el usuario o la clave estan mal")
    @PostMapping("/login")
    public TokenRespuesta login(@Valid @RequestBody LoginSolicitud solicitud) {
        return autenticacion.entrar(solicitud.usuario(), solicitud.clave());
    }

    @Operation(summary = "Quien soy: los datos del token con el que llamo")
    @GetMapping("/yo")
    public Map<String, Object> yo(@AuthenticationPrincipal Jwt token) {
        return Map.of(
                "usuario", token.getSubject(),
                "roles", token.getClaimAsStringList("roles"),
                "venceEn", token.getExpiresAt().toString());
    }
}