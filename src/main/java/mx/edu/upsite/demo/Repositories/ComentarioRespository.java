package mx.edu.upsite.demo.Repositories;


import mx.edu.upsite.demo.Entities.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRespository extends JpaRepository<Comentario, Integer> {
}
