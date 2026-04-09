package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.*;
import mx.edu.upsite.demo.Entities.Carrera;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Enums.Rol;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final LikePublicacionRepository likePublicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarreraRepository carreraRepository; // Inyectado
    private final GrupoRepository grupoRepository;     // Inyectado
    private final ModerationService moderationService;
    private final MultimediaPublicacionService multimediaPublicacionService;

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getFeed(Integer carreraFiltro, Importancia importancia, Integer idUsuario, Boolean esGlobalFiltro, Integer grupoFiltro, int page, int size) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado, no se puede generar el feed."));

        Integer carreraUsuario = (usuario.getCarrera() != null) ? usuario.getCarrera().getId() :
                ((usuario.getGrupo() != null && usuario.getGrupo().getCarrera() != null)
                ? usuario.getGrupo().getCarrera().getId() : null);
        
        Integer grupoUsuario = (usuario.getGrupo() != null) ? usuario.getGrupo().getId() : null;

        String importanciaStr = (importancia != null) ? importancia.name() : null;

        // DOCENTE o superior ven todo (esPersonal = true)
        boolean esPersonal = usuario.getRol() != Rol.ESTUDIANTE;

        Pageable pageable = PageRequest.of(page, size);

        List<Publicacion> publicaciones = publicacionRepository.findFeed(
                carreraUsuario,
                grupoUsuario,
                idUsuario,
                carreraFiltro,
                importanciaStr,
                esPersonal,
                esGlobalFiltro,
                grupoFiltro,
                pageable
        );

        if (publicaciones.isEmpty()) {
            return List.of();
        }

        return publicaciones.stream()
                .map(p -> toDTO(p, idUsuario))
                .toList();
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario) {
        List<MultimediaPublicacionResponseDTO> multimedia = (p.getMultimedia() != null)
                ? p.getMultimedia().stream()
                .map(m -> new MultimediaPublicacionResponseDTO(m.getId(), m.getRuta(), m.getTipo()))
                .toList()
                : List.of();

        UsuarioResponseDTO usuarioDTO = null;
        if (p.getUsuario() != null) {
            Usuario u = p.getUsuario();
            usuarioDTO = new UsuarioResponseDTO(
                    u.getId(), u.getNombres(), u.getApellidos(),
                    (u.getGrupo() != null) ? u.getGrupo().getNombre() : null,
                    (u.getGrupo() != null && u.getGrupo().getCarrera() != null) ? u.getGrupo().getCarrera().getNombre() : null,
                    u.getFotoPerfil(), u.getEmail(), u.getRol(), u.getMatricula(),
                    null, null, null,
                    (u.getCarrera() != null) ? u.getCarrera().getId() : null,
                    (u.getGrupo() != null) ? u.getGrupo().getId() : null
            );
        }

        Long totalLikes = likePublicacionRepository.countByIdIdPublicacion(p.getId());
        Boolean meGusta = (idUsuario != null) && likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(p.getId(), idUsuario);
        Long totalComentarios = comentarioRepository.countByPublicacionIdAndStatus(p.getId(), 1);

        List<CarreraResponseDTO> carreras = p.getTargetCarreras().stream()
                .map(c -> new CarreraResponseDTO(c.getId(), c.getNombre()))
                .toList();

        List<GrupoResponseDTO> grupos = p.getGruposDestino().stream()
                .map(g -> new GrupoResponseDTO(g.getId(), g.getNombre(), g.getCarrera().getNombre()))
                .toList();

        return new PublicacionResponseDTO(
                p.getId(), (p.getTexto() != null) ? p.getTexto() : "",
                p.getImportancia(), multimedia, usuarioDTO,
                (totalLikes != null) ? totalLikes : 0L,
                (totalComentarios != null) ? totalComentarios : 0L,
                meGusta,
                carreras,
                grupos
        );
    }

    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Integer idUsuario) {
        return guardarPublicacion(dto, idUsuario, null);
    }

    @Transactional
    protected PublicacionResponseDTO guardarPublicacion(PublicacionRequestDTO dto, Integer idUsuario, Integer idPublicacion) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede publicar: Usuario no encontrado."));

        if (usuario.getStatus() == 0) {
            throw new BadRequestException("Un usuario desactivado no puede realizar publicaciones.");
        }

        // REGLA: Estudiantes solo pueden publicar globalmente y solo tipo PUBLICACION
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            if (!dto.esGlobal()) {
                throw new BadRequestException("Los estudiantes solo pueden realizar publicaciones globales.");
            }
            if (dto.importancia() != Importancia.PUBLICACION) {
                throw new BadRequestException("Los estudiantes solo pueden realizar publicaciones de tipo estándar (PUBLICACION).");
            }
        }

        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación es obligatorio.");
        }

        boolean esApropiado = moderationService.esContenidoApropiado(dto.texto().trim(), null);

        Publicacion publicacion = (idPublicacion != null) ? publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación a actualizar no encontrada.")) : new Publicacion();

        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto().trim());
        if (dto.importancia() != null) {
            publicacion.setImportancia(dto.importancia());
        }
        publicacion.setEsGlobal(dto.esGlobal());

        // Asignación de carreras y grupos
        if (!dto.esGlobal()) {
            if (dto.idsCarreras() != null && !dto.idsCarreras().isEmpty()) {
                List<Carrera> carreras = carreraRepository.findAllById(dto.idsCarreras());
                if (carreras.size() != dto.idsCarreras().size()) {
                    throw new ResourceNotFoundException("Una o más carreras especificadas no existen.");
                }
                publicacion.setTargetCarreras(carreras);

                if (dto.idsGrupos() != null && !dto.idsGrupos().isEmpty()) {
                    List<Grupo> grupos = grupoRepository.findAllById(dto.idsGrupos());
                    // Validar que todos los grupos encontrados pertenezcan a las carreras especificadas
                    boolean todosPertenecen = grupos.stream().allMatch(g -> 
                        dto.idsCarreras().contains(g.getCarrera().getId())
                    );
                    if (!todosPertenecen || grupos.size() != dto.idsGrupos().size()) {
                        throw new BadRequestException("Uno o más grupos no son válidos o no pertenecen a las carreras seleccionadas.");
                    }
                    publicacion.setGruposDestino(grupos);
                } else {
                    publicacion.getGruposDestino().clear();
                }
            } else {
                publicacion.getTargetCarreras().clear();
                publicacion.getGruposDestino().clear();
            }
        } else {
            publicacion.getTargetCarreras().clear();
            publicacion.getGruposDestino().clear();
        }


        if (esApropiado) {
            publicacion.setModeracion(Moderacion.APROBADO);
            publicacion.setStatus(1);
        } else {
            publicacion.setModeracion(Moderacion.RECHAZADO);
            publicacion.setStatus(0);
        }

        Publicacion guardada = publicacionRepository.save(publicacion);

        if (dto.multimedia() != null && !dto.multimedia().isEmpty()) {
            for (MultimediaPublicacionRequestDTO mediaDto : dto.multimedia()) {
                multimediaPublicacionService.subirMultimedia(guardada.getId(), mediaDto, idUsuario);
            }
        }

        if (!esApropiado) {
            throw new BadRequestException("Tu publicación contiene contenido inapropiado y ha sido retirada para revisión.");
        }

        return toDTO(guardada, idUsuario);
    }

    @Transactional
    public void eliminarPublicacion(Integer idPublicacion, Integer idUsuarioLogueado) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación a eliminar."));

        if (!publicacion.getUsuario().getId().equals(idUsuarioLogueado)) {
            throw new BadRequestException("No tienes permisos para eliminar esta publicación.");
        }

        if (publicacion.getStatus() == 0) {
            throw new ConflictException("La publicación ya ha sido eliminada anteriormente.");
        }

        publicacion.setStatus(0);
        publicacion.setFechaEliminacion(OffsetDateTime.now());
        publicacionRepository.save(publicacion);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getPublicacionesByAutorId(Integer idAutor, Integer idUsuarioLogueado) {
        Usuario autor = usuarioRepository.findById(idAutor)
                .orElseThrow(() -> new ResourceNotFoundException("El perfil solicitado no existe."));

        if (autor.getStatus() == 0) {
            throw new BadRequestException("No se pueden consultar publicaciones de un usuario desactivado.");
        }

        return publicacionRepository.findByUsuarioIdAndStatus(idAutor, 1)
                .stream()
                .map(p -> toDTO(p, idUsuarioLogueado))
                .toList();
    }

    @Transactional
    public PublicacionResponseDTO actualizar(Integer idPublicacion, PublicacionRequestDTO dto, Integer idUsuarioLogueado) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede editar: Publicación no encontrada."));

        if (!publicacion.getUsuario().getId().equals(idUsuarioLogueado)) {
            throw new BadRequestException("No tienes permisos para editar esta publicación.");
        }

        return guardarPublicacion(dto, idUsuarioLogueado, idPublicacion);
    }
}
