package mx.edu.upsite.demo.Services;

import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.GrupoRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.GrupoResponseDTO;
import mx.edu.upsite.demo.Entities.Carrera;
import mx.edu.upsite.demo.Entities.Grupo;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.CarreraRepository;
import mx.edu.upsite.demo.Repositories.GrupoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrupoService {
    private final GrupoRepository grupoRepository;
    private final CarreraRepository carreraRepository;

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> getAll(){
        return grupoRepository.findAll().stream()
                .map(this::toDto)
                .sorted(this::compararGrupos)
                .toList();
    }
    @Transactional(readOnly = true)
    public GrupoResponseDTO getById(Integer id){
        return grupoRepository.findById(id).map(this::toDto)
                .orElseThrow(()-> new ResourceNotFoundException("Grupo no encontrado con id "+id));
    }
    @Transactional(readOnly = true)
    public GrupoResponseDTO getByNombre(String nombre){
        return grupoRepository.findByNombreIgnoreCase(nombre).map(this::toDto)
                .orElseThrow(()->new ResourceNotFoundException("No existe grupo de nombre "+nombre));
    }
    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> getAllByCarreraId(Integer id){
        if(carreraRepository.existsById(id)){
            return grupoRepository.findAllByCarreraId(id).stream()
                    .map(this::toDto)
                    .sorted(this::compararGrupos)
                    .toList();
        }else{
            throw new ResourceNotFoundException("No existe una carrera con el id "+id);
        }
    }

    @Transactional(readOnly = true)
    public List<GrupoResponseDTO> getAllByCarreraIds(List<Integer> ids){
        return grupoRepository.findAllByCarreraIdIn(ids).stream()
                .map(this::toDto)
                .sorted(this::compararGrupos)
                .toList();
    }

    private int compararGrupos(GrupoResponseDTO g1, GrupoResponseDTO g2) {
        try {
            String[] s1 = g1.nombre().split("-");
            String[] s2 = g2.nombre().split("-");
            int n1 = Integer.parseInt(s1[0]);
            int n2 = Integer.parseInt(s2[0]);
            if (n1 != n2) return Integer.compare(n1, n2);
            if (s1.length > 1 && s2.length > 1) {
                return Integer.compare(Integer.parseInt(s1[1]), Integer.parseInt(s2[1]));
            }
        } catch (Exception e) {
            return g1.nombre().compareTo(g2.nombre());
        }
        return g1.nombre().compareTo(g2.nombre());
    }

    @Transactional
    public GrupoResponseDTO crear(GrupoRequestDTO dto) {
        // 1. Validar que el nombre no sea nulo o vacío
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre del grupo no puede estar vacío.");
        }

        // 2. Buscar la carrera (Si no existe, no podemos crear el grupo)
        Carrera carrera = carreraRepository.findById(dto.idCarrera())
                .orElseThrow(() -> new ResourceNotFoundException("No se puede crear el grupo: La carrera con ID " + dto.idCarrera() + " no existe."));

        // 3. Evitar duplicados del mismo nombre en LA MISMA carrera
        if (grupoRepository.existsByNombreIgnoreCaseAndCarreraId(dto.nombre().trim(), dto.idCarrera())) {
            throw new ConflictException("El grupo '" + dto.nombre() + "' ya existe para la carrera " + carrera.getNombre());
        }

        Grupo nuevoGrupo = new Grupo();
        nuevoGrupo.setNombre(dto.nombre().trim());
        nuevoGrupo.setCarrera(carrera);

        return toDto(grupoRepository.save(nuevoGrupo));
    }

    @Transactional
    public GrupoResponseDTO actualizar(Integer id, GrupoRequestDTO dto) {
        // 1. Verificar si el grupo existe
        Grupo grupoExistente = grupoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el grupo con ID " + id));

        // 2. Si se envía un nombre, actualizarlo
        if (dto.nombre() != null && !dto.nombre().trim().isEmpty()) {
            grupoExistente.setNombre(dto.nombre().trim());
        }

        // 3. Si se envía una carrera, validar y actualizar
        if (dto.idCarrera() != null) {
            Carrera carrera = carreraRepository.findById(dto.idCarrera())
                    .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con ID " + dto.idCarrera()));
            grupoExistente.setCarrera(carrera);
        }

        return toDto(grupoRepository.save(grupoExistente));
    }

    @Transactional
    public void eliminar(Integer id) {
        // 1. Verificamos si existe antes de intentar borrar
        if (!grupoRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: El grupo con ID " + id + " no existe.");
        }

        try {
            // 2. Eliminación física
            grupoRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // 3. Blindaje por si el grupo ya tiene alumnos asignados en el futuro
            throw new ConflictException("No se puede eliminar el grupo porque tiene registros asociados (como alumnos o horarios).");
        }
    }
    public GrupoResponseDTO toDto(Grupo grupo){
        return new GrupoResponseDTO(
                grupo.getId(),
                grupo.getNombre(),
                grupo.getCarrera().getNombre()
        );
    }
}
