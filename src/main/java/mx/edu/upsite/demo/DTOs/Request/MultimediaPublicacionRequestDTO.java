package mx.edu.upsite.demo.DTOs.Request;

import mx.edu.upsite.demo.Enums.TipoMultimedia;

public record MultimediaPublicacionRequestDTO(
    Integer id,
    String ruta,
    TipoMultimedia tipoMultimedia,
    Integer idPublicacion
) {
}
