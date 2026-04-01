package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.edu.upsite.demo.Enums.Rol;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
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
    @Column(name="rol",nullable = false)
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

    @Column(name = "ultimo_ip", columnDefinition = "inet")
    private String ultimoIp; // El tipo INET de Postgres se mapea como String en Java

    @Column(name = "fecha_eliminacion")
    private OffsetDateTime fechaEliminacion;
}
