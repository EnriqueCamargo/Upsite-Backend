package mx.edu.upsite.demo.Entities.id;

import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LikePublicacionID implements Serializable {

    private Integer idPublicacion;
    private Integer idUsuario;
}