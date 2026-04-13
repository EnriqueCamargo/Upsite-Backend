package mx.edu.upsite.demo.Repositories;


import mx.edu.upsite.demo.Entities.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByPublicacionIdAndStatusAndPadreIsNull(Integer idPublicacion, Integer status);
    List<Comentario> findByPadreIdAndStatus(Integer idPadre, Integer status);
    Long countByPublicacionIdAndStatus(Integer idPublicacion, Integer status);
    Long countByPadreIdAndStatus(Integer idPadre, Integer status);

    @Query("SELECT c.publicacion.id, COUNT(c) FROM Comentario c WHERE c.publicacion.id IN :ids AND c.status = 1 GROUP BY c.publicacion.id")
    List<Object[]> countByPublicacionIdsAndStatus(@Param("ids") List<Integer> ids);

    @Query("SELECT c.padre.id, COUNT(c) FROM Comentario c WHERE c.padre.id IN :ids AND c.status = 1 GROUP BY c.padre.id")
    List<Object[]> countByPadreIdsAndStatus(@Param("ids") List<Integer> ids);
}
