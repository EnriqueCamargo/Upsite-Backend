package mx.edu.upsite.demo.DTOs;

import java.time.OffsetDateTime;

public record LikeComentarioDTO(
        Integer idComentario,
        Integer idUsuario,
        OffsetDateTime fechaLike
) {
}
