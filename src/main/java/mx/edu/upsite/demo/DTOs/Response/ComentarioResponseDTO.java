package mx.edu.upsite.demo.DTOs.Response;

import java.time.OffsetDateTime;

public record ComentarioResponseDTO (
        Integer id,
        String texto,
        OffsetDateTime fecha,
        String autorNombre,
        String autorFoto,
        String matricula,
        Integer idPublicacion,
        Integer idPadre, //null en caso de ser el primer comentario y no una respuesta a otro
        Long totalLikes,   // ← nuevo
        Boolean meGusta    // ← nuevo
){
}
