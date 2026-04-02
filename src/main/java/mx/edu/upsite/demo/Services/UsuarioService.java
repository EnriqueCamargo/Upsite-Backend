package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.UsuarioDTO;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<UsuarioDTO> getAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public UsuarioDTO getById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toDTO(usuario);
    }

    public UsuarioDTO create(UsuarioDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setGoogleId(dto.googleId());
        usuario.setEmail(dto.email());
        usuario.setMatricula(dto.matricula());
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setFotoPerfil(dto.fotoPerfil());
        return toDTO(usuarioRepository.save(usuario));
    }

    public UsuarioDTO update(Integer id, UsuarioDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setNombres(dto.nombres());
        usuario.setApellidos(dto.apellidos());
        usuario.setFotoPerfil(dto.fotoPerfil());
        return toDTO(usuarioRepository.save(usuario));
    }

    public void delete(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setStatus(0);
        usuario.setFechaEliminacion(OffsetDateTime.now());
        usuarioRepository.save(usuario);
    }

    private UsuarioDTO toDTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getGoogleId(),
                u.getEmail(),
                u.getMatricula(),
                u.getNombres(),
                u.getApellidos(),
                u.getFotoPerfil(),
                u.getRol(),
                u.getGrupo() != null ? u.getGrupo().getId() : null,
                u.getStatus(),
                u.getFechaCreacion(),
                u.getUltimoAcceso(),
                u.getUltimoIp(),
                u.getFechaEliminacion()
        );
    }
}



