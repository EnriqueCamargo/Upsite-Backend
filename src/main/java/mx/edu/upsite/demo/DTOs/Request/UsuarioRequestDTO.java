package mx.edu.upsite.demo.DTOs.Request;

import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Enums.Rol;

public record UsuarioRequestDTO(
        String googleID,
        String email,
        String matricula,
        String nombres,
        String Apellidos,
        String fotoPerfil,
        Rol rol,
        String grupo,
        String password
) {
}
