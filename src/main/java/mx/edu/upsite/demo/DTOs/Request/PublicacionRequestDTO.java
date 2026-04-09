package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;

import java.util.List;

public record PublicacionRequestDTO(
        @NotNull(message = "el id del usuario es obligatorio")
        Integer usuarioID,
        @Size(max = 2000 , message = "El texto es demasiado largo")
        String texto,
        @NotBlank(message = "El nivel de importancia es obligatorio")
        Importancia importancia,
        Moderacion moderacion,
        String feedbackIA,
        List<Integer> idsCarreras, // Cambiado a lista para múltiples carreras
        List<Integer> idsGrupos,
        List<MultimediaPublicacionRequestDTO> multimedia,
        @NotNull(message = "se debe definir si la publicacion es global o no")
        boolean esGlobal
) {
}
