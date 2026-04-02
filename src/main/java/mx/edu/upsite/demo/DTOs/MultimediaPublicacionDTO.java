package mx.edu.upsite.demo.DTOs;

import mx.edu.upsite.demo.Enums.TipoMultimedia;

public record MultimediaPublicacionDTO(
        Integer idMedia,
        String ruta,
        TipoMultimedia tipo,
        Integer idPublicacion
) {}
