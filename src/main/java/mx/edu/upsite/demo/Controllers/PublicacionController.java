package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Services.LikePublicacionService;
import mx.edu.upsite.demo.Services.PublicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {
    private final PublicacionService publicacionService;
    private final LikePublicacionService likePublicacionService;

    @GetMapping("/feed")
    public ResponseEntity<List<PublicacionResponseDTO>> getFeed(
            @RequestParam(required = false) Integer carrera,
            @RequestParam(required = false) Importancia importancia) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(publicacionService.getFeed(carrera, importancia, usuario.getId()));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> darLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        likePublicacionService.darLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> quitarLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        likePublicacionService.quitarLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }
}
