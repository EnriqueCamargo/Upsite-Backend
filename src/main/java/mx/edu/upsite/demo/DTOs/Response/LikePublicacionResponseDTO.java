package mx.edu.upsite.demo.DTOs.Response;

import java.time.OffsetDateTime;

public record LikePublicacionResponseDTO(
        Integer idPublicacion,
        String nombreUsuario,
        String fotoUsuario,
        String matricula,
        OffsetDateTime fecha
) {
}
