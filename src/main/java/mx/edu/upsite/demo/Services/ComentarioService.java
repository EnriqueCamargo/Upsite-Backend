package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.ComentarioDTO;
import mx.edu.upsite.demo.Entities.Comentario;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.ComentarioRespository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRespository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;

    public List<ComentarioDTO> getAll() {
        return comentarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ComentarioDTO getById(Integer id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        return toDTO(comentario);
    }

    public ComentarioDTO create(ComentarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Publicacion publicacion = publicacionRepository.findById(dto.idPublicacion())
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));

        Comentario padre = dto.idPadre() != null
                ? comentarioRepository.findById(dto.idPadre())
                .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"))
                : null;

        Comentario comentario = new Comentario();
        comentario.setTexto(dto.texto());
        comentario.setUsuario(usuario);
        comentario.setPublicacion(publicacion);
        comentario.setPadre(padre);
        comentario.setStatus(1);

        return toDTO(comentarioRepository.save(comentario));
    }

    public ComentarioDTO update(Integer id, ComentarioDTO dto) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        comentario.setTexto(dto.texto());
        return toDTO(comentarioRepository.save(comentario));
    }

    public void delete(Integer id) {
        Comentario comentario = comentarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
        comentario.setStatus(0);
        comentarioRepository.save(comentario);
    }

    private ComentarioDTO toDTO(Comentario c) {
        return new ComentarioDTO(
                c.getId(),
                c.getTexto(),
                c.getFechaComentario(),
                c.getStatus(),
                c.getUsuario() != null ? c.getUsuario().getId() : null,
                c.getPublicacion() != null ? c.getPublicacion().getId() : null,
                c.getPadre() != null ? c.getPadre().getId() : null
        );
    }
}