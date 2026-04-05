package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Entities.Usuario;
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
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }


    //METODO GENERAL DE BUSCADOR
    @Transactional(readOnly=true)
    public List<UsuarioResponseDTO> buscarUsuario(String parametro, Integer idUsuarioLogueado) {
        return usuarioRepository.buscarUsuario(parametro).stream().filter(usuario -> usuario.getStatus().equals(1))
                .map(u -> {

                    boolean loSigo = u.getSeguidores().stream()
                            .anyMatch(s -> s.getId().equals(idUsuarioLogueado));
                    return mapearADto(u, loSigo);
                })
                .toList();
    }


    public List<UsuarioResponseDTO> buscarUsuarioPorGrupo(String nombreGrupo){
        return usuarioRepository.findByStatusAndGrupoNombre(1,nombreGrupo).stream()
                .map(usuario -> mapearADto(usuario,null)).toList();
    }
    public List<UsuarioResponseDTO> buscarUsuarioPorGrupoId(Integer id){
        return usuarioRepository.findByStatusAndGrupoId(1,id).stream()
                .map(usuario -> mapearADto(usuario,null)).toList();
    }

    @Transactional
    public void asignarGrupo(Integer idUsuario, Integer idGrupo){
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(()->new RuntimeException("Usuario no encontrado"));
        Grupo grupo = grupoRepository.findById(idGrupo).orElseThrow(()-> new RuntimeException("Grupo no encontrado"));

        usuario.setGrupo(grupo);
        usuarioRepository.save(usuario);
    }

    public boolean desactivarUsuario(Integer id){
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setStatus(0);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
        }
    public boolean reactivarUsuario(Integer id){
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setStatus(1);
            usuarioRepository.save(usuario);
            return true;
        }).orElse(false);
    }



    private UsuarioResponseDTO mapearADto(Usuario u, Boolean loSigo) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getNombres(),
                u.getApellidos(),
                u.getGrupo() != null ? u.getGrupo().getNombre() : null,
                (u.getGrupo() != null && u.getGrupo().getCarrera() != null)
                        ? u.getGrupo().getCarrera().getNombre() : null,
                u.getFotoPerfil(),
                u.getEmail(),
                u.getRol(),
                u.getMatricula(),
                (long) u.getSeguidores().size(),
                (long) u.getSiguiendo().size(),
                loSigo
        );
    }

    }


















