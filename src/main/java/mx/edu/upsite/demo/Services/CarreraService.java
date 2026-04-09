package mx.edu.upsite.demo.Services;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import mx.edu.upsite.demo.DTOs.Request.CarreraRequestDTO;
import mx.edu.upsite.demo.DTOs.Response.CarreraResponseDTO;
import mx.edu.upsite.demo.Entities.Carrera;
import mx.edu.upsite.demo.Exceptions.BadRequestException;
import mx.edu.upsite.demo.Exceptions.ConflictException;
import mx.edu.upsite.demo.Exceptions.ResourceNotFoundException;
import mx.edu.upsite.demo.Repositories.CarreraRepository;
import org.apache.catalina.LifecycleState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CarreraService {

    private final CarreraRepository carreraRepository;

    public List<CarreraResponseDTO> getAll(){
        return carreraRepository.findAll().stream().map(this::toDTO).toList();
    }
    public CarreraResponseDTO getById(Integer id){
        return carreraRepository.findById(id).map(this::toDTO)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro a una carrera con id "+id));
    }
    public CarreraResponseDTO getByNombre(String nombre){
        return carreraRepository.findByNombreIgnoreCase(nombre).map(this::toDTO)
                .orElseThrow(()->new ResourceNotFoundException("No se encontro la carrera "+nombre));
    }
    @Transactional
    public void updateCarreraById(Integer id, CarreraRequestDTO dto) {

        Carrera carreraExistente = carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe una carrera con id " + id));


        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la carrera no puede estar vacío.");
        }


        carreraExistente.setNombre(dto.nombre().trim());


        carreraRepository.save(carreraExistente);
    }
    @Transactional
    public void eliminarCarreraById(Integer id) {
        if (!carreraRepository.existsById(id)) {
            throw new ResourceNotFoundException("No se puede eliminar: La carrera con ID " + id + " no existe.");
        }
        try {
            carreraRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("No se puede eliminar la carrera porque tiene registros asociados (alumnos o grupos).");
        }
    }
    @Transactional
    public CarreraResponseDTO crear(CarreraRequestDTO dto) {

        if (carreraRepository.existsByNombreIgnoreCase(dto.nombre().trim())) {
            throw new ConflictException("La carrera '" + dto.nombre() + "' ya existe en el sistema.");
        }
        if (dto.nombre() == null || dto.nombre().trim().isEmpty()) {
            throw new BadRequestException("El nombre de la carrera es obligatorio.");
        }
        Carrera nuevaCarrera = new Carrera();
        nuevaCarrera.setNombre(dto.nombre().trim());
        Carrera guardada = carreraRepository.save(nuevaCarrera);
        return toDTO(guardada);
    }


    public CarreraResponseDTO toDTO(Carrera carrera){
        return new CarreraResponseDTO(
                carrera.getId(),
                carrera.getNombre()

        );
    }
}
