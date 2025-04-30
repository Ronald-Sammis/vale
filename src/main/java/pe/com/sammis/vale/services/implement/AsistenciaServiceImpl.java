package pe.com.sammis.vale.services.implement;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.repositories.AsistenciaRepository;
import pe.com.sammis.vale.repositories.EmpleadoRepository;
import pe.com.sammis.vale.services.interfaces.IAsistenciaService;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;

import java.time.LocalDate;
import java.util.*;

@Service
public class AsistenciaServiceImpl implements IAsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    public AsistenciaServiceImpl(AsistenciaRepository asistenciaRepository) {
        this.asistenciaRepository = asistenciaRepository;
    }


    @Override
    public List<LocalDate> findDistinctFechas() {
        return asistenciaRepository.findDistinctFechas();
    }

    @Override
    public boolean existsByFecha(LocalDate fecha) {
        return asistenciaRepository.existsByFecha(fecha);
    }

    @Override
    public List<Asistencia> findByFecha(LocalDate fecha) {
        return asistenciaRepository.findByFecha(fecha);
    }

    @Override
    public Asistencia save(Asistencia asistencia) {
        return asistenciaRepository.save(asistencia);
    }

    @Override
    public void deleteByFecha(LocalDate fecha) {
        asistenciaRepository.deleteByFecha(fecha);
    }

    @Override
    public boolean existsByEmpleadoAndFecha(Empleado empleado, LocalDate fecha) {
        return asistenciaRepository.existsByEmpleadoAndFecha(empleado, fecha);
    }

    @Override
    public void saveAll(List<Asistencia> asistencias) {
        asistenciaRepository.saveAll(asistencias);
    }

    @Override
    public Asistencia findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha) {
        return asistenciaRepository.findByEmpleadoAndFecha(empleado, fecha);
    }

    @Override
    public List<Asistencia> findAll() {
        return asistenciaRepository.findAll();
    }

    public Map<Empleado, Map<LocalDate, TipoAsistencia>> findByFechaBetween2(LocalDate fechaInicio, LocalDate fechaFin) {
        List<Asistencia> asistencias = asistenciaRepository.findByFechaBetween(fechaInicio, fechaFin);

        Map<Empleado, Map<LocalDate, TipoAsistencia>> mapa = new LinkedHashMap<>();

        for (Asistencia asistencia : asistencias) {
            Empleado empleado = asistencia.getEmpleado();
            LocalDate fecha = asistencia.getFecha();
            TipoAsistencia tipo = asistencia.getTipoAsistencia();

            mapa.computeIfAbsent(empleado, e -> new HashMap<>())
                    .put(fecha, tipo);
        }

        return mapa;
    }


}
