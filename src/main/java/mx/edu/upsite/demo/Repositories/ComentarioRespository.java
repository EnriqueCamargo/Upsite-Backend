package mx.edu.upsite.demo.Repositories;


import mx.edu.upsite.demo.Entities.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioRespository extends JpaRepository<Comentario, Integer> {
    List<Comentario> findByPublicacionIdAndStatusAndPadreIsNull(Integer idPublicacion, Integer status);
}
