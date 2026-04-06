package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.ComentarioRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.ComentarioResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Services.ComentarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class ComentarioController {

    private final ComentarioService comentarioService;

    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<ComentarioResponseDTO>> getComentarios(@PathVariable Integer id) {
        return ResponseEntity.ok(comentarioService.getComentariosByPublicacion(id));
    }

    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ComentarioResponseDTO> crear(
            @PathVariable Integer id,
            @RequestBody ComentarioRequestDTO dto) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(comentarioService.crear(id, usuario.getId(), dto.texto(), dto.idPadre()));
    }

    @PostMapping("/comentarios/{id}/like")
    public ResponseEntity<Void> darLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        comentarioService.darLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comentarios/{id}/like")
    public ResponseEntity<Void> quitarLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        comentarioService.quitarLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }
}
