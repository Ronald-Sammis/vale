package pe.com.sammis.vale.services.implement;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.repositories.EmpleadoRepository;
import pe.com.sammis.vale.repositories.TipoAsistenciaRepository;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;

import java.util.List;
import java.util.Optional;

@Service
public class TipoAsistenciaServiceImpl implements ITipoAsistenciaService {

    private final TipoAsistenciaRepository tipoAsistenciaRepository;
    public TipoAsistenciaServiceImpl(TipoAsistenciaRepository tipoAsistenciaRepository) {
        this.tipoAsistenciaRepository = tipoAsistenciaRepository;
    }

    @Override
    public List<TipoAsistencia> findAll() {
        return tipoAsistenciaRepository.findAll();
    }

    @Override
    public Optional<TipoAsistencia> findById(Long id) {
        return tipoAsistenciaRepository.findById(id);
    }

    @Override
    public TipoAsistencia save(TipoAsistencia empleado) {
        return tipoAsistenciaRepository.save(empleado);
    }

    @Override
    public TipoAsistencia update(Long id, TipoAsistencia empleado) {
        return tipoAsistenciaRepository.findById(id).map(emp -> {
            emp.setNombre(empleado.getNombre());
            return tipoAsistenciaRepository.save(emp);
        }).orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con ID: " + id));
    }

    @Override
    public void deleteById(Long id) {
        tipoAsistenciaRepository.deleteById(id);
    }

    @Override
    public Optional<TipoAsistencia> findByNombre(String nombre) {
        return tipoAsistenciaRepository.findByNombre(nombre);
    }


}
