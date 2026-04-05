package mx.edu.upsite.demo.DTOs.Response;

import java.time.OffsetDateTime;

public record LoginResponseDTO(
        String token,
        UsuarioResponseDTO usuario
) {
}
