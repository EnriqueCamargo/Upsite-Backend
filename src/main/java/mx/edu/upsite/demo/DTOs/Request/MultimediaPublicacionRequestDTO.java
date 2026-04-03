package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.edu.upsite.demo.Enums.TipoMultimedia;

public record MultimediaPublicacionRequestDTO(
    @NotBlank(message = "la ruta Es obligatoria")
    String ruta,
    @NotNull(message = "el tipo de multimedia es obligatorio")
    TipoMultimedia tipoMultimedia,
    @NotNull(message = "el id de la publicacion es obligatorio")
    Integer idPublicacion
) {
}
