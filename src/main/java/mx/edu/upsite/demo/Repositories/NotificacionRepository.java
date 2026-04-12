package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
    // Obtener todas las notificaciones de un usuario ordenadas por fecha (descendente)
    List<Notificacion> findByUsuarioIdOrderByFechaDesc(Integer idUsuario);

    // Para mostrar el contador de notificaciones pendientes en el Front-end
    long countByUsuarioIdAndLeidoFalse(Integer idUsuario);

    // Opcional: Para borrar notificaciones antiguas automáticamente si fuera necesario
    void deleteByUsuarioId(Integer idUsuario);
}
