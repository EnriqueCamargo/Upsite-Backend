package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.TipoMultimedia;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "multimedia_publicaciones")
public class MultimediaPublicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_media")
    private Integer id;

    @Column(name = "ruta", nullable = false, columnDefinition = "TEXT")
    private String ruta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoMultimedia tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;
}
