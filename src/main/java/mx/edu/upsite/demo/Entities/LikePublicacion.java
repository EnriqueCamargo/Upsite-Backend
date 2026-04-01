package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.*;
import mx.edu.upsite.demo.Entities.id.LikePublicacionID;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "likes_publicaciones")
public class LikePublicacion {

    @EmbeddedId
    private LikePublicacionID id = new LikePublicacionID();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPublicacion") // Se vincula con el campo del LikePublicacionId
    @JoinColumn(name = "id_publicacion")
    private Publicacion publicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario") // Se vincula con el campo del LikePublicacionId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_like", updatable = false)
    private OffsetDateTime fechaLike;
}
