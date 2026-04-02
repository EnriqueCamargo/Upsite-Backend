package mx.edu.upsite.demo.DTOs;

import java.time.OffsetDateTime;

public record LikePublicacionDTO(
        Integer idPublicacion,
        Integer idUsuario,
        OffsetDateTime fechaLike
) {
}
