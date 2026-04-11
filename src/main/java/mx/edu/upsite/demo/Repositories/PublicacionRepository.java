package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Publicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
    
    // Consulta para el feed optimizada con EXISTS para evitar duplicados por JOINs
    @Query(value = "SELECT p.* FROM publicaciones p " +
            "WHERE p.status = 1 " +
            "AND (" +
            "   :esPersonal = true " + // Si es personal (Docente+), ve todo
            "   OR p.es_global = true " + 
            "   OR p.id_usuario = :idUsuario " + // O que sea el autor
            "   OR EXISTS (" + 
            "       SELECT 1 FROM publicaciones_carreras pc " +
            "       WHERE pc.id_publicacion = p.id_publicacion " +
            "       AND pc.id_carrera = :carreraUsuario " +
            "       AND (" +
            "           NOT EXISTS (SELECT 1 FROM publicaciones_grupos pg WHERE pg.id_publicacion = p.id_publicacion) " + 
            "           OR EXISTS (SELECT 1 FROM publicaciones_grupos pg2 WHERE pg2.id_publicacion = p.id_publicacion AND pg2.id_grupo = :grupoUsuario)" +
            "       )" +
            "   )" +
            ") " +
            "AND (:esGlobalFiltro IS NULL OR p.es_global = :esGlobalFiltro) " +
            "AND (:carreraFiltro IS NULL OR EXISTS (SELECT 1 FROM publicaciones_carreras pc2 WHERE pc2.id_publicacion = p.id_publicacion AND pc2.id_carrera = :carreraFiltro)) " +
            "AND (:grupoFiltro IS NULL OR EXISTS (SELECT 1 FROM publicaciones_grupos pg3 WHERE pg3.id_publicacion = p.id_publicacion AND pg3.id_grupo = :grupoFiltro)) " +
            "AND (:importancia IS NULL OR p.importancia = CAST(:importancia AS enum_importancia)) " +
            "ORDER BY " +
            "   (p.fecha_publicacion::date = CURRENT_DATE) DESC, " +
            "   CASE " +
            "       WHEN p.fecha_publicacion::date = CURRENT_DATE THEN " +
            "           CASE " +
            "               WHEN p.importancia = 'AVISO_IMPORTANTE' THEN 1 " +
            "               WHEN p.importancia = 'AVISO' THEN 2 " +
            "               ELSE 3 " +
            "           END " +
            "       ELSE 4 " +
            "   END ASC, " +
            "   p.fecha_publicacion DESC",
            nativeQuery = true)
    List<Publicacion> findFeed(
            @Param("carreraUsuario") Integer carreraUsuario,
            @Param("grupoUsuario") Integer grupoUsuario,
            @Param("idUsuario") Integer idUsuario,
            @Param("carreraFiltro") Integer carreraFiltro,
            @Param("importancia") String importancia,
            @Param("esPersonal") boolean esPersonal,
            @Param("esGlobalFiltro") Boolean esGlobalFiltro,
            @Param("grupoFiltro") Integer grupoFiltro,
            Pageable pageable
    );

    Page<Publicacion> findByUsuarioIdAndStatusOrderByIdDesc(Integer id, Integer status, Pageable pageable);
}
