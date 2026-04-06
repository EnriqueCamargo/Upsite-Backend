package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Services.PublicacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publicaciones")
@RequiredArgsConstructor
public class PublicacionController {
    private final PublicacionService publicacionService;

    @GetMapping("/feed")
    public ResponseEntity<List<PublicacionResponseDTO>> getFeed(
            @RequestParam(required = false) Integer carrera,
            @RequestParam(required = false) Importancia importancia) {
        return ResponseEntity.ok(publicacionService.getFeed(carrera, importancia));
    }
}
