package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.LikePublicacion;
import mx.edu.upsite.demo.Entities.id.LikePublicacionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikePublicacionRepository extends JpaRepository<LikePublicacion, LikePublicacionID> {
    boolean existsByIdIdPublicacionAndIdIdUsuario(Integer idPublicacion, Integer idUsuario);
    void deleteByIdIdPublicacionAndIdIdUsuario(Integer idPublicacion, Integer idUsuario);
    Long countByIdIdPublicacion(Integer idPublicacion);
}
