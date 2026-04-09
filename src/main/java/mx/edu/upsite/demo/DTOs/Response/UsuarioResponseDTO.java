package mx.edu.upsite.demo.DTOs.Response;

import mx.edu.upsite.demo.Enums.Rol;

public record UsuarioResponseDTO(
        Integer id,
        String nombres,
        String apellidos,
        String grupo,
        String carrera,
        String fotoPerfil,
        String email,
        Rol rol,
        String matricula,
        Long seguidoresCount,
        Long siguiendoCount,
        Boolean loSigo,
        Integer idCarrera, // Nuevo campo
        Integer idGrupo     // También id de grupo para facilitar filtrado
) {
}
