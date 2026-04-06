package mx.edu.upsite.demo.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.ComentarioResponseDTO;
import mx.edu.upsite.demo.Entities.Comentario;
import mx.edu.upsite.demo.Entities.LikeComentario;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.ComentarioRepository;
import mx.edu.upsite.demo.Repositories.LikeComentarioRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikeComentarioRepository likeComentarioRepository;

    public List<ComentarioResponseDTO> getComentariosByPublicacion(Integer idPublicacion) {
        return comentarioRepository
                .findByPublicacionIdAndStatusAndPadreIsNull(idPublicacion, 1)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ComentarioResponseDTO crear(Integer idPublicacion, Integer idUsuario, String texto, Integer idPadre) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Comentario padre = idPadre != null
                ? comentarioRepository.findById(idPadre)
                .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"))
                : null;

        Comentario comentario = new Comentario();
        comentario.setTexto(texto);
        comentario.setUsuario(usuario);
        comentario.setPublicacion(publicacion);
        comentario.setPadre(padre);
        comentario.setStatus(1);

        return toDTO(comentarioRepository.save(comentario));
    }

    public void darLike(Integer idComentario, Integer idUsuario) {
        if (likeComentarioRepository.existsByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario)) {
            throw new RuntimeException("Ya diste like a este comentario");
        }

        Comentario comentario = comentarioRepository.findById(idComentario)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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
            throw new RuntimeException("No has dado like a este comentario");
        }
        likeComentarioRepository.deleteByIdIdComentarioAndIdIdUsuario(idComentario, idUsuario);
    }

    private ComentarioResponseDTO toDTO(Comentario c) {
        return new ComentarioResponseDTO(
                c.getId(),
                c.getTexto(),
                c.getFechaComentario(),
                c.getUsuario().getNombres() + " " + c.getUsuario().getApellidos(),
                c.getUsuario().getFotoPerfil(),
                c.getUsuario().getMatricula(),
                c.getPublicacion().getId(),
                c.getPadre() != null ? c.getPadre().getId() : null
        );
    }
}