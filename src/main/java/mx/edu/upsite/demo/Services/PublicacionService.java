package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.*;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final LikePublicacionRepository likePublicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final MultimediaPublicacionService multimediaPublicacionService;
    private final NotificacionService notificacionService; // Tu servicio
    private final CarreraRepository carreraRepository;     // De Alvaro
    private final GrupoRepository grupoRepository;         // De Alvaro
    private final ModerationService moderationService;     // De Alvaro

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
        return publicacionRepository.findFeed(carreraUsuario, grupoUsuario, idUsuario, carreraFiltro, impStr, esPersonal, esGlobalFiltro, grupoFiltro, pageable)
                .stream()
                .map(p -> toDTO(p, idUsuario))
                .toList();
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

        // VALIDACIONES DE ROL (Lógica de Alvaro)
        validarReglasPorRol(usuario, dto);

        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación es obligatorio.");
        }

        // MODERACIÓN (Lógica de Alvaro)
        boolean esApropiado = moderationService.esContenidoApropiado(dto.texto().trim(), null);

        Publicacion publicacion = (idPublicacion != null)
                ? publicacionRepository.findById(idPublicacion).orElseThrow(() -> new ResourceNotFoundException("No encontrada."))
                : new Publicacion();

        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto().trim());
        publicacion.setImportancia(dto.importancia());
        publicacion.setEsGlobal(dto.esGlobal());

        // GESTIÓN DE ALCANCE (Carreras/Grupos)
        configurarAlcance(publicacion, dto);

        if (esApropiado) {
            publicacion.setModeracion(Moderacion.APROBADO);
            publicacion.setStatus(1);
        } else {
            publicacion.setModeracion(Moderacion.RECHAZADO);
            publicacion.setStatus(0);
        }

        Publicacion guardada = publicacionRepository.save(publicacion);

        // NOTIFICACIONES (Tu lógica)
        if (esApropiado && (usuario.getRol() != Rol.ESTUDIANTE)) {
            procesarNotificacionesAvisos(guardada, usuario);
        }

        // MULTIMEDIA
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
        for (Usuario receptor : destinatarios) {
            if (!receptor.getId().equals(autor.getId())) {
                notificacionService.crearNotificacion(receptor, autor, TipoNotificacion.AVISO, mensaje, pub, true);
            }
        }
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario) {
        // Implementación de mapeo a DTO (se mantiene igual a tu versión anterior)
        // ... (el código de toDTO que ya tenías)
        return null; // Reemplazar con tu lógica de mapeo
    }

    @Transactional
    public void eliminarPublicacion(Integer idPublicacion, Integer idUsuarioLogueado) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación."));
        publicacion.setStatus(0);
        publicacion.setFechaEliminacion(OffsetDateTime.now());
        publicacionRepository.save(publicacion);
    }
}