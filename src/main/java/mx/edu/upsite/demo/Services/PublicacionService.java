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


    @Transactional
    public PublicacionResponseDTO crear(PublicacionRequestDTO dto, Integer idUsuario) {
        // 1. Blindaje de Usuario (Existencia y Status)
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede publicar: Usuario no encontrado."));

        if (usuario.getStatus() == 0) {
            throw new BadRequestException("Un usuario desactivado no puede realizar publicaciones.");
        }

        // 2. Blindaje de Texto (Obligatorio según @Column nullable=false)
        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación es obligatorio.");
        }

        // 3. Mapeo a la Entidad
        Publicacion publicacion = new Publicacion();
        publicacion.setUsuario(usuario);
        publicacion.setTexto(dto.texto().trim());

        // Blindaje de Enum: Si el DTO no lo trae, usamos el valor por defecto de la Entidad
        if (dto.importancia() != null) {
            publicacion.setImportancia(dto.importancia());
        }
        publicacion.setEsGlobal(dto.esGlobal());
        // 5. Valores por defecto del sistema
        publicacion.setModeracion(Moderacion.PENDIENTE);
        publicacion.setStatus(1);
        // fechaPublicacion se genera sola por @CreationTimestamp

        // 6. Guardado y Mapeo al DTO de salida
        Publicacion guardada = publicacionRepository.save(publicacion);

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
        // 1. Buscar la publicación original
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede editar: Publicación no encontrada."));

        // 2. Blindaje de Seguridad: Solo el autor puede editar su propio contenido
        if (!publicacion.getUsuario().getId().equals(idUsuarioLogueado)) {
            throw new BadRequestException("No tienes permisos para editar esta publicación.");
        }

        // 3. Blindaje de Estado: No se puede editar algo que ya fue eliminado (Soft Delete)
        if (publicacion.getStatus() == 0) {
            throw new ConflictException("No se puede editar una publicación que ha sido eliminada.");
        }

        // 4. Blindaje de Contenido: Validar que el nuevo texto no sea basura
        if (dto.texto() == null || dto.texto().trim().isEmpty()) {
            throw new BadRequestException("El contenido de la publicación no puede quedar vacío.");
        }

        // 5. Actualización de campos permitidos
        publicacion.setTexto(dto.texto().trim());

        // Actualizamos importancia solo si el DTO la trae, si no, conservamos la anterior
        if (dto.importancia() != null) {
            publicacion.setImportancia(dto.importancia());
        }

        // El booleano 'esGlobal' se asigna directamente (siendo primitivo en el DTO)
        publicacion.setEsGlobal(dto.esGlobal());

        // 6. Persistencia y retorno
        // Nota: No tocamos fechaPublicacion (es @CreationTimestamp) ni moderacion (a menos que quieras resetearla a PENDIENTE)
        Publicacion actualizada = publicacionRepository.save(publicacion);

        return toDTO(actualizada, idUsuarioLogueado);
    }
}