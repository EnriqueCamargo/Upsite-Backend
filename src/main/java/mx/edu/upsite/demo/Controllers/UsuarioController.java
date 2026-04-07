package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor

public class UsuarioController {

    private final UsuarioService usuarioService;

    // 1. Obtener todos los activos
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.getAllUsuarios());
    }

    // 2. Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.getUsuarioById(id));
    }

    @GetMapping("/eliminados")
    public ResponseEntity<List<UsuarioResponseDTO>>getAllUsuariosEliminados(){
        return ResponseEntity.ok(usuarioService.getAllUsuariosEliminados());
    }

    // 3. El Buscador Global (Recibe el parámetro 'q')
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponseDTO>> buscar(
            @RequestParam String q,
            @RequestParam Integer idLogueado) { // Después lo sacaremos del JWT
        return ResponseEntity.ok(usuarioService.buscarUsuario(q, idLogueado));
    }
    @GetMapping("/grupo/{grupoNombre}")
    public ResponseEntity<List<UsuarioResponseDTO>>listarPorNombreGrupo(@PathVariable String nombreGrupo){
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorGrupo(nombreGrupo));
    }
    // 4. Filtrar por Grupo
    @GetMapping("/grupo/{grupoId}")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorGrupoId(@PathVariable Integer grupoId) {
        return ResponseEntity.ok(usuarioService.buscarUsuarioPorGrupoId(grupoId));
    }

    // 5. Asignar Grupo (PUT porque modificamos un recurso existente)
    @PutMapping("/{idUsuario}/asignar-grupo/{idGrupo}")
    public ResponseEntity<Void> asignarGrupo(@PathVariable Integer idUsuario, @PathVariable Integer idGrupo) {
        usuarioService.asignarGrupo(idUsuario, idGrupo);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Boolean> reactivar(@PathVariable Integer id){
        return ResponseEntity.ok(usuarioService.reactivarUsuario(id));
    }
    // 6. Soft Delete (Desactivar)
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Boolean> desactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.desactivarUsuario(id));
    }

}