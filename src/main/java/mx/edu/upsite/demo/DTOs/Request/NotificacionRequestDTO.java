package mx.edu.upsite.demo.DTOs.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.edu.upsite.demo.Enums.TipoNotificacion;

public record NotificacionRequestDTO(
        @Email(message = "el formato del email no es valido")
        @NotBlank(message = "el email es obligatorio")
        String destinatarioEmail,
        @NotBlank(message = "el asunto es obligatorio")
        String asunto,
        @NotBlank(message = "el titulo del correo es obligatorio")
        String tituloCuerpo,
        @NotBlank(message = "el cuerpo del mensaje es obligatorio")
        String mensajeCuerpo,
        String urlAccion,
        @NotNull(message = "el tipo de notificacion es obligatoria")
        TipoNotificacion tipoNotificacion
) {
}
