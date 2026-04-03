package mx.edu.upsite.demo.Services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Rol;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

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



        //Separa las partes del cuerpo del email
        String cuerpoEmail=email.split("@")[0];
        String inicialEmail=cuerpoEmail.substring(0,1).toLowerCase();
        String restoEmail=cuerpoEmail.substring(1).toLowerCase();

        //formato de los nombres para el email
        String inicialNombre= nombres.substring(0,1).toLowerCase();
        String primerApellido = (apellidos != null && !apellidos.isEmpty())
                ? apellidos.split(" ")[0].toLowerCase()
                : null;



        // Buscar o crear el usuario
        Usuario usuario = usuarioRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setGoogleId(googleId);
                    nuevo.setEmail(email);
                    nuevo.setNombres(nombres);
                    nuevo.setApellidos(apellidos);
                    nuevo.setFotoPerfil(fotoPerfil);
                    if (cuerpoEmail.matches("\\d+")) nuevo.setRol(Rol.ESTUDIANTE);
                    else if(inicialEmail==inicialNombre && restoEmail==primerApellido) nuevo.setRol(Rol.DOCENTE);
                    return usuarioRepository.save(nuevo);
                });

        // Generar y devolver JWT propio
        return jwtService.generateToken(usuario);
    }
}
