package mx.edu.upsite.demo.DTOs.Response;

import mx.edu.upsite.demo.Enums.TipoMultimedia;

public record MultimediaPublicacionResponseDTO(
        Integer id,
        String ruta,
        TipoMultimedia tipo
) {
}
