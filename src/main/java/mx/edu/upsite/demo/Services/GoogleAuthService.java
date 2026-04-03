package mx.edu.upsite.demo.Services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public String loginWithGoogle(String googleToken) throws Exception {
        // Verificar el token con Google
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(googleToken);
        if (idToken == null) {
            throw new RuntimeException("Token de Google inválido");
        }

        // Extraer datos del usuario desde el token de Google
        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String nombres = (String) payload.get("given_name");
        String apellidos = (String) payload.get("family_name");
        String fotoPerfil = (String) payload.get("picture");

        // Buscar o crear el usuario
        Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setGoogleId(googleId);
                    nuevo.setEmail(email);
                    nuevo.setNombres(nombres);
                    nuevo.setApellidos(apellidos);
                    nuevo.setFotoPerfil(fotoPerfil);
                    return usuarioRepository.save(nuevo);
                });

        // Generar y devolver JWT propio
        return jwtService.generateToken(usuario);
    }
}
