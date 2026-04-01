package mx.edu.upsite.demo.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "carreras")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;
}