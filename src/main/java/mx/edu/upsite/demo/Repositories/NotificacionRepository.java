package mx.edu.upsite.demo.Repositories;

import mx.edu.upsite.demo.Entities.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer> {
}
