package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.TipoNotificacion;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "notificaciones")
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_emisor")
    private Usuario emisor;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_publicacion")
    private Publicacion publicacion;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ref_comentario")
    private Comentario comentario;

    @Column(name = "leido")
    private Boolean leido = false;

    @Column(name = "enviado_email")
    private Boolean enviadoEmail = false;

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private OffsetDateTime fecha;
    @Column(name = "tipo_notificacion")
    private TipoNotificacion tipoNotificacion;

}
