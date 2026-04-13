package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.NotificacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Services.NotificacionService;
import mx.edu.upsite.demo.Services.PublicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;


    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>> getMisNotificaciones(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        // Extraemos al usuario del contexto de seguridad (como en tus otros controladores)
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(notificacionService.listarNotificaciones(usuario.getId(), page, size));
    }


}