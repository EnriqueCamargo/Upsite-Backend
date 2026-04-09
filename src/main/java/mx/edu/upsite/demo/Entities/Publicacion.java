package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(name = "importancia", columnDefinition = "enum_importancia")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Importancia importancia=Importancia.PUBLICACION;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderacion", columnDefinition = "enum_moderacion")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Moderacion moderacion=Moderacion.PENDIENTE;

    @Column(name = "feedback_ia",columnDefinition = "TEXT")
    private String feedbackIA;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "publicaciones_carreras",
            joinColumns = @JoinColumn(name = "id_publicacion"),
            inverseJoinColumns = @JoinColumn(name = "id_carrera")
    )
    private List<Carrera> targetCarreras = new ArrayList<>();

    @Column(name="es_global")
    private Boolean esGlobal=false;

    @Column(name = "status")
    private Integer status=1;

    @CreationTimestamp
    @Column(name = "fecha_publicacion")
    private OffsetDateTime fechaPublicacion=OffsetDateTime.now();

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "publicaciones_grupos", // Nombre de la tabla física en DB
            joinColumns = @JoinColumn(name = "id_publicacion"), // FK a esta entidad
            inverseJoinColumns = @JoinColumn(name = "id_grupo")  // FK a la entidad Grupo
    )
    private List<Grupo> gruposDestino = new ArrayList<>();

    @OneToMany(mappedBy = "publicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MultimediaPublicacion> multimedia = new ArrayList<>();
}
