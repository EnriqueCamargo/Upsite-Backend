package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.GrupoRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.GrupoResponseDTO;
import mx.edu.upsite.demo.Services.GrupoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping
    public ResponseEntity<List<GrupoResponseDTO>> getAll() {
        return ResponseEntity.ok(grupoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(grupoService.getById(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<GrupoResponseDTO> getByNombre(@PathVariable String nombre) {
        return ResponseEntity.ok(grupoService.getByNombre(nombre));
    }

    @GetMapping("/carrera/{carreraId}")
    public ResponseEntity<List<GrupoResponseDTO>> getAllByCarrera(@PathVariable Integer carreraId) {
        return ResponseEntity.ok(grupoService.getAllByCarreraId(carreraId));
    }

    @GetMapping("/carreras")
    public ResponseEntity<List<GrupoResponseDTO>> getAllByCarreraIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(grupoService.getAllByCarreraIds(ids));
    }

    @PostMapping
    public ResponseEntity<GrupoResponseDTO> crear(@RequestBody GrupoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grupoService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody GrupoRequestDTO dto) {
        return ResponseEntity.ok(grupoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        grupoService.eliminar(id);
        return ResponseEntity.ok().body("Grupo eliminado correctamente");
    }
}