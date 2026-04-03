package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @Email(message = "el formato del email es invalido")
        @NotBlank(message = "el email es obligatorio")
        String email,
        @Size(min = 8,message = "la contrasena debe contener minimo 8 caracteres")
        @NotBlank(message = "la contrasena es obligatoria")
        String password
) {
}
