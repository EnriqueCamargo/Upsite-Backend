package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.CarreraRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.CarreraResponseDTO;
import mx.edu.upsite.demo.Services.CarreraService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carreras")
@RequiredArgsConstructor
public class CarreraController {
    private final CarreraService carreraService;

    @GetMapping
    public ResponseEntity<List<CarreraResponseDTO>> getall(){
        return ResponseEntity.ok(carreraService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarreraResponseDTO> getById(@PathVariable Integer id){
        return ResponseEntity.ok(carreraService.getById(id));
    }

    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CarreraResponseDTO> getByNombre(@PathVariable String nombre){
        return ResponseEntity.ok(carreraService.getByNombre(nombre));
    }

    @PostMapping
    public ResponseEntity<CarreraResponseDTO> crear(@RequestBody CarreraRequestDTO carrera){
        return ResponseEntity.status(HttpStatus.CREATED).body(carreraService.crear(carrera));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody CarreraRequestDTO carrera){
        carreraService.updateCarreraById(id,carrera);
        return ResponseEntity.ok().body("Carrera actualizada Correctamente");
    }

    @DeleteMapping("/borrar/{id}")
    public ResponseEntity<?> borrar(@PathVariable Integer id){
        carreraService.eliminarCarreraById(id);
        return ResponseEntity.ok().body("Carrera Eliminada correctamente");
    }
}
