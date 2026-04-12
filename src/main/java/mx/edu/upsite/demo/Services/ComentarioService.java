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
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikeComentarioRepository likeComentarioRepository;
    private final ModerationService moderationService;

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> getComentariosByPublicacion(Integer idPublicacion, Integer idUsuario) {
        if (!publicacionRepository.existsById(idPublicacion)) {
            throw new ResourceNotFoundException("No se pueden obtener comentarios: Publicación no encontrada.");
        }

        List<Comentario> comentarios = comentarioRepository.findByPublicacionIdAndStatusAndPadreIsNull(idPublicacion, 1);
        return convertToDTOList(comentarios, idUsuario);
    }

    private List<ComentarioResponseDTO> convertToDTOList(List<Comentario> comentarios, Integer idUsuario) {
        if (comentarios.isEmpty()) return List.of();

        List<Integer> ids = comentarios.stream().map(Comentario::getId).toList();

        Map<Integer, Long> likesMap = likeComentarioRepository.countLikesForComentarios(ids)
                .stream().collect(Collectors.toMap(o -> (Integer) o[0], o -> (Long) o[1]));

        Map<Integer, Long> responsesMap = comentarioRepository.countByPadreIdsAndStatus(ids)
                .stream().collect(Collectors.toMap(o -> (Integer) o[0], o -> (Long) o[1]));

        Set<Integer> likedSet = (idUsuario != null)
                ? new HashSet<>(likeComentarioRepository.findLikedByUsuario(idUsuario, ids))
                : Set.of();

        return comentarios.stream()
                .map(c -> toDTO(c, idUsuario, likesMap, responsesMap, likedSet))
                .toList();
    }

    private ComentarioResponseDTO toDTO(Comentario c, Integer idUsuario, Map<Integer, Long> likesMap, Map<Integer, Long> responsesMap, Set<Integer> likedSet) {
        return new ComentarioResponseDTO(
                c.getId(),
                c.getTexto(),
                c.getFechaComentario(),
                c.getUsuario() != null ? c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos() : "Usuario Anónimo",
                c.getUsuario() != null ? c.getUsuario().getFotoPerfil() : null,
                c.getUsuario() != null ? c.getUsuario().getMatricula() : null,
                c.getPublicacion() != null ? c.getPublicacion().getId() : null,
                c.getPadre() != null ? c.getPadre().getId() : null,
                likesMap.getOrDefault(c.getId(), 0L),
                likedSet.contains(c.getId()),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                responsesMap.getOrDefault(c.getId(), 0L)
        );
    }

    @Transactional
    public ComentarioResponseDTO crear(Integer idPublicacion, Integer idUsuario, String texto, Integer idPadre) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede comentar: Publicación no encontrada."));

        if (publicacion.getStatus() == 0) {
            throw new BadRequestException("No se permiten nuevos comentarios en una publicación eliminada.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        if (texto == null || texto.trim().isEmpty()) {
            throw new BadRequestException("El comentario no puede estar vacío.");
        }

        if (!moderationService.esContenidoApropiado(texto.trim(), null)) {
            throw new BadRequestException("Tu comentario contiene contenido inapropiado y no puede ser publicado.");
        }

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
        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado."));

        if (comentario.getStatus() == 0) {
            throw new BadRequestException("No se puede dar like a un comentario eliminado.");
        }

        if (likeComentarioRepository.existsByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario)) {
            throw new ConflictException("Ya has reaccionado a este comentario.");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        LikeComentario like = new LikeComentario();
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

    @Transactional(readOnly = true)
    public List<ComentarioResponseDTO> getRespuestas(Integer idPadre, Integer idUsuario) {
        List<Comentario> respuestas = comentarioRepository.findByPadreIdAndStatus(idPadre, 1);
        return convertToDTOList(respuestas, idUsuario);
    }

    private ComentarioResponseDTO toDTO(Comentario c, Integer idUsuario) {
        return convertToDTOList(List.of(c), idUsuario).get(0);
    }
}
