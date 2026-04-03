package mx.edu.upsite.demo.DTOs.Request;

public record NotificacionRequestDTO(
        String destinatarioEmail,
        String asunto,
        String tituloCuerpo,
        String mensajeCuerpo,
        String urlAccion,
        String tipo
) {
}
