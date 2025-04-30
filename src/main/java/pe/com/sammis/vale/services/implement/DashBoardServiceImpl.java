package pe.com.sammis.vale.services.implement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pe.com.sammis.vale.repositories.AsistenciaRepository;
import pe.com.sammis.vale.repositories.EmpleadoRepository;
import pe.com.sammis.vale.services.interfaces.IDashboardService;

import java.time.LocalDate;

@Service
public class DashBoardServiceImpl implements IDashboardService {


    private final AsistenciaRepository asistenciaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final JdbcTemplate jdbcTemplate;

    public DashBoardServiceImpl(AsistenciaRepository asistenciaRepository, EmpleadoRepository empleadoRepository,JdbcTemplate jdbcTemplate) {
        this.asistenciaRepository = asistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public String countTotalAsistencias() {
        long totalAsistencias = asistenciaRepository.count();
        return String.valueOf(totalAsistencias);  // Convertir el valor a String
    }

    @Override
    public String countAsistenciasHoy() {
        long asistenciasHoy = asistenciaRepository.countByFecha(LocalDate.now());
        return String.valueOf(asistenciasHoy);  // Convertir el valor a String
    }

    @Override
    public String countAsistenciasPorEmpleado(Long empleadoId) {
        long asistenciasPorEmpleado = asistenciaRepository.countByEmpleado_Id(empleadoId);
        return String.valueOf(asistenciasPorEmpleado);  // Convertir el valor a String
    }

    @Override
    public String countEmpleadosActivos() {
        long empleadosActivos = empleadoRepository.countByEstadoTrue();
        return String.valueOf(empleadosActivos);  // Convertir el valor a String
    }

    @Override
    public String obtenerPesoTablaAsistencias() {
        String sql = "SELECT pg_size_pretty(pg_total_relation_size('tb_asistencias'))";
        return jdbcTemplate.queryForObject(sql, String.class);  // No es necesario cambiar nada aquí
    }

    @Override
    public String countFechasTomadas() {
        String sql = "SELECT COUNT(DISTINCT fecha) FROM tb_asistencias";  // Contamos las fechas distintas
        return String.valueOf(jdbcTemplate.queryForObject(sql, Long.class));  // Convertimos a String
    }




}
