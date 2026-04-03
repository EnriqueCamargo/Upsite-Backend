package mx.edu.upsite.demo.DTOs.Request;

import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;

import java.util.List;

public record PublicacionRequestDTO(
        Integer usuarioID,
        String texto,
        Importancia importancia,
        Moderacion moderacion,
        String feedbackIA,
        List<Integer> idsGrupos,
        List<MultimediaPublicacionRequestDTO> multimedia,
        boolean esGlobal
) {
}
