package sv.resguardo.api.servicio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.resguardo.api.dto.TokenRespuesta;
import sv.resguardo.api.entidad.Usuario;
import sv.resguardo.api.excepcion.CredencialesInvalidasException;
import sv.resguardo.api.repositorio.UsuarioRepositorio;
import sv.resguardo.api.repositorio.UsuarioRolRepositorio;

import java.time.Instant;
import java.util.List;

@Service
public class AutenticacionServicio {

    private static final long DURACION_SEGUNDOS = 3600;

    private final UsuarioRepositorio usuarios;
    private final UsuarioRolRepositorio usuarioRoles;
    private final PasswordEncoder bcrypt;
    private final JwtEncoder jwt;

    public AutenticacionServicio(UsuarioRepositorio usuarios, UsuarioRolRepositorio usuarioRoles,
                                 PasswordEncoder bcrypt, JwtEncoder jwt) {
        this.usuarios = usuarios;
        this.usuarioRoles = usuarioRoles;
        this.bcrypt = bcrypt;
        this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public TokenRespuesta entrar(String nombreUsuario, String clave) {
        Usuario usuario = usuarios.findByNombreUsuario(nombreUsuario)
                .filter(Usuario::isActivo)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!bcrypt.matches(clave, usuario.getClaveHash())) {
            throw new CredencialesInvalidasException();
        }

        List<String> roles = usuarioRoles.findByUsuarioId(usuario.getId()).stream()
                .map(ur -> ur.getRol().getNombre())
                .toList();

        Instant ahora = Instant.now();
        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer("resguardo")
                .subject(usuario.getNombreUsuario())
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(DURACION_SEGUNDOS))
                .claim("roles", roles)
                .build();
        JwsHeader encabezado = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwt.encode(JwtEncoderParameters.from(encabezado, datos)).getTokenValue();
        return new TokenRespuesta(token, "Bearer", DURACION_SEGUNDOS);
    }
}