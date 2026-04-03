package mx.edu.upsite.demo.DTOs.Response;

import java.time.OffsetDateTime;

public record NotificacionResponseDTO(

        Integer id,
        String mensaje,      // Ejemplo: "Enrique Camargo te empezó a seguir"
        String emisorNombre,
        String emisorFoto,
        String tipo,         // El Enum convertido a String
        Integer refId,       // ID de la publicación o comentario para hacer clic e ir ahí
        Boolean leido,
        OffsetDateTime fecha

) {
}
