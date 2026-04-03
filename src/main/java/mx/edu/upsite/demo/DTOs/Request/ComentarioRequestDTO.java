package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ComentarioRequestDTO(
        @NotBlank(message = "el texto es obligatorio")
        String texto,
        @NotNull(message = "el id del usuario es obligatorio")
        Integer idUsuario,
        @NotNull(message = "el id de la publicacion es obligatorio")
        Integer idPublicacion,
        Integer idPadre // null en caso de ser el primero comentario y no una respuesta a otro comentario
) {
}
