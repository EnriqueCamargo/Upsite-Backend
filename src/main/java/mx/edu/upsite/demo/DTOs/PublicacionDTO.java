package mx.edu.upsite.demo.DTOs;

import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;

import java.time.OffsetDateTime;

public record PublicacionDTO(
        Integer idPublicacion,
        Integer idUsuario,
        String texto,
        Importancia importancia,
        Moderacion moderacion,
        String feedbackIa,
        Integer targetCarrera,
        Integer targetGrupo,
        Boolean esGlobal,
        Integer status,
        OffsetDateTime fechaPublicacion,
        OffsetDateTime fechaEliminacion
){}
