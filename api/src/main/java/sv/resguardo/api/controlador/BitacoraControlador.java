package sv.resguardo.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.resguardo.api.dto.BitacoraRespuesta;
import sv.resguardo.api.servicio.BitacoraServicio;

import java.util.List;

@Tag(name = "Bitacora", description = "Quien hizo que: solo ASESOR y ADMIN")
@RestController
@RequestMapping("/api/bitacora")
public class BitacoraControlador {

    private final BitacoraServicio bitacora;

    public BitacoraControlador(BitacoraServicio bitacora) {
        this.bitacora = bitacora;
    }

    @Operation(summary = "Las 20 ultimas operaciones", description = "200 para ASESOR y ADMIN; 403 para CLIENTE")
    @PreAuthorize("hasAnyRole('ASESOR', 'ADMIN')")
    @GetMapping
    public List<BitacoraRespuesta> ultimas() {
        return bitacora.ultimas();
    }
}