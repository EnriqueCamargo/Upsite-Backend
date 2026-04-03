package mx.edu.upsite.demo.DTOs.Response;

public record ProfesorAlcanceResponseDTO(
        Integer idUsuario,
        String nombreProfesor,
        Integer idCarrera,
        String nombreCarrera,
        Integer idGrupo,
        String nombreGrupo // Ej: "ITI-9-1"
) {}
