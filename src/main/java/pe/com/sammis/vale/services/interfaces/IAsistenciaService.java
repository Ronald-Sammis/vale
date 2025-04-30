package pe.com.sammis.vale.services.interfaces;

import org.springframework.data.repository.query.Param;
import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface IAsistenciaService {
    List<LocalDate> findDistinctFechas();
    boolean existsByFecha(LocalDate fecha);
    List<Asistencia> findByFecha(LocalDate fecha);
    Asistencia save(Asistencia asistencia);
    void deleteByFecha(LocalDate fecha);
    boolean existsByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);
    void saveAll(List<Asistencia> asistencias);
    Asistencia findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);
    List<Asistencia> findAll();
    Map<Empleado, Map<LocalDate, TipoAsistencia>> findByFechaBetween2(LocalDate fechaInicio, LocalDate fechaFin);
}


