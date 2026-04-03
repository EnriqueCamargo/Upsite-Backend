package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.LikePublicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikePublicacionRepository extends JpaRepository<LikePublicacion,Integer> {
}
