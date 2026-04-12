package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Carrera;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Rol;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.CarreraRepository;
import mx.edu.upsite.demo.Repositories.GrupoRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final GrupoRepository grupoRepository;
    private final CarreraRepository carreraRepository;

    public List<UsuarioResponseDTO> getAllUsuarios() {
        return usuarioRepository.findAll().stream().map(usuario -> mapearADto(usuario, null)).toList();
    }

    @Transactional
    public void asignarGrupo(Integer idUsuario, Integer idGrupo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + idUsuario));

        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado con ID: " + idGrupo));

        usuario.setGrupo(grupo);
        usuario.setCarrera(grupo.getCarrera());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getStatus() == 0) {
            throw new ConflictException("El usuario ya se encuentra desactivado.");
        }

        usuario.setStatus(0);
        usuario.setFechaEliminacion(OffsetDateTime.now());
        usuarioRepository.save(usuario);
    }

    public void reactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getStatus() == 1) {
            throw new ConflictException("El usuario ya se encuentra activo.");
        }

        usuario.setStatus(1);
        usuario.setFechaEliminacion(null);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuarioAdmin(Integer id, UsuarioResponseDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        usuario.setRol(dto.rol());
        
        if (dto.idCarrera() != null) {
            Carrera carrera = carreraRepository.findById(dto.idCarrera())
                    .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada."));
            usuario.setCarrera(carrera);
        } else {
            usuario.setCarrera(null);
        }

        if (dto.idGrupo() != null) {
            Grupo grupo = grupoRepository.findById(dto.idGrupo())
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado."));
            usuario.setGrupo(grupo);
        } else {
            usuario.setGrupo(null);
        }

        // Lógica de cambio de status
        if (dto.status() != null) {
            if (usuario.getStatus() == 1 && dto.status() == 0) {
                usuario.setFechaEliminacion(OffsetDateTime.now());
            } else if (usuario.getStatus() == 0 && dto.status() == 1) {
                usuario.setFechaEliminacion(null);
            }
            usuario.setStatus(dto.status());
        }

        return mapearADto(usuarioRepository.save(usuario), null);
    }

    @Transactional
    public void seguirUsuario(Integer idSeguidor, Integer idSeguido) {
        if (idSeguidor.equals(idSeguido)) {
            throw new BadRequestException("No puedes seguirte a ti mismo.");
        }

        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new ResourceNotFoundException("Seguidor no encontrado."));
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a seguir no encontrado."));

        if (!seguidor.getSiguiendo().contains(seguido)) {
            seguidor.getSiguiendo().add(seguido);
            usuarioRepository.save(seguidor);
        }
    }

    @Transactional
    public void dejarDeSeguir(Integer idSeguidor, Integer idSeguido) {
        Usuario seguidor = usuarioRepository.findById(idSeguidor)
                .orElseThrow(() -> new ResourceNotFoundException("Seguidor no encontrado."));
        Usuario seguido = usuarioRepository.findById(idSeguido)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario a dejar de seguir no encontrado."));

        seguidor.getSiguiendo().remove(seguido);
        usuarioRepository.save(seguidor);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getSeguidores(Integer idUsuario, Integer idLogueado) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        return usuario.getSeguidores().stream()
                .map(u -> {
                    boolean loSigo = (idLogueado != null) &&
                            (u.getSeguidores() != null && u.getSeguidores().stream()
                                    .anyMatch(s -> s.getId().equals(idLogueado)));
                    return mapearADto(u, loSigo);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> getSiguiendo(Integer idUsuario, Integer idLogueado) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        return usuario.getSiguiendo().stream()
                .map(u -> {
                    boolean loSigo = (idLogueado != null) &&
                            (u.getSeguidores() != null && u.getSeguidores().stream()
                                    .anyMatch(s -> s.getId().equals(idLogueado)));
                    return mapearADto(u, loSigo);
                })
                .toList();
    }

    public List<UsuarioResponseDTO> buscarUsuario(String criterio, Integer idLogueado) {
        return usuarioRepository.buscarUsuario(criterio).stream()
                .map(u -> {
                    boolean loSigo = (idLogueado != null) &&
                            (u.getSeguidores() != null && u.getSeguidores().stream()
                                    .anyMatch(s -> s.getId().equals(idLogueado)));
                    return mapearADto(u, loSigo);
                })
                .toList();
    }

    public List<UsuarioResponseDTO> buscarUsuarioPorGrupo(String grupoNombre) {
        return usuarioRepository.findByStatusAndGrupoNombre(1, grupoNombre).stream()
                .map(u -> mapearADto(u, null)).toList();
    }

    public List<UsuarioResponseDTO> buscarUsuarioPorGrupoId(Integer grupoId) {
        return usuarioRepository.findByStatusAndGrupoId(1, grupoId).stream()
                .map(u -> mapearADto(u, null)).toList();
    }

    public List<UsuarioResponseDTO> getAllUsuariosEliminados() {
        return usuarioRepository.findByStatus(0).stream().map(usuario -> mapearADto(usuario,null)).toList();
    }

    public UsuarioResponseDTO getUsuarioById(Integer id, Integer idLogueado) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    boolean loSigo = (idLogueado != null) &&
                            (u.getSeguidores() != null && u.getSeguidores().stream()
                                    .anyMatch(s -> s.getId().equals(idLogueado)));
                    return mapearADto(u, loSigo);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    private UsuarioResponseDTO mapearADto(Usuario u, Boolean loSigo) {
        long totalSeguidores = (u.getSeguidores() != null) ? u.getSeguidores().size() : 0L;
        long totalSiguiendo = (u.getSiguiendo() != null) ? u.getSiguiendo().size() : 0L;
        boolean siguesAEsteUsuario = (loSigo != null) ? loSigo : false;
        String nombreGrupo = (u.getGrupo() != null) ? u.getGrupo().getNombre() : null;
        String nombreCarrera = (u.getGrupo() != null && u.getGrupo().getCarrera() != null)
                ? u.getGrupo().getCarrera().getNombre() : null;

        return new UsuarioResponseDTO(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                nombreGrupo,
                nombreCarrera,
                u.getFotoPerfil(),
                u.getEmail(),
                u.getRol(),
                u.getMatricula(),
                totalSeguidores,
                totalSiguiendo,
                siguesAEsteUsuario,
                (u.getCarrera() != null) ? u.getCarrera().getId() : 
                    ((u.getGrupo() != null && u.getGrupo().getCarrera() != null) ? u.getGrupo().getCarrera().getId() : null),
                (u.getGrupo() != null) ? u.getGrupo().getId() : null,
                u.getStatus()
        );
    }
}
