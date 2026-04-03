package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LikeComentarioRequestDTO(
        @NotNull(message = "el id del usuario es obligatorio")
        Integer idUsuario,
        @NotNull(message = "el id del comentario es obligatorio")
        Integer idComentario
) {
}
