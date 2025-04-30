package pe.com.sammis.vale.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.com.sammis.vale.models.Empleado;

import java.util.List;
import java.util.Optional;

public interface EmpleadoRepository extends JpaRepository<Empleado,Long> {
    Optional<Empleado> findByDni(String dni);

    @Query("SELECT e FROM Empleado e WHERE e.estado = true")
    List<Empleado> findAllActive();
    long countByEstadoTrue();


}
