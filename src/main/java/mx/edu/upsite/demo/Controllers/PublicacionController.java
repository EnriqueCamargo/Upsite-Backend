package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Services.LikePublicacionService;
import mx.edu.upsite.demo.Services.MultimediaPublicacionService;
import mx.edu.upsite.demo.Services.PublicacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {

    private final PublicacionService publicacionService;
    private final LikePublicacionService likePublicacionService;
    private final MultimediaPublicacionService multimediaPublicacionService;

    // --- CONSULTAS ---

    @GetMapping("/feed")
    public ResponseEntity<List<PublicacionResponseDTO>> getFeed(
            @RequestParam(required = false) Integer carrera,
            @RequestParam(required = false) Importancia importancia) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(publicacionService.getFeed(carrera, importancia, usuario.getId()));
    }

    @GetMapping("/autor/{idAutor}")
    public ResponseEntity<List<PublicacionResponseDTO>> getByAutor(@PathVariable Integer idAutor) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(publicacionService.getPublicacionesByAutorId(idAutor, usuario.getId()));
    }

    // --- ACCIONES CORE ---

    @PostMapping
    public ResponseEntity<PublicacionResponseDTO> crear(@RequestBody PublicacionRequestDTO dto) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // Usamos status 201 para creaciones exitosas
        return ResponseEntity.status(HttpStatus.CREATED).body(publicacionService.crear(dto, usuario.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionResponseDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody PublicacionRequestDTO dto) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(publicacionService.actualizar(id, dto, usuario.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        publicacionService.eliminarPublicacion(id, usuario.getId());
        return ResponseEntity.noContent().build(); // 204 No Content es el estándar tras borrar
    }

    // --- INTERACCIONES Y MULTIMEDIA ---

    @PostMapping("/{id}/like")
    public ResponseEntity<Void> darLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        likePublicacionService.darLike(id, usuario.getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> quitarLike(@PathVariable Integer id) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        likePublicacionService.quitarLike(id, usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/multimedia")
    public ResponseEntity<MultimediaPublicacionResponseDTO> subirMultimedia(
            @PathVariable Integer id,
            @RequestBody MultimediaPublicacionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(multimediaPublicacionService.subirMultimedia(id, dto));
    }
}
