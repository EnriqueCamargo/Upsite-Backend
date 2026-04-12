package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Services.UsuarioService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // 1. Obtener todos (paginado para admin)
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(usuarioService.getAllUsuarios(PageRequest.of(page, size)));
    }

    // 2. Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.getUsuarioById(id, logueado.getId()));
    }

    @GetMapping("/eliminados")
    public ResponseEntity<List<UsuarioResponseDTO>> getAllUsuariosEliminados() {
        return ResponseEntity.ok(usuarioService.getAllUsuariosEliminados());
    }

    // 3. El Buscador Global (Recibe el parámetro 'q')
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscar(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.buscarUsuario(q, logueado.getId(), PageRequest.of(page, size)));
    }

    @GetMapping("/grupo/{grupoNombre}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorNombreGrupo(@PathVariable String grupoNombre) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorGrupo(grupoNombre));
    }

    // 4. Filtrar por Grupo
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorGrupoId(@PathVariable Integer grupoId) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorGrupoId(grupoId));
    }

    // 5. Asignar Grupo
    @PutMapping("/{idUsuario}/asignar-grupo/{idGrupo}")
    public ResponseEntity<Void> asignarGrupo(@PathVariable Integer idUsuario, @PathVariable Integer idGrupo) {
        usuarioService.asignarGrupo(idUsuario, idGrupo);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<?> reactivar(@PathVariable Integer id) {
        usuarioService.reactivarUsuario(id);
        return ResponseEntity.ok("Usuario reactivado exitosamente");
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable Integer id) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok("Usuario desactivado exitosamente");
    }

    @PutMapping("/{id}/actualizar")
    public ResponseEntity<UsuarioResponseDTO> actualizar(@PathVariable Integer id, @RequestBody UsuarioResponseDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, dto));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> seguir(@PathVariable Integer id) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        usuarioService.seguirUsuario(logueado.getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> dejarDeSeguir(@PathVariable Integer id) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        usuarioService.dejarDeSeguir(logueado.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/seguidores")
    public ResponseEntity<List<UsuarioResponseDTO>> getSeguidores(@PathVariable Integer id) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.getSeguidores(id, logueado.getId()));
    }

    @GetMapping("/{id}/siguiendo")
    public ResponseEntity<List<UsuarioResponseDTO>> getSiguiendo(@PathVariable Integer id) {
        Usuario logueado = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(usuarioService.getSiguiendo(id, logueado.getId()));
    }
}