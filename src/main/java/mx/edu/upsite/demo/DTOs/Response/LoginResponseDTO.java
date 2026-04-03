package mx.edu.upsite.demo.DTOs.Response;

public record LoginResponseDTO(
        String token,
        UsuarioResponseDTO usuario
) {
}
