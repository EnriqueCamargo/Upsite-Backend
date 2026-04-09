    package mx.edu.upsite.demo.Services;

    import lombok.RequiredArgsConstructor;
    import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
    import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
    import mx.edu.upsite.demo.Entities.MultimediaPublicacion;
    import mx.edu.upsite.demo.Entities.Publicacion;
    import mx.edu.upsite.demo.Enums.Moderacion; // Asegúrate de importar tu Enum
    import mx.edu.upsite.demo.Enums.Rol;
    import mx.edu.upsite.demo.Enums.TipoMultimedia;
    import mx.edu.upsite.demo.Exceptions.BadRequestException;
    import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
    import mx.edu.upsite.demo.Repositories.MultimediaPublicacionRepository;
    import mx.edu.upsite.demo.Repositories.PublicacionRepository;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;


    @Service
    @RequiredArgsConstructor
    public class MultimediaPublicacionService {

        private final MultimediaPublicacionRepository multimediaPublicacionRepository;
        private final PublicacionRepository publicacionRepository;
        private final ModerationService moderationService; // ← Inyección del servicio de Gemini

        @Transactional
        public MultimediaPublicacionResponseDTO subirMultimedia(
                Integer idPublicacion, MultimediaPublicacionRequestDTO dto, Integer idUsuarioAutenticado) {

            Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se puede registrar multimedia: Publicación no encontrada."));

            if (!publicacion.getUsuario().getId().equals(idUsuarioAutenticado)) {
                throw new BadRequestException("No tienes permiso para añadir multimedia a esta publicación.");
            }

            if (publicacion.getStatus() == 0 && publicacion.getModeracion() != Moderacion.RECHAZADO) {
                throw new BadRequestException("No se puede añadir multimedia a una publicación eliminada.");
            }

            // REGLA: Estudiantes no pueden subir videos
            if (publicacion.getUsuario().getRol() == Rol.ESTUDIANTE && dto.tipoMultimedia() == TipoMultimedia.VIDEO) {
                throw new BadRequestException("Los estudiantes no tienen permiso para subir videos, solo imágenes.");
            }

            // ← Contamos directo en BD, no desde la lista en memoria
            long totalActual = multimediaPublicacionRepository.countByPublicacionId(idPublicacion);
            if (totalActual >= 5) {
                throw new BadRequestException("La publicación ya alcanzó el límite máximo de archivos (5).");
            }

            boolean esApropiada = true;
            if (dto.tipoMultimedia() == TipoMultimedia.IMAGE) {
                esApropiada = moderationService.esContenidoApropiado(null, dto.ruta());
            }

            if (!esApropiada) {
                publicacion.setModeracion(Moderacion.RECHAZADO);
                publicacion.setStatus(0);
                publicacionRepository.save(publicacion);
            }

            MultimediaPublicacion media = new MultimediaPublicacion();
            media.setRuta(dto.ruta());
            media.setTipo(dto.tipoMultimedia());
            media.setPublicacion(publicacion);

            MultimediaPublicacion guardada = multimediaPublicacionRepository.save(media);

            if (!esApropiada) {
                throw new BadRequestException(
                        "La imagen fue detectada como inapropiada. La publicación ha sido retirada para revisión.");
            }

            return new MultimediaPublicacionResponseDTO(
                    guardada.getId(),
                    guardada.getRuta(),
                    guardada.getTipo()
            );
        }
    }