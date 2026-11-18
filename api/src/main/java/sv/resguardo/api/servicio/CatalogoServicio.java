package sv.resguardo.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.resguardo.api.dto.BancoRespuesta;
import sv.resguardo.api.dto.CategoriaRespuesta;
import sv.resguardo.api.entidad.Banco;
import sv.resguardo.api.entidad.Categoria;
import sv.resguardo.api.repositorio.BancoRepositorio;
import sv.resguardo.api.repositorio.CategoriaRepositorio;

import java.util.List;

@Service
public class CatalogoServicio {

    private final CategoriaRepositorio categorias;
    private final BancoRepositorio bancos;

    public CatalogoServicio(CategoriaRepositorio categorias, BancoRepositorio bancos) {
        this.categorias = categorias;
        this.bancos = bancos;
    }

    @Transactional(readOnly = true)
    public List<CategoriaRespuesta> categorias() {
        return categorias.findAll().stream()
                .filter(Categoria::isActivo)
                .map(CategoriaRespuesta::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BancoRespuesta> bancos() {
        return bancos.findAll().stream()
                .filter(Banco::isActivo)
                .map(BancoRespuesta::de)
                .toList();
    }
}