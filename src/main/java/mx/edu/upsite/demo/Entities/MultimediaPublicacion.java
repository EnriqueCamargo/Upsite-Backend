package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.TipoMultimedia;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
    @Column(name = "tipo", columnDefinition = "enum_multimedia")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private TipoMultimedia tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;
}
