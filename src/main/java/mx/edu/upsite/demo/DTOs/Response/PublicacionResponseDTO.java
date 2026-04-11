package mx.edu.upsite.demo.DTOs.Response;

import mx.edu.upsite.demo.Enums.Importancia;

import java.time.OffsetDateTime;
import java.util.List;

public record PublicacionResponseDTO(
        Integer id,
        String texto,
        Importancia importancia,
        List<MultimediaPublicacionResponseDTO> multimedia,
        UsuarioResponseDTO usuario,
        Long totalLikes,
        Long totalComentarios,
        Boolean meGusta,
        List<CarreraResponseDTO> targetCarreras,
        List<GrupoResponseDTO> targetGrupos,
        OffsetDateTime fechaPublicacion
) {
}
