package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Repositories.ComentarioRepository;
import mx.edu.upsite.demo.Repositories.LikePublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final LikePublicacionRepository likePublicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;

    public List<PublicacionResponseDTO> getFeed(Integer carrera, Importancia importancia, Integer idUsuario) {
        String importanciaStr = importancia != null ? importancia.name() : null;
        return publicacionRepository.findFeed(carrera, importanciaStr)
                .stream()
                .map(p -> toDTO(p, idUsuario))
                .toList();
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario) {
        List<MultimediaPublicacionResponseDTO> multimedia = p.getMultimedia()
                .stream()
                .map(m -> new MultimediaPublicacionResponseDTO(
                        m.getId(),
                        m.getRuta(),
                        m.getTipo()
                ))
                .toList();

        UsuarioResponseDTO usuario = new UsuarioResponseDTO(
                p.getUsuario().getId(),
                p.getUsuario().getNombres(),
                p.getUsuario().getApellidos(),
                p.getUsuario().getGrupo() != null ? p.getUsuario().getGrupo().getNombre() : null,
                p.getUsuario().getGrupo() != null && p.getUsuario().getGrupo().getCarrera() != null
                        ? p.getUsuario().getGrupo().getCarrera().getNombre() : null,
                p.getUsuario().getFotoPerfil(),
                p.getUsuario().getEmail(),
                p.getUsuario().getRol(),
                p.getUsuario().getMatricula(),
                null,
                null,
                null
        );

        Long totalLikes = likePublicacionRepository.countByIdIdPublicacion(p.getId());
        Boolean meGusta = likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(p.getId(), idUsuario);
        Long totalComentarios = comentarioRepository.countByPublicacionIdAndStatus(p.getId(), 1);


        return new PublicacionResponseDTO(
                p.getId(),
                p.getTexto(),
                p.getImportancia(),
                multimedia,
                usuario,
                totalLikes,
                totalComentarios,
                meGusta
        );
    }

    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Publicacion publicacion = new Publicacion();
        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto());
        publicacion.setImportancia(dto.importancia());
        publicacion.setEsGlobal(dto.esGlobal());
        publicacion.setModeracion(Moderacion.PENDIENTE);
        publicacion.setStatus(1);

        return toDTO(publicacionRepository.save(publicacion), idUsuario);
    }
}