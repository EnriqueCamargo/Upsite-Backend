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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private final NotificacionRepository repository;
    private final EmailService emailService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void crearNotificacion(Usuario receptor, Usuario emisor, TipoNotificacion tipo, String mensaje, Publicacion pub, boolean enviarEmail) {
        // 1. Guardar en Base de Datos
        Notificacion noti = new Notificacion();
        noti.setUsuario(receptor);
        noti.setEmisor(emisor);
        noti.setTipoNotificacion(tipo);
        noti.setPublicacion(pub);
        noti.setLeido(false);

        Notificacion guardada = repository.save(noti);

        // 2. Si aplica, enviar el correo institucional
        if (enviarEmail) {
            NotificacionRequestDTO mailDto = new NotificacionRequestDTO(
                    receptor.getEmail(),
                    "Nueva notificación en UPSITE",
                    "¡Hola, " + receptor.getNombres() + "!",
                    mensaje,
                    "https://upsite-upsin.netlify.app/publicacion/" + pub.getId(), // Tu URL de producción
                    tipo
            );
            emailService.enviarNotificacion(mailDto);

            guardada.setEnviadoEmail(true);
            repository.save(guardada);
        }
    }

    public List<NotificacionResponseDTO> listarNotificaciones(Integer usuarioId) {
        return repository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream()
                .map(this::toDTO)
                .toList();
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