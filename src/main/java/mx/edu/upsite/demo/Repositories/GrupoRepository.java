package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo,Integer> {
}
