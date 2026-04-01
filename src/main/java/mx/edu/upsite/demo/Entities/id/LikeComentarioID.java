package mx.edu.upsite.demo.Entities.id;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // CRÍTICO: JPA lo usa para comparar llaves en memoria
public class LikeComentarioID implements Serializable {

    private Integer idComentario;
    private Integer idUsuario;
}