package pe.com.sammis.vale.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;

import java.util.Optional;

public interface TipoAsistenciaRepository extends JpaRepository<TipoAsistencia,Long> {
    Optional<TipoAsistencia> findByNombre(String nombre);
}
