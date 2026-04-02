package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.PublicacionDTO;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;

    public List<PublicacionDTO> getAll() {
        return publicacionRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public PublicacionDTO getById(Integer id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));
        return toDTO(publicacion);
    }

    public PublicacionDTO create(PublicacionDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.idUsuario())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Publicacion publicacion = new Publicacion();
        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto());
        publicacion.setImportancia(dto.importancia());
        publicacion.setEsGlobal(dto.esGlobal());
        publicacion.setTargetCarrera(dto.targetCarrera());
        publicacion.setTargetGrupo(dto.targetGrupo());
        publicacion.setModeracion(Moderacion.PENDIENTE);
        publicacion.setStatus(1);

        return toDTO(publicacionRepository.save(publicacion));
    }

    public PublicacionDTO update(Integer id, PublicacionDTO dto) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));
        publicacion.setTexto(dto.texto());
        publicacion.setImportancia(dto.importancia());
        return toDTO(publicacionRepository.save(publicacion));
    }

    public void delete(Integer id) {
        Publicacion publicacion = publicacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));
        publicacion.setStatus(0);
        publicacion.setFechaEliminacion(OffsetDateTime.now());
        publicacionRepository.save(publicacion);
    }

    private PublicacionDTO toDTO(Publicacion p) {
        return new PublicacionDTO(
                p.getId(),
                p.getUsuario() != null ? p.getUsuario().getId() : null,
                p.getTexto(),
                p.getImportancia(),
                p.getModeracion(),
                p.getFeedbackIA(),
                p.getTargetCarrera(),
                p.getTargetGrupo(),
                p.getEsGlobal(),
                p.getStatus(),
                p.getFechaPublicacion(),
                p.getFechaEliminacion()
        );
    }
}