package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.LikePublicacion;
import mx.edu.upsite.demo.Entities.id.LikePublicacionID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface LikePublicacionRepository extends JpaRepository<LikePublicacion, LikePublicacionID> {
    boolean existsByIdIdPublicacionAndIdIdUsuario(Integer idPublicacion, Integer idUsuario);
    void deleteByIdIdPublicacionAndIdIdUsuario(Integer idPublicacion, Integer idUsuario);
    Long countByIdIdPublicacion(Integer idPublicacion);

    @Query("SELECT l.id.idPublicacion, COUNT(l) FROM LikePublicacion l WHERE l.id.idPublicacion IN :ids GROUP BY l.id.idPublicacion")
    List<Object[]> countLikesForPublicaciones(@Param("ids") List<Integer> ids);

    @Query("SELECT l.id.idPublicacion FROM LikePublicacion l WHERE l.id.idUsuario = :idUsuario AND l.id.idPublicacion IN :ids")
    List<Integer> findLikedByUsuario(@Param("idUsuario") Integer idUsuario, @Param("ids") List<Integer> ids);
}
