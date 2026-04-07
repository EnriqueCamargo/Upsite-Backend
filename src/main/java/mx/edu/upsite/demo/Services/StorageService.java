package mx.edu.upsite.demo.Services;

import mx.edu.upsite.demo.Enums.TipoMultimedia;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${storage.location}")
    private String storageLocation;

    public String guardar(MultipartFile archivo, TipoMultimedia tipo) throws IOException {
        // 1. Blindaje: Archivo básico
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("No se puede procesar un archivo vacío.");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            throw new BadRequestException("El archivo no tiene una extensión válida.");
        }

        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".") + 1).toLowerCase();

        // 2. Blindaje por TipoMultimedia (Reglas de negocio)
        validarExtension(extension, tipo);

        // 3. Preparación de rutas seguras
        Path directorioRaiz = Paths.get(storageLocation).toAbsolutePath().normalize();
        if (!Files.exists(directorioRaiz)) {
            Files.createDirectories(directorioRaiz);
        }

        // 4. Generación de nombre único para evitar colisiones
        String nombreArchivo = UUID.randomUUID().toString() + "." + extension;
        Path destino = directorioRaiz.resolve(nombreArchivo).normalize();

        // 5. Blindaje Anti-Path Traversal
        if (!destino.startsWith(directorioRaiz)) {
            throw new BadRequestException("Intento de acceso ilegal al sistema de archivos.");
        }

        // 6. Guardado físico
        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        return "/uploads/" + nombreArchivo;
    }

    private void validarExtension(String extension, TipoMultimedia tipo) {
        switch (tipo) {
            case IMAGE -> {
                if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension))
                    throw new BadRequestException("Extensión de imagen no permitida.");
            }
            case VIDEO -> {
                if (!List.of("mp4", "mov", "avi").contains(extension))
                    throw new BadRequestException("Extensión de video no permitida.");
            }
            case DOCUMENT -> {
                if (!List.of("pdf", "docx", "pptx", "txt").contains(extension))
                    throw new BadRequestException("Extensión de documento no permitida.");
            }
            default -> throw new BadRequestException("Tipo de multimedia no soportado.");
        }
    }
}
