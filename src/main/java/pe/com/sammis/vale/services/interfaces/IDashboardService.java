package pe.com.sammis.vale.services.interfaces;

public interface IDashboardService {

    String countTotalAsistencias();

    String countAsistenciasHoy();

    String countAsistenciasPorEmpleado(Long empleadoId);

    String countEmpleadosActivos();

    String obtenerPesoTablaAsistencias();

    String countFechasTomadas();
}
