package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.MultimediaPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MultimediaPublicacionRepository extends JpaRepository<MultimediaPublicacion,Integer> {
}
