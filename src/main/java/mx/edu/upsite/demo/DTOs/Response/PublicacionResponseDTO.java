package mx.edu.upsite.demo.DTOs.Response;

import jakarta.persistence.criteria.CriteriaBuilder;
import mx.edu.upsite.demo.Enums.Importancia;

import java.util.List;

public record PublicacionResponseDTO(
        Integer id,
        String texto,
        Importancia importancia,
        List<MultimediaPublicacionResponseDTO> multimedia,
        UsuarioResponseDTO usuario,
        Long totalLikes,
        Long totalComentarios,
        Boolean meGusta
) {
}
