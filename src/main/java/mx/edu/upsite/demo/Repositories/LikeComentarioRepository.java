package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.LikeComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import mx.edu.upsite.demo.Entities.id.LikeComentarioID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface LikeComentarioRepository extends JpaRepository<LikeComentario, LikeComentarioID> {
    boolean existsByIdIdComentarioAndIdIdUsuario(Integer idComentario, Integer idUsuario);
    void deleteByIdIdComentarioAndIdIdUsuario(Integer idComentario, Integer idUsuario);
    Long countByIdIdComentario(Integer idComentario);

    @Query("SELECT l.id.idComentario, COUNT(l) FROM LikeComentario l WHERE l.id.idComentario IN :ids GROUP BY l.id.idComentario")
    List<Object[]> countLikesForComentarios(@Param("ids") List<Integer> ids);

    @Query("SELECT l.id.idComentario FROM LikeComentario l WHERE l.id.idUsuario = :idUsuario AND l.id.idComentario IN :ids")
    List<Integer> findLikedByUsuario(@Param("idUsuario") Integer idUsuario, @Param("ids") List<Integer> ids);
}