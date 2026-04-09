package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.MultimediaPublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MultimediaPublicacionRepository extends JpaRepository<MultimediaPublicacion,Integer> {
    List<MultimediaPublicacion> findByPublicacionId(Integer idPublicacion);
    long countByPublicacionId(Integer idPublicacion);
}
