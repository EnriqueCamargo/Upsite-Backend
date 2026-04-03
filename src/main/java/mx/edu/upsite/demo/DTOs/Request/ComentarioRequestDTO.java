package mx.edu.upsite.demo.DTOs.Request;

public record ComentarioRequestDTO(
        String texto,
        Integer idUsuario,
        Integer idPublicacion,
        Integer idPadre // null en caso de ser el primero comentario y no una respuesta a otro comentario
) {
}
