package mx.edu.upsite.demo.Services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.Security.JwtTokenProvider;
import mx.edu.upsite.demo.DTOs.Response.LoginResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Rol;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final UsuarioRepository usuarioRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO loginConGoogle(String idTokenString) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(idTokenString);
        } catch (Exception e) {
            throw new BadRequestException("Token de Google inválido");
        }

        if (idToken == null) {
            throw new BadRequestException("Token de Google inválido");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        // Validar que sea correo institucional de la UPSIN
        if (!email.endsWith("@upsin.edu.mx")) {
            throw new BadRequestException("Solo se permiten correos institucionales de upsin.edu.mx");
        }

        String googleId = payload.getSubject();
        String nombres = (String) payload.get("given_name");
        String apellidos = (String) payload.get("family_name");
        String fotoPerfil = (String) payload.get("picture");

        String apellidoFormato=apellidos.toLowerCase().trim();

        if(apellidoFormato.contains(" ")) apellidoFormato=apellidoFormato.split(" ")[0];

        String formatoDocente = nombres.toLowerCase().substring(0,1)+apellidoFormato;

        // Intentar buscar usuario por email, si no existe crearlo
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Generar matrícula a partir del correo (ej: 2021030612)
                    String matricula = email.split("@")[0];

                    if(matricula.equals(formatoDocente)){
                        return Usuario.builder()
                                .email(email)
                                .googleId(googleId)
                                .nombres(nombres)
                                .apellidos(apellidos)
                                .fotoPerfil(fotoPerfil)
                                .matricula(matricula)
                                .rol(Rol.DOCENTE)
                                .status(1)
                                .siguiendo(new java.util.ArrayList<>())
                                .seguidores(new java.util.ArrayList<>())
                                .build();
                    }else{
                        return Usuario.builder()
                                .email(email)
                                .googleId(googleId)
                                .nombres(nombres)
                                .apellidos(apellidos)
                                .fotoPerfil(fotoPerfil)
                                .matricula(matricula)
                                .rol(Rol.ESTUDIANTE)
                                .status(1)
                                .siguiendo(new java.util.ArrayList<>())
                                .seguidores(new java.util.ArrayList<>())
                                .build();
                    }
                });

        // Actualizar datos de Google por si cambiaron
        usuario.setGoogleId(googleId);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setFotoPerfil(fotoPerfil);


        // >>> AÑADIDO: Actualizar fecha de último acceso en cada login <<<
        usuario.setUltimoAcceso(OffsetDateTime.now());

        usuario = usuarioRepository.save(usuario);
        String token=jwtTokenProvider.generateToken(usuario);
        UsuarioResponseDTO usuarioResponseDTO=new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getGrupo() != null ? usuario.getGrupo().getNombre() : null,
                usuario.getGrupo() != null && usuario.getGrupo().getCarrera() != null
                        ? usuario.getGrupo().getCarrera().getNombre() : null,
                usuario.getFotoPerfil(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getMatricula(),
                null, // seguidoresCount
                null, // siguiendoCount
                null, // loSigo
                usuario.getCarrera() != null ? usuario.getCarrera().getId() : 
                    (usuario.getGrupo() != null && usuario.getGrupo().getCarrera() != null ? usuario.getGrupo().getCarrera().getId() : null),
                usuario.getGrupo() != null ? usuario.getGrupo().getId() : null,
                usuario.getStatus()
        );
        return new LoginResponseDTO(token, usuarioResponseDTO);
    }
}
