package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.Entities.LikePublicacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.LikePublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class LikePublicacionService {

    private final LikePublicacionRepository likePublicacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void darLike(Integer idPublicacion, Integer idUsuario) {
        // 1. Blindaje de Existencia y Status (Publicación)
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede dar like: Publicación no encontrada."));

        if (publicacion.getStatus() == 0) {
            throw new BadRequestException("No se puede dar like a una publicación eliminada.");
        }

        // 2. Blindaje de Usuario (Existencia y Status)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (usuario.getStatus() == 0) {
            throw new BadRequestException("Un usuario desactivado no puede realizar esta acción.");
        }

        // 3. Blindaje de Duplicidad
        if (likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario)) {
            throw new ConflictException("Ya has reaccionado a esta publicación.");
        }

        // 4. Creación Segura usando tu @EmbeddedId inicializado
        LikePublicacion like = new LikePublicacion();

        // Seteamos los valores dentro del ID que ya se inicializó en la entidad
        like.getId().setIdPublicacion(idPublicacion);
        like.getId().setIdUsuario(idUsuario);

        // Seteamos las relaciones (Importante para @MapsId)
        like.setPublicacion(publicacion);
        like.setUsuario(usuario);

        likePublicacionRepository.save(like);
    }

    @Transactional
    public void quitarLike(Integer idPublicacion, Integer idUsuario) {
        if (!likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario)) {
            throw new ResourceNotFoundException("No se encontró el like para eliminar.");
        }
        likePublicacionRepository.deleteByIdIdPublicacionAndIdIdUsuario(idPublicacion, idUsuario);
    }

    @Transactional(readOnly = true)
    public Long contarLikes(Integer idPublicacion) {
        if (!publicacionRepository.existsById(idPublicacion)) {
            return 0L;
        }
        return likePublicacionRepository.countByIdIdPublicacion(idPublicacion);
    }
}