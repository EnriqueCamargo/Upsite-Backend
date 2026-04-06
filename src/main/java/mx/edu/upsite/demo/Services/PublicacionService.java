package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Response.MultimediaPublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.PublicacionResponseDTO;
import mx.edu.upsite.demo.DTOs.Response.UsuarioResponseDTO;
import mx.edu.upsite.demo.Entities.Publicacion;
import mx.edu.upsite.demo.Entities.Usuario;
import mx.edu.upsite.demo.Enums.Importancia;
import mx.edu.upsite.demo.Enums.Moderacion;
import mx.edu.upsite.demo.Repositories.MultimediaPublicacionRepository;
import mx.edu.upsite.demo.Repositories.PublicacionRepository;
import mx.edu.upsite.demo.Repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

    @Service
    @RequiredArgsConstructor
    public class PublicacionService {

        private final PublicacionRepository publicacionRepository;

        public List<PublicacionResponseDTO> getFeed(Integer carrera, Importancia importancia) {
            String importanciaStr = importancia != null ? importancia.name() : null;
            return publicacionRepository.findFeed(carrera, importanciaStr)
                    .stream()
                    .map(this::toDTO)
                    .toList();
        }

        private PublicacionResponseDTO toDTO(Publicacion p) {
            List<MultimediaPublicacionResponseDTO> multimedia = p.getMultimedia()
                    .stream()
                    .map(m -> new MultimediaPublicacionResponseDTO(
                            m.getId(),
                            m.getRuta(),
                            m.getTipo()
                    ))
                    .toList();

            UsuarioResponseDTO usuario = new UsuarioResponseDTO(
                    p.getUsuario().getId(),
                    p.getUsuario().getNombres(),
                    p.getUsuario().getApellidos(),
                    p.getUsuario().getGrupo() != null ? p.getUsuario().getGrupo().getNombre() : null,
                    p.getUsuario().getGrupo() != null && p.getUsuario().getGrupo().getCarrera() != null
                            ? p.getUsuario().getGrupo().getCarrera().getNombre() : null,
                    p.getUsuario().getFotoPerfil(),
                    p.getUsuario().getEmail(),
                    p.getUsuario().getRol(),
                    p.getUsuario().getMatricula(),
                    null,
                    null,
                    null
            );

            return new PublicacionResponseDTO(
                    p.getId(),
                    p.getTexto(),
                    p.getImportancia(),
                    multimedia,
                    usuario,
                    0L,
                    0L,
                    false
            );
        }
    }