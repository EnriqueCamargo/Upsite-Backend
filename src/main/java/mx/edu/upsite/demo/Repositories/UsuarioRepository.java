package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository <Usuario, Integer> {
    Optional<Usuario> findByGoogleId(String googleId);
}
