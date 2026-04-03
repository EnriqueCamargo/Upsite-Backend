package mx.edu.upsite.demo.DTOs.Request;

import org.antlr.v4.runtime.misc.NotNull;

public record ProfesorAlcanceRequestDTO(
        Integer idUsuario,
        Integer IdCarrera,
        Integer idGrupo
) {
}
