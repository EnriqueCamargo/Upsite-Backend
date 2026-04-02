package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.NotificacionDTO;
import mx.edu.upsite.demo.Entities.Comentario;
import mx.edu.upsite.demo.Entities.Notificacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.ComentarioRespository;
import mx.edu.upsite.demo.Repositories.NotificacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PublicacionRepository publicacionRepository;
    private final ComentarioRespository comentarioRepository;

    public List<NotificacionDTO> getAll() {
        return notificacionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public NotificacionDTO getById(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
        return toDTO(notificacion);
    }

    public NotificacionDTO create(NotificacionDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Usuario emisor = usuarioRepository.findById(dto.idEmisor())
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        Publicacion publicacion = dto.refPublicacion() != null
                ? publicacionRepository.findById(dto.refPublicacion())
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"))
                : null;

        Comentario comentario = dto.refComentario() != null
                ? comentarioRepository.findById(dto.refComentario())
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"))
                : null;

        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setEmisor(emisor);
        notificacion.setPublicacion(publicacion);
        notificacion.setComentario(comentario);
        notificacion.setLeido(false);
        notificacion.setEnviadoEmail(false);

        return toDTO(notificacionRepository.save(notificacion));
    }

    public NotificacionDTO update(Integer id, NotificacionDTO dto) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
        notificacion.setLeido(dto.leido());
        notificacion.setEnviadoEmail(dto.enviadoEmail());
        return toDTO(notificacionRepository.save(notificacion));
    }

    public void delete(Integer id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificacion no encontrada"));
        notificacionRepository.delete(notificacion);
    }

    private NotificacionDTO toDTO(Notificacion n) {
        return new NotificacionDTO(
                n.getId(),
                n.getUsuario() != null ? n.getUsuario().getId() : null,
                n.getEmisor() != null ? n.getEmisor().getId() : null,
                n.getPublicacion() != null ? n.getPublicacion().getId() : null,
                n.getComentario() != null ? n.getComentario().getId() : null,
                n.getLeido(),
                n.getEnviadoEmail(),
                n.getFecha()
        );
    }
}