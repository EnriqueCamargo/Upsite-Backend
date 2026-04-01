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
public class ProfesorAlcanceID implements Serializable {
    private Integer idUsuario;
    private Integer idCarrera;
    private Integer idGrupo;
}