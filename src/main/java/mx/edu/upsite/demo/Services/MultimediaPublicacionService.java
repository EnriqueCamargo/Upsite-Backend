package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.MultimediaPublicacionRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.Entities.MultimediaPublicacion;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Enums.TipoMultimedia;
import mx.edu.upsite.demo.Repositories.MultimediaPublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MultimediaPublicacionService {

    private final MultimediaPublicacionRepository multimediaPublicacionRepository;
    private final PublicacionRepository publicacionRepository;
    private final StorageService storageService;

    public MultimediaPublicacionResponseDTO subirMultimedia(Integer idPublicacion, MultimediaPublicacionRequestDTO dto) {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));

        MultimediaPublicacion media = new MultimediaPublicacion();
        media.setRuta(dto.ruta());
        media.setTipo(dto.tipoMultimedia());
        media.setPublicacion(publicacion);

        multimediaPublicacionRepository.save(media);

        return new MultimediaPublicacionResponseDTO(
                media.getId(),
                media.getRuta(),
                media.getTipo()
        );
    }
}
