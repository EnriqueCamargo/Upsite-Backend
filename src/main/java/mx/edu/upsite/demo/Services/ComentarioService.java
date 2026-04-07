package mx.edu.upsite.demo.Services;


import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.ComentarioResponseDTO;
import mx.edu.upsite.demo.Entities.Comentario;
import mx.edu.upsite.demo.Entities.LikeComentario;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.ComentarioRepository;
import mx.edu.upsite.demo.Repositories.LikeComentarioRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikeComentarioRepository likeComentarioRepository;

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> getComentariosByPublicacion(Integer idPublicacion, Integer idUsuario) {
        // 1. Blindaje de Existencia: No listamos comentarios de algo que no existe o está borrado
        if (!publicacionRepository.existsById(idPublicacion)) {
            throw new ResourceNotFoundException("No se pueden obtener comentarios: Publicación no encontrada.");
        }

        return comentarioRepository
                .findByPublicacionIdAndStatusAndPadreIsNull(idPublicacion, 1)
                .stream()
                .map(c -> toDTO(c, idUsuario))
                .toList();

    }

    @Transactional
    public ComentarioResponseDTO crear(Integer idPublicacion, Integer idUsuario, String texto, Integer idPadre) {
        // 2. Blindaje de Publicación y Estado
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede comentar: Publicación no encontrada."));

        if (publicacion.getStatus() == 0) {
            throw new BadRequestException("No se permiten nuevos comentarios en una publicación eliminada.");
        }

        // 3. Blindaje de Usuario
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        // 4. Blindaje de Texto
        if (texto == null || texto.trim().isEmpty()) {
            throw new BadRequestException("El comentario no puede estar vacío.");
        }

        // 5. Blindaje de Jerarquía (Hilos)
        Comentario padre = null;
        if (idPadre != null) {
            padre = comentarioRepository.findById(idPadre)
                    .orElseThrow(() -> new ResourceNotFoundException("El comentario al que intentas responder no existe."));

            if (padre.getStatus() == 0) {
                throw new BadRequestException("No puedes responder a un comentario eliminado.");
            }
        }

        Comentario comentario = new Comentario();
        comentario.setTexto(texto.trim());
        comentario.setUsuario(usuario);
        comentario.setPublicacion(publicacion);
        comentario.setPadre(padre);
        comentario.setStatus(1);

        return toDTO(comentarioRepository.save(comentario), idUsuario);
    }

    @Transactional
    public void darLike(Integer idComentario, Integer idUsuario) {
        // 6. Blindaje de Comentario: Existencia y Status
        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado."));

        if (comentario.getStatus() == 0) {
            throw new BadRequestException("No se puede dar like a un comentario eliminado.");
        }

        // 7. Blindaje de Duplicidad
        if (likeComentarioRepository.existsByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario)) {
            throw new ConflictException("Ya has reaccionado a este comentario.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        // 8. Creación segura de la relación de Like
        LikeComentario like = new LikeComentario();
        // Inicializamos el ID embebido si no lo hace la entidad por defecto
        like.getId().setIdComentario(idComentario);
        like.getId().setIdUsuario(idUsuario);

        like.setComentario(comentario);
        like.setUsuario(usuario);

        likeComentarioRepository.save(like);
    }

    @Transactional
    public void quitarLike(Integer idComentario, Integer idUsuario) {
        if (!likeComentarioRepository.existsByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario)) {
            throw new ResourceNotFoundException("No se encontró la reacción para eliminar.");
        }
        likeComentarioRepository.deleteByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario);
    }

    private ComentarioResponseDTO toDTO(Comentario c, Integer idUsuario) {
        Long totalLikes = likeComentarioRepository.countByIdIdComentario(c.getId());
        Boolean meGusta = likeComentarioRepository.existsByIdIdComentarioAndIdIdUsuario(c.getId(), idUsuario);

        // Blindaje de Nulos en el mapeo
        return new ComentarioResponseDTO(
                c.getId(),
                c.getTexto(),
                c.getFechaComentario(),
                c.getUsuario() != null ? c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos() : "Usuario Anónimo",
                c.getUsuario() != null ? c.getUsuario().getFotoPerfil() : null,
                c.getUsuario() != null ? c.getUsuario().getMatricula() : null,
                c.getPublicacion() != null ? c.getPublicacion().getId() : null,
                c.getPadre() != null ? c.getPadre().getId() : null,
                totalLikes,
                meGusta
        );
    }
}