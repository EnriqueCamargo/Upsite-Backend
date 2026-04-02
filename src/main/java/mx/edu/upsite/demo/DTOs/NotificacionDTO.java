package mx.edu.upsite.demo.DTOs;

import java.time.OffsetDateTime;

public record NotificacionDTO(
        Integer idNotificacion,
        Integer idUsuario,
        Integer idEmisor,
        Integer refPublicacion,
        Integer refComentario,
        Boolean leido,
        Boolean enviadoEmail,
        OffsetDateTime fecha
) {}
