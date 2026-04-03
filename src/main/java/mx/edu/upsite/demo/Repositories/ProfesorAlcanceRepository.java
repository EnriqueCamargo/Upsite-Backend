package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.ProfesorAlcance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfesorAlcanceRepository  extends JpaRepository<ProfesorAlcance,Integer> {
}
