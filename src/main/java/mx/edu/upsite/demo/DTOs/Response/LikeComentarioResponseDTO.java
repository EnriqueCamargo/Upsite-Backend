package mx.edu.upsite.demo.DTOs.Response;

import java.time.OffsetDateTime;

public record LikeComentarioResponseDTO(
        String nombreUsuario,
        String matricula,
        String fotoUsuario,
        Integer idComentario,
        OffsetDateTime fecha
) {
}
