package mx.edu.upsite.demo.Entities;
import jakarta.persistence.*;
import lombok.*;
import mx.edu.upsite.demo.Entities.id.LikeComentarioID;
import org.hibernate.annotations.CreationTimestamp;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "likes_comentarios")
public class LikeComentario {

    @EmbeddedId
    private LikeComentarioID id = new LikeComentarioID();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idComentario") // Mapea el idComentario del LikeComentarioId
    @JoinColumn(name = "id_comentario")
    private Comentario comentario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario") // Mapea el idUsuario del LikeComentarioId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "fecha_like", updatable = false)
    private OffsetDateTime fechaLike;
}
