package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="publicaciones")
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id_publicacion",nullable = false)
    private Integer id;

    // En lugar de Integer idUsuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "texto",nullable = false,columnDefinition = "TEXT")
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(name = "importancia")
    private Importancia importancia=Importancia.PUBLICACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderacion")
    private Moderacion moderacion=Moderacion.PENDIENTE;

    @Column(name = "feedback_ia",columnDefinition = "TEXT")
    private String feedbackIA;

    @Column(name = "target_carrera")
    private  Integer targetCarrera;

    @Column(name="es_global")
    private Boolean esGlobal=false;

    @Column(name = "status")
    private Integer status=1;

    @CreationTimestamp
    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion=OffsetDateTime.now();

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion;



}
