package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.*;
import mx.edu.upsite.demo.Entities.id.ProfesorAlcanceID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "profesor_alcance")
public class ProfesorAlcance {

    @EmbeddedId
    private ProfesorAlcanceID id = new ProfesorAlcanceID();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idCarrera")
    @JoinColumn(name = "id_carrera")
    private Carrera carrera;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idGrupo")
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;
}