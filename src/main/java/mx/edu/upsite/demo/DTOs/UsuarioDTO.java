package mx.edu.upsite.demo.DTOs;

import mx.edu.upsite.demo.Enums.Rol;

import java.time.OffsetDateTime;

public record UsuarioDTO(
        Integer idUsuario,
        String googleId,
        String email,
        String matricula,
        String nombres,
        String apellidos,
        String fotoPerfil,
        Rol rol,
        Integer idGrupo,
        Integer status,
        OffsetDateTime fechaCreacion,
        OffsetDateTime ultimoAcceso,
        String ultimoIp,
        OffsetDateTime fechaEliminacion
) {}
