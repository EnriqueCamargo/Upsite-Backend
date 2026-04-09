package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Request.PublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.ComentarioRepository;
import mx.edu.upsite.demo.Repositories.LikePublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final LikePublicacionRepository likePublicacionRepository;
    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModerationService moderationService;
    private final MultimediaPublicacionService multimediaPublicacionService;

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getFeed(Integer carrera, Importancia importancia, Integer idUsuario) {
        // 1. Blindaje de Identidad: Si el ID de usuario no existe, el mapeo de likes fallará
        if (idUsuario != null && !usuarioRepository.existsById(idUsuario)) {
            throw new ResourceNotFoundException("No se puede generar el feed: El usuario con ID " + idUsuario + " no existe.");
        }

        // 2. Blindaje de Enums: Evitamos errores si el enum llega nulo
        String importanciaStr = (importancia != null) ? importancia.name() : null;

        // 3. Obtención y Mapeo Blindado
        List<Publicacion> publicaciones = publicacionRepository.findFeed(carrera, importanciaStr);

        // Si no hay publicaciones, devolvemos lista vacía (200 OK), no error.
        if (publicaciones.isEmpty()) {
            return List.of();
        }

        return publicaciones.stream()
                .map(p -> toDTO(p, idUsuario))
                .toList();
    }

    private PublicacionResponseDTO toDTO(Publicacion p, Integer idUsuario) {
        // 1. Blindaje de Multimedia (Evita NPE si la lista es null)
        List<MultimediaPublicacionResponseDTO> multimedia = (p.getMultimedia() != null)
                ? p.getMultimedia().stream()
                  .map(m -> new MultimediaPublicacionResponseDTO(
                          m.getId(),
                          m.getRuta(),
                          m.getTipo()))
                  .toList()
                : List.of();

        // 2. Blindaje de Usuario (Autor de la publicación)
        UsuarioResponseDTO usuarioDTO = null;
        if (p.getUsuario() != null) {
            Usuario u = p.getUsuario();
            usuarioDTO = new UsuarioResponseDTO(
                    u.getId(),
                    u.getNombres(),
                    u.getApellidos(),
                    (u.getGrupo() != null) ? u.getGrupo().getNombre() : null,
                    (u.getGrupo() != null && u.getGrupo().getCarrera() != null)
                            ? u.getGrupo().getCarrera().getNombre() : null,
                    u.getFotoPerfil(),
                    u.getEmail(),
                    u.getRol(),
                    u.getMatricula(),
                    null, null, null // Seguidores/Siguiendo no suelen ser necesarios en el Feed
            );
        }

        // 3. Blindaje de Contadores e Interacción
        // Si idUsuario es null (ej. sesión expirada), meGusta debe ser false, no error
        Long totalLikes = likePublicacionRepository.countByIdIdPublicacion(p.getId());
        Boolean meGusta = (idUsuario != null)
                && likePublicacionRepository.existsByIdIdPublicacionAndIdIdUsuario(p.getId(), idUsuario);

        Long totalComentarios = comentarioRepository.countByPublicacionIdAndStatus(p.getId(), 1);

        // 4. Construcción final con protección de texto
        return new PublicacionResponseDTO(
                p.getId(),
                (p.getTexto() != null) ? p.getTexto() : "", // Evita nulos en el contenido
                p.getImportancia(),
                multimedia,
                usuarioDTO,
                (totalLikes != null) ? totalLikes : 0L,
                (totalComentarios != null) ? totalComentarios : 0L,
                meGusta
        );
    }


    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Integer idUsuario) {
        PublicacionResponseDTO resultado = guardarPublicacion(dto, idUsuario);
        return resultado;
    }

    @Transactional
    protected PublicacionResponseDTO guardarPublicacion(PublicacionRequestDTO dto, Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede publicar: Usuario no encontrado."));

        if (usuario.getStatus() == 0) {
            throw new BadRequestException("Un usuario desactivado no puede realizar publicaciones.");
        }

        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación es obligatorio.");
        }

        boolean esApropiado = moderationService.esContenidoApropiado(dto.texto().trim(), null);

        Publicacion publicacion = new Publicacion();
        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto().trim());

        if (dto.importancia() != null) {
            publicacion.setImportancia(dto.importancia());
        }
        publicacion.setEsGlobal(dto.esGlobal());

        if (esApropiado) {
            publicacion.setModeracion(Moderacion.APROBADO);
            publicacion.setStatus(1);
        } else {
            publicacion.setModeracion(Moderacion.RECHAZADO);
            publicacion.setStatus(0);
        }

        Publicacion guardada = publicacionRepository.save(publicacion);

        // Guardar multimedia siempre, sea aprobado o no
        if (dto.multimedia() != null && !dto.multimedia().isEmpty()) {
            for (MultimediaPublicacionRequestDTO mediaDto : dto.multimedia()) {
                MultimediaPublicacionRequestDTO mediaDtoConId = new MultimediaPublicacionRequestDTO(
                        mediaDto.ruta(),
                        mediaDto.tipoMultimedia(),
                        guardada.getId()
                );
                multimediaPublicacionService.subirMultimedia(guardada.getId(), mediaDtoConId);
            }
        }

        // Lanzamos DESPUÉS de que todo se guardó, pero DENTRO de @Transactional
        // para que el commit ya ocurrió antes de llegar aquí
        if (!esApropiado) {
            throw new BadRequestException("Tu publicación contiene contenido inapropiado y ha sido retirada para revisión.");
        }

        return toDTO(guardada, idUsuario);
    }
    @Transactional
    public void eliminarPublicacion(Integer idPublicacion, Integer idUsuarioLogueado) {
        // 1. Buscamos la publicación
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la publicación a eliminar."));

        // 2. Blindaje de Seguridad: Solo el autor (o un Admin) puede borrarla
        if (!publicacion.getUsuario().getId().equals(idUsuarioLogueado)) {
            throw new BadRequestException("No tienes permisos para eliminar esta publicación.");
        }

        // 3. Blindaje de Estado: Si ya está eliminada, lanzamos conflicto
        if (publicacion.getStatus() == 0) {
            throw new ConflictException("La publicación ya ha sido eliminada anteriormente.");
        }

        // 4. Aplicamos Soft Delete
        publicacion.setStatus(0);
        publicacion.setFechaEliminacion(OffsetDateTime.now()); // Usando tu tipo de dato de la entidad

        publicacionRepository.save(publicacion);
    }

    @Transactional(readOnly = true)
    public List<PublicacionResponseDTO> getPublicacionesByAutorId(Integer idAutor, Integer idUsuarioLogueado) {
        // 1. Validamos que el autor existe y está activo
        Usuario autor = usuarioRepository.findById(idAutor)
                .orElseThrow(() -> new ResourceNotFoundException("El perfil solicitado no existe."));

        if (autor.getStatus() == 0) {
            throw new BadRequestException("No se pueden consultar publicaciones de un usuario desactivado.");
        }

        // 2. Consulta al repositorio filtrando por status = 1
        // Nota: Asegúrate de tener este método en tu PublicacionRepository
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

        if (publicacion.getStatus() == 0 && publicacion.getModeracion() != Moderacion.RECHAZADO) {
            throw new ConflictException("No se puede editar una publicación que ha sido eliminada.");
        }

        // 1. Re-moderación del nuevo texto
        boolean esApropiado = moderationService.esContenidoApropiado(dto.texto().trim(), null);

        // 2. Actualización de campos
        publicacion.setTexto(dto.texto().trim());

        if (dto.importancia() != null) {
            publicacion.setImportancia(dto.importancia());
        }
        publicacion.setEsGlobal(dto.esGlobal());

        // 3. Si el nuevo texto es malo, la "baneamos" de nuevo
        if (!esApropiado) {
            publicacion.setModeracion(Moderacion.RECHAZADO);
            publicacion.setStatus(0);
        } else {
            publicacion.setModeracion(Moderacion.APROBADO);
            publicacion.setStatus(1);
        }

        Publicacion actualizada = publicacionRepository.save(publicacion);

        if (!esApropiado) {
            throw new BadRequestException("La actualización contiene contenido inapropiado y la publicación ha sido retirada.");
        }

        return toDTO(actualizada, idUsuarioLogueado);
    }
}