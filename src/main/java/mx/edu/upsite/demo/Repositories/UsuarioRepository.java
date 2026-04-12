package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository <Usuario, Integer> {
    Optional<Usuario> findByGoogleId(String googleId);
    Optional<Usuario> findByEmail(String email);
    @Query("SELECT u FROM Usuario u WHERE " +
            "LOWER(CONCAT(u.nombres, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :criterio, '%')) OR " +
            "u.matricula LIKE %:criterio%")
    List<Usuario> buscarUsuario(@Param("criterio") String criterio);
    List<Usuario> findByStatusAndGrupoId(Integer status, Integer grupoId);
    List<Usuario> findByStatusAndGrupoNombre(Integer status, String grupoNombre);
    List<Usuario>findByStatus(Integer status);
    List<Usuario> findByCarreraIdAndStatus(Integer idCarrera, Integer status);
    List<Usuario> findByGrupoIdAndStatus(Integer idGrupo, Integer status);
}
