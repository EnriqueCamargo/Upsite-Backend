package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.*;
import mx.edu.upsite.demo.Enums.Rol;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {

    @Id
    @Column(name = "id_usuario",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="google_id",nullable = false)
    private String googleId;

    @Column(name="email",nullable = false,unique = true)
    private String email;

    @Column(name ="matricula",unique = true,nullable = false)
    private String matricula;

    @Column(name="nombres",nullable = false)
    private String nombres;

    @Column(name="apellidos",nullable = false)
    private String apellidos;

    @Column(name="foto_perfil")
    private String fotoPerfil;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, columnDefinition = "enum_rol")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Rol rol=Rol.ESTUDIANTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    private Grupo grupo;

    @Column(name = "status")
    private Integer status = 1;

    @CreationTimestamp //Genera la fecha por default
    @Column(name = "fecha_creacion", updatable = false)
    private OffsetDateTime fechaCreacion=OffsetDateTime.now();

    @UpdateTimestamp
    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;

    // Estos son mis FANS (los que me siguen a mí)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "seguidores",
            joinColumns = @JoinColumn(name = "id_seguido"), // Yo soy el seguido
            inverseJoinColumns = @JoinColumn(name = "id_seguidor") // Ellos son los seguidores
    )
    private List<Usuario> seguidores = new ArrayList<>();

    // Estos son mis ÍDOLOS (a los que yo sigo)
    @ManyToMany(mappedBy = "seguidores")
    private List<Usuario> siguiendo = new ArrayList<>();
}
