package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotNull;

public record LikePublicacionRequestDTO(
        @NotNull(message = "el id del usuario es obligatorio")
        Integer idUsuario,
        @NotNull(message = "el id de la publicacion es obligatorio")
        Integer idPublicacion
) {
}
