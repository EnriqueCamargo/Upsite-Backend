package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GrupoRequestDTO (
        @NotBlank(message = "el nombre del grupo es obligatorio")
        String nombre,
        @NotNull(message = "el id de la carrera es obligatorio")
        Integer idCarrera
){
}
