package sv.resguardo.api.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import sv.resguardo.api.dto.BitacoraRespuesta;
import sv.resguardo.api.entidad.BitacoraOperacion;
import sv.resguardo.api.entidad.ContratoGarantia;
import sv.resguardo.api.entidad.Usuario;
import sv.resguardo.api.entidad.enums.ResultadoOperacion;
import sv.resguardo.api.entidad.enums.TipoOperacion;
import sv.resguardo.api.repositorio.BitacoraOperacionRepositorio;
import sv.resguardo.api.repositorio.ContratoGarantiaRepositorio;
import sv.resguardo.api.repositorio.UsuarioRepositorio;

import java.util.List;
import java.util.Optional;

@Service
public class BitacoraServicio {

    private final BitacoraOperacionRepositorio bitacora;
    private final UsuarioRepositorio usuarios;
    private final ContratoGarantiaRepositorio contratos;

    public BitacoraServicio(BitacoraOperacionRepositorio bitacora, UsuarioRepositorio usuarios,
                            ContratoGarantiaRepositorio contratos) {
        this.bitacora = bitacora;
        this.usuarios = usuarios;
        this.contratos = contratos;
    }

    // REQUIRED
    @Transactional
    public void registrarExitosa(Usuario usuario, ContratoGarantia contrato, TipoOperacion operacion, String detalle) {
        bitacora.save(new BitacoraOperacion(usuario, contrato, operacion,
                ResultadoOperacion.EXITOSA, recortar(detalle)));
    }

    // REQUIRES_NEW
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarRechazada(String nombreUsuario, String numeroContrato, TipoOperacion operacion, String detalle) {
        Optional<Usuario> usuario = usuarios.findByNombreUsuario(nombreUsuario);
        if (usuario.isEmpty()) {
            return;
        }
        ContratoGarantia contrato = contratos.findByNumeroContrato(numeroContrato).orElse(null);
        bitacora.save(new BitacoraOperacion(usuario.get(), contrato, operacion,
                ResultadoOperacion.RECHAZADA, recortar(detalle)));
    }

    @Transactional(readOnly = true)
    public List<BitacoraRespuesta> ultimas() {
        return bitacora.findTop20ByOrderByIdDesc().stream()
                .map(BitacoraRespuesta::de)
                .toList();
    }

    private String recortar(String detalle) {
        if (detalle == null) {
            return null;
        }
        return detalle.length() > 255 ? detalle.substring(0, 255) : detalle;
    }
}