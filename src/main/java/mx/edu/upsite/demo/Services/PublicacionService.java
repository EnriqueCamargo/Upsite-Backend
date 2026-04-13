package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.*;
import mx.edu.upsite.demo.Entities.*;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Enums.Rol;
import mx.edu.upsite.demo.Enums.TipoNotificacion;
import mx.edu.upsite.demo.Repositories.*;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikePublicacionRepository likePublicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final MultimediaPublicacionService multimediaPublicacionService;
    private final NotificacionService notificacionService;
    private final CarreraRepository carreraRepository;
    private final GrupoRepository grupoRepository;
    private final ModerationService moderationService;

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getFeed(Integer carreraFiltro, Importancia importanciaFiltro, Integer idUsuario, Boolean esGlobalFiltro, Integer grupoFiltro, int page, int size) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado."));

        boolean esPersonal = usuario.getRol() == Rol.DOCENTE || usuario.getRol() == Rol.ADMIN;
        Integer carreraUsuario = (usuario.getCarrera() != null) ? usuario.getCarrera().getId() :
                ((usuario.getGrupo() != null) ? usuario.getGrupo().getCarrera().getId() : null);
        Integer grupoUsuario = (usuario.getGrupo() != null) ? usuario.getGrupo().getId() : null;

        String impStr = (importanciaFiltro != null) ? importanciaFiltro.name() : null;

        Pageable pageable = PageRequest.of(page, size);
        List<Publicacion> publicaciones = publicacionRepository.findFeed(carreraUsuario, grupoUsuario, idUsuario, carreraFiltro, impStr, esPersonal, esGlobalFiltro, grupoFiltro, pageable);
        
        return convertToDTOList(publicaciones, idUsuario);
    }

    private List<PublicacionResponseDTO> convertToDTOList(List<Publicacion> publicaciones, Integer idUsuario) {
        if (publicaciones.isEmpty()) return List.of();

        List<Integer> ids = publicaciones.stream().map(Publicacion::getId).toList();

        // Bulk fetch likes count
        Map<Integer, Long> likesMap = likePublicacionRepository.countLikesForPublicaciones(ids)
                .stream().collect(Collectors.toMap(o -> (Integer) o[0], o -> (Long) o[1]));

        // Bulk fetch comments count
        Map<Integer, Long> commentsMap = comentarioRepository.countByPublicacionIdsAndStatus(ids)
                .stream().collect(Collectors.toMap(o -> (Integer) o[0], o -> (Long) o[1]));

        // Bulk fetch if liked by current user
        Set<Integer> likedSet = (idUsuario != null) 
                ? new HashSet<>(likePublicacionRepository.findLikedByUsuario(idUsuario, ids)) 
                : Set.of();

        return publicaciones.stream()
                .map(p -> toDTO(p, idUsuario, likesMap, commentsMap, likedSet))
                .toList();
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario, Map<Integer, Long> likesMap, Map<Integer, Long> commentsMap, Set<Integer> likedSet) {
        List<MultimediaPublicacionResponseDTO> multimedia = (p.getMultimedia() != null)
                ? p.getMultimedia().stream()
                  .map(m -> new MultimediaPublicacionResponseDTO(m.getId(), m.getRuta(), m.getTipo()))
                  .toList() : List.of();

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
                    (u.getGrupo() != null) ? u.getGrupo().getId() : null,
                    u.getStatus()
            );
        }

        Long totalLikes = likesMap.getOrDefault(p.getId(), 0L);
        Boolean meGusta = likedSet.contains(p.getId());
        Long totalComentarios = commentsMap.getOrDefault(p.getId(), 0L);

        List<CarreraResponseDTO> carreras = (p.getTargetCarreras() != null) ? p.getTargetCarreras().stream()
                .map(c -> new CarreraResponseDTO(c.getId(), c.getNombre()))
                .toList() : List.of();

        List<GrupoResponseDTO> grupos = (p.getGruposDestino() != null) ? p.getGruposDestino().stream()
                .map(g -> new GrupoResponseDTO(g.getId(), g.getNombre(), (g.getCarrera() != null) ? g.getCarrera().getNombre() : null))
                .toList() : List.of();

        return new PublicacionResponseDTO(
                p.getId(), p.getTexto(), p.getImportancia(), multimedia, usuarioDTO,
                totalLikes,
                totalComentarios,
                meGusta, carreras, grupos, p.getFechaPublicacion()
        );
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario) {
        return convertToDTOList(List.of(p), idUsuario).get(0);
    }

    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Integer idUsuario) {
        return guardarPublicacion(dto, idUsuario, null);
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

    @Transactional
    protected PublicacionResponseDTO guardarPublicacion(PublicacionRequestDTO dto, Integer idUsuario, Integer idPublicacion) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede publicar: Usuario no encontrado."));

        if (usuario.getStatus() == 0) {
            throw new BadRequestException("Un usuario desactivado no puede realizar publicaciones.");
        }

        validarReglasPorRol(usuario, dto);

        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación es obligatorio.");
        }

        boolean esApropiado = moderationService.esContenidoApropiado(dto.texto().trim(), null);

        Publicacion publicacion = (idPublicacion != null)
                ? publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResourceNotFoundException("No encontrada."))
                : new Publicacion();

        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto().trim());
        publicacion.setImportancia(dto.importancia());
        publicacion.setEsGlobal(dto.esGlobal());

        configurarAlcance(publicacion, dto);

        if (esApropiado) {
            publicacion.setModeracion(Moderacion.APROBADO);
            publicacion.setStatus(1);
        } else {
            publicacion.setModeracion(Moderacion.RECHAZADO);
            publicacion.setStatus(0);
        }

        Publicacion guardada = publicacionRepository.save(publicacion);

        if (esApropiado && (usuario.getRol() != Rol.ESTUDIANTE)) {
            procesarNotificacionesAvisos(guardada, usuario);
        }

        if (dto.multimedia() != null && !dto.multimedia().isEmpty()) {
            for (MultimediaPublicacionRequestDTO mediaDto : dto.multimedia()) {
                multimediaPublicacionService.subirMultimedia(guardada.getId(), mediaDto, idUsuario);
            }
        }

        if (!esApropiado) {
            throw new BadRequestException("Tu publicación contiene contenido inapropiado y ha sido retirada.");
        }

        return toDTO(guardada, idUsuario);
    }

    private void validarReglasPorRol(Usuario usuario, PublicacionRequestDTO dto) {
        if (usuario.getRol() == Rol.ESTUDIANTE) {
            if (!dto.esGlobal()) throw new BadRequestException("Estudiantes solo publican globalmente.");
            if (dto.importancia() != Importancia.PUBLICACION) throw new BadRequestException("Estudiantes solo usan tipo PUBLICACION.");
        }
        if (usuario.getRol() == Rol.DOCENTE) {
            if (dto.importancia() == Importancia.AVISO_IMPORTANTE) throw new BadRequestException("Docentes no crean avisos urgentes.");
            if (dto.importancia() == Importancia.AVISO && dto.esGlobal()) throw new BadRequestException("Avisos de docentes no pueden ser globales.");
        }
    }

    private void configurarAlcance(Publicacion pub, PublicacionRequestDTO dto) {
        if (!dto.esGlobal() && dto.idsCarreras() != null && !dto.idsCarreras().isEmpty()) {
            List<Carrera> carreras = carreraRepository.findAllById(dto.idsCarreras());
            if (carreras.size() != dto.idsCarreras().size()) throw new BadRequestException("Carreras no válidas.");
            pub.setTargetCarreras(carreras);

            if (dto.idsGrupos() != null && !dto.idsGrupos().isEmpty()) {
                List<Grupo> grupos = grupoRepository.findAllById(dto.idsGrupos());
                pub.setGruposDestino(grupos);
            } else {
                pub.getGruposDestino().clear();
            }
        } else {
            pub.getTargetCarreras().clear();
            pub.getGruposDestino().clear();
        }
    }

    private void procesarNotificacionesAvisos(Publicacion pub, Usuario autor) {
        Set<Usuario> destinatarios = new HashSet<>();
        if (pub.getEsGlobal()) {
            destinatarios.addAll(usuarioRepository.findByStatus(1));
        } else {
            if (pub.getGruposDestino() != null && !pub.getGruposDestino().isEmpty()) {
                for (Grupo g : pub.getGruposDestino()) destinatarios.addAll(usuarioRepository.findByGrupoIdAndStatus(g.getId(), 1));
            } else if (pub.getTargetCarreras() != null && !pub.getTargetCarreras().isEmpty()) {
                for (Carrera c : pub.getTargetCarreras()) destinatarios.addAll(usuarioRepository.findByCarreraIdAndStatus(c.getId(), 1));
            }
        }

        String mensaje = "El " + autor.getRol() + " " + autor.getNombres() + " ha publicado un aviso importante.";
        notificacionService.crearNotificacionesBulk(destinatarios, autor, TipoNotificacion.AVISO, mensaje, pub, true);
    }

    @Transactional
    public void eliminarPublicacion(Integer idPublicacion, Integer idUsuarioLogueado) {
        Publicacion p = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No encontrada."));
        p.setStatus(0);
        p.setFechaEliminacion(OffsetDateTime.now());
        publicacionRepository.save(p);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getPublicacionesByAutorId(Integer idAutor, Integer idUsuarioLogueado, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Publicacion> publicaciones = publicacionRepository.findByUsuarioIdAndStatusOrderByIdDesc(idAutor, 1, pageable).getContent();
        return convertToDTOList(publicaciones, idUsuarioLogueado);
    }
}
