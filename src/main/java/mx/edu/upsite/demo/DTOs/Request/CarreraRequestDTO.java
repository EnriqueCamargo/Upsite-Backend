package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.NotBlank;

public record CarreraRequestDTO(
        @NotBlank(message = "el nombre de la carrera es obligatorio")
        String nombre
) {
}
