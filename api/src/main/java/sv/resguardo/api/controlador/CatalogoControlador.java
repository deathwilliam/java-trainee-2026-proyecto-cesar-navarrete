package sv.resguardo.api.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sv.resguardo.api.dto.BancoRespuesta;
import sv.resguardo.api.dto.CategoriaRespuesta;
import sv.resguardo.api.servicio.CatalogoServicio;

import java.util.List;

@Tag(name = "Catalogos", description = "Categorias con sus limites y comision, y bancos aceptados")
@RestController
@RequestMapping("/api")
public class CatalogoControlador {

    private final CatalogoServicio catalogos;

    public CatalogoControlador(CatalogoServicio catalogos) {
        this.catalogos = catalogos;
    }

    @Operation(summary = "Categorias activas")
    @GetMapping("/categorias")
    public List<CategoriaRespuesta> categorias() {
        return catalogos.categorias();
    }

    @Operation(summary = "Bancos activos")
    @GetMapping("/bancos")
    public List<BancoRespuesta> bancos() {
        return catalogos.bancos();
    }
}