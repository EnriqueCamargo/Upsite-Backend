package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.GrupoRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final GrupoRepository grupoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<UsuarioResponseDTO> getAllUsuarios() {

            return usuarioRepository.findByStatus(1).stream()
                    .map(usuario -> mapearADto(usuario,null)).toList();

        }
    public List<UsuarioResponseDTO> getAllUsuariosEliminados() {
        return usuarioRepository.findByStatus(0).stream().map(usuario -> mapearADto(usuario,null)).toList();
    }

    public UsuarioResponseDTO getUsuarioById(Integer id) {
        return usuarioRepository.findById(id)
                .map(u -> mapearADto(u, null))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }


    //METODO GENERAL DE BUSCADOR
    @Transactional(readOnly=true)
    public List<UsuarioResponseDTO> buscarUsuario(String parametro, Integer idUsuarioLogueado) {

        return usuarioRepository.buscarUsuario(parametro).stream()
                .filter(u -> u.getStatus().equals(1)) // Solo gente activa
                .map(u -> {
                    boolean loSigo = (idUsuarioLogueado != null) &&
                            (u.getSeguidores() != null && u.getSeguidores().stream()
                                    .anyMatch(s -> s.getId().equals(idUsuarioLogueado)));

                    return mapearADto(u, loSigo);
                })
                .toList();
    }


    public List<UsuarioResponseDTO> buscarUsuarioPorGrupo(String nombreGrupo){
        if (!grupoRepository.existsByNombre(nombreGrupo)) {
            throw new ResourceNotFoundException("El grupo '" + nombreGrupo + "' no existe.");
        }


        return usuarioRepository.findByStatusAndGrupoNombre(1, nombreGrupo).stream()
                .map(usuario -> mapearADto(usuario, null))
                .toList();
    }
    public List<UsuarioResponseDTO> buscarUsuarioPorGrupoId(Integer id){
        if (!grupoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se pudo realizar la búsqueda: El grupo con ID " + id + " no existe.");
        }

        return usuarioRepository.findByStatusAndGrupoId(1, id).stream()
                .map(usuario -> mapearADto(usuario, null))
                .toList();
    }

    @Transactional
    public void asignarGrupo(Integer idUsuario, Integer idGrupo) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede asignar grupo: Usuario no encontrado."));

        // Regla de negocio: No puedes modificar un usuario desactivado
        if (usuario.getStatus() == 0) {
            throw new BadRequestException("No se pueden realizar cambios en un usuario desactivado.");
        }

        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new ResourceNotFoundException("El grupo con ID " + idGrupo + " no existe."));

        // Regla de negocio opcional: No re-asignar el mismo grupo
        if (usuario.getGrupo() != null && usuario.getGrupo().getId().equals(idGrupo)) {
            throw new ConflictException("El usuario ya pertenece a este grupo.");
        }

        usuario.setGrupo(grupo);
        usuarioRepository.save(usuario);
    }

    public void desactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getStatus() == 0) {
            throw new ConflictException("El usuario ya se encuentra desactivado.");
        }

        usuario.setStatus(0);
        usuarioRepository.save(usuario);
    }

    public void reactivarUsuario(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (usuario.getStatus() == 1) {
            throw new ConflictException("El usuario ya se encuentra activo.");
        }

        usuario.setStatus(1);
        usuarioRepository.save(usuario);
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
                siguesAEsteUsuario
        );
    }

    }


















