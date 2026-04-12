package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.NotificacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.NotificacionResponseDTO;
import mx.edu.upsite.demo.Entities.Notificacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.TipoNotificacion;
import mx.edu.upsite.demo.Repositories.NotificacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.ArrayList;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void crearNotificacion(Usuario receptor, Usuario emisor, TipoNotificacion tipo, String mensaje, Publicacion pub, boolean enviarEmail) {
        crearNotificacionesBulk(List.of(receptor), emisor, tipo, mensaje, pub, enviarEmail);
    }

    @Transactional
    public void crearNotificacionesBulk(Collection<Usuario> receptores, Usuario emisor, TipoNotificacion tipo, String mensaje, Publicacion pub, boolean enviarEmail) {
        List<Notificacion> notificaciones = new ArrayList<>();
        for (Usuario receptor : receptores) {
            if (receptor.getId().equals(emisor.getId())) continue;

            Notificacion noti = new Notificacion();
            noti.setUsuario(receptor);
            noti.setEmisor(emisor);
            noti.setTipoNotificacion(tipo);
            noti.setPublicacion(pub);
            noti.setLeido(false);
            noti.setEnviadoEmail(enviarEmail);
            notificaciones.add(noti);
        }

        repository.saveAll(notificaciones);

        if (enviarEmail) {
            for (Usuario receptor : receptores) {
                if (receptor.getId().equals(emisor.getId())) continue;

                NotificacionRequestDTO mailDto = new NotificacionRequestDTO(
                        receptor.getEmail(),
                        "Nueva notificación en UPSITE",
                        "¡Hola, " + receptor.getNombres() + "!",
                        mensaje,
                        "https://upsite-upsin.netlify.app/publicacion/" + pub.getId(),
                        tipo
                );
                emailService.enviarNotificacion(mailDto);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarNotificaciones(Integer usuarioId, int page, int size) {
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId, PageRequest.of(page, size))
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<NotificacionResponseDTO> listarNotificaciones(Integer usuarioId) {
        return listarNotificaciones(usuarioId, 0, 50);
    }

    private NotificacionResponseDTO toDTO(Notificacion n) {
        return new NotificacionResponseDTO(
                n.getId(),
                "Nueva actividad de " + n.getEmisor().getNombres(),
                n.getEmisor().getNombres(),
                n.getEmisor().getFotoPerfil(), // Asegúrate de tener este campo
                n.getTipoNotificacion().name(),
                n.getPublicacion() != null ? n.getPublicacion().getId() : null,
                n.getLeido(),
                n.getFecha()
        );
    }
}