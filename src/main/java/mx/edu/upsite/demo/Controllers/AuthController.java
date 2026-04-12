package mx.edu.upsite.demo.Controllers;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.LoginResponseDTO;
import mx.edu.upsite.demo.Services.GoogleAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final GoogleAuthService googleAuthService;

    @PostMapping("/google")
    public ResponseEntity<LoginResponseDTO> loginWithGoogle(@RequestBody Map<String, String> body) {
        try {
            LoginResponseDTO response = googleAuthService.loginConGoogle(body.get("token"));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
