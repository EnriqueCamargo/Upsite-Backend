package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo,Integer> {
    boolean existsByNombre(String nombre);
    Optional<Grupo> findByNombreIgnoreCase(String nombre);
    List<Grupo> findAllByCarreraId(Integer carreraId);
    boolean existsByNombreIgnoreCaseAndCarreraId(String nombre, Integer carreraId);
}
