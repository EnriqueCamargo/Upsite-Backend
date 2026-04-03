package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Enums.Rol;

public record UsuarioRequestDTO(
        @NotBlank(message = "El Google ID es obligatorio para OAuth2")
        String googleID,

        @Email(message = "El formato del email no es válido")
        @NotBlank(message = "El email no puede estar vacío")
        String email,

        @NotBlank(message = "La matrícula es obligatoria para alumnos de la UPSIN")
        String matricula,

        @NotBlank(message = "El nombre es obligatorio")
        String nombres,

        @NotBlank(message = "El apellido es obligatorio")
        String apellidos,

        String fotoPerfil,

        @NotNull(message = "Debe asignar un rol (ESTUDIANTE/PROFESOR)")
        Rol rol,

        @NotBlank(message = "El grupo es obligatorio para la segmentación")
        String grupo,

        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}
