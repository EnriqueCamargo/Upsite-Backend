package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.Entities.MultimediaPublicacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Enums.TipoMultimedia;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.InternalServerErrorException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.MultimediaPublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MultimediaPublicacionService {

    private final MultimediaPublicacionRepository multimediaPublicacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final StorageService storageService;

    @Transactional
    public MultimediaPublicacionResponseDTO subirMultimedia(Integer idPublicacion, MultipartFile archivo, TipoMultimedia tipo) {
        // 1. Blindaje de Publicación: Debe existir y estar activa (status 1)
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new ResourceNotFoundException("No se puede subir multimedia: Publicación no encontrada."));

        if (publicacion.getStatus() == 0) {
            throw new BadRequestException("No se puede añadir multimedia a una publicación eliminada.");
        }

        // 2. Blindaje de Capacidad (Opcional pero recomendado)
        // Podrías limitar a que cada post de UPSIN no tenga más de 5 archivos
        if (publicacion.getMultimedia().size() >= 5) {
            throw new BadRequestException("La publicación ya alcanzó el límite máximo de archivos (5).");
        }

        try {
            // 3. Delegación al StorageService (El blindaje físico que hicimos antes)
            String rutaGenerada = storageService.guardar(archivo, tipo);

            // 4. Mapeo y Persistencia
            MultimediaPublicacion media = new MultimediaPublicacion();
            media.setRuta(rutaGenerada);
            media.setTipo(tipo);
            media.setPublicacion(publicacion);

            MultimediaPublicacion guardada = multimediaPublicacionRepository.save(media);

            // 5. Retorno limpio
            return new MultimediaPublicacionResponseDTO(
                    guardada.getId(),
                    guardada.getRuta(),
                    guardada.getTipo()
            );

        } catch (IOException e) {
            // Transformamos el error de bajo nivel en algo que Spring entienda
            throw new InternalServerErrorException("Error crítico al procesar el archivo en el servidor.");
        }
    }
}
