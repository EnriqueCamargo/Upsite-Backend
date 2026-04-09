package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Carrera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarreraRepository extends JpaRepository<Carrera, Integer> {

    // Busca por nombre ignorando mayúsculas/minúsculas
    Optional<Carrera> findByNombreIgnoreCase(String nombre);

    // Verifica si existe ignorando mayúsculas/minúsculas
    boolean existsByNombreIgnoreCase(String nombre);
}
