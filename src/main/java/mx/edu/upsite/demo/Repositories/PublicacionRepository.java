package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Enums.Importancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
    @Query(value = "SELECT * FROM publicaciones p WHERE p.status = 1 AND p.es_global = true " +
            "AND (:carrera IS NULL OR p.target_carrera = :carrera) " +
            "AND (CAST(:importancia AS VARCHAR) IS NULL OR p.importancia = CAST(:importancia AS enum_importancia))",
            nativeQuery = true)
    List<Publicacion> findFeed(
            @Param("carrera") Integer carrera,
            @Param("importancia") String importancia
    );
}
