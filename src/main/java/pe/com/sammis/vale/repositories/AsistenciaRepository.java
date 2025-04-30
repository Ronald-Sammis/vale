package pe.com.sammis.vale.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia,Long> {

    List<Asistencia> findByFechaBetween(LocalDate inicio, LocalDate fin);
    boolean existsByFecha(LocalDate fecha);
    @Query("SELECT DISTINCT a.fecha FROM Asistencia a ORDER BY a.fecha DESC")
    List<LocalDate> findDistinctFechas();
    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.empleado " +
            "LEFT JOIN FETCH a.tipoAsistencia " +
            "WHERE a.fecha = :fecha")
    List<Asistencia> findByFecha(@Param("fecha") LocalDate fecha);


    @Modifying
    @Transactional
    @Query("DELETE FROM Asistencia a WHERE a.fecha = :fecha")
    void deleteByFecha(@Param("fecha") LocalDate fecha);

    boolean existsByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);
    Asistencia findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);


    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.empleado " +
            "JOIN FETCH a.tipoAsistencia " +
            "WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin")
    List<Asistencia> findByFechaBetween2(@Param("fechaInicio") LocalDate fechaInicio,
                                                @Param("fechaFin") LocalDate fechaFin);



    long countByFecha(LocalDate fecha);

    long countByEmpleado_Id(Long empleadoId);

    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.fecha = :fecha AND a.empleado.id = :empleadoId")
    long countByFechaAndEmpleadoId(LocalDate fecha, Long empleadoId);




}
