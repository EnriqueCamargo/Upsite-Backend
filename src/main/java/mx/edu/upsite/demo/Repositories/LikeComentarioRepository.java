package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.LikeComentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeComentarioRepository extends JpaRepository<LikeComentario,Integer> {
    boolean existsByIdIdComentarioAndIdIdUsuario(Integer idComentario, Integer idUsuario);
    void deleteByIdIdComentarioAndIdIdUsuario(Integer idComentario, Integer idUsuario);
    Long countByIdIdComentario(Integer idComentario);
}