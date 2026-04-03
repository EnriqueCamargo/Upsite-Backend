package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotNull;

public record ProfesorAlcanceRequestDTO(
        @NotNull(message = "El id del usuario es obligatorio")
        Integer idUsuario,
        @NotNull(message = "el id de la carrera es obligatorio")
        Integer IdCarrera,

        Integer idGrupo
) {
}
