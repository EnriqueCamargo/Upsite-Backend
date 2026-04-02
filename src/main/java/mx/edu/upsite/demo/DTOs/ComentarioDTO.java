package mx.edu.upsite.demo.DTOs;

import java.time.OffsetDateTime;

public record ComentarioDTO(
        Integer idComentario,
        String texto,
        OffsetDateTime fechaComentario,
        Integer status,
        Integer idUsuario,
        Integer idPublicacion,
        Integer idPadre
) {}
