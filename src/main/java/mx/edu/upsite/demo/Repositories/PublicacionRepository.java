package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
}
