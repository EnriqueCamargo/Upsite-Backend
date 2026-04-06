package mx.edu.upsite.demo.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.Entities.LikePublicacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.LikePublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LikePublicacionService {

    private final LikePublicacionRepository likePublicacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public void darLike(Integer idPublicacion, Integer idUsuario) {
        if (likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario)) {
            throw new RuntimeException("Ya diste like a esta publicacion");
        }

        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LikePublicacion like = new LikePublicacion();
        like.getId().setIdPublicacion(idPublicacion);
        like.getId().setIdUsuario(idUsuario);
        like.setPublicacion(publicacion);
        like.setUsuario(usuario);

        likePublicacionRepository.save(like);
    }

    @Transactional
    public void quitarLike(Integer idPublicacion, Integer idUsuario) {
        if (!likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario)) {
            throw new RuntimeException("No has dado like a esta publicacion");
        }
        likePublicacionRepository.deleteByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario);
    }

    public Long contarLikes(Integer idPublicacion) {
        return likePublicacionRepository.countByIdIdPublicacion(idPublicacion);
    }
}
