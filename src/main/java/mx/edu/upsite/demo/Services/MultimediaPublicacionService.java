package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
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

    public MultimediaPublicacionResponseDTO subirMultimedia(Integer idPublicacion, MultipartFile archivo) throws IOException {
        Publicacion publicacion = publicacionRepository.findById(idPublicacion)
                .orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));

        String ruta = storageService.guardar(archivo);

        MultimediaPublicacion media = new MultimediaPublicacion();
        media.setRuta(ruta);
        media.setTipo(archivo.getContentType().startsWith("image")
                ? TipoMultimedia.IMAGE : TipoMultimedia.VIDEO);
        media.setPublicacion(publicacion);

        multimediaPublicacionRepository.save(media);

        return new MultimediaPublicacionResponseDTO(
                media.getId(),
                media.getRuta(),
                media.getTipo()
        );
    }
}
