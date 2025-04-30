package pe.com.sammis.vale.services.interfaces;

import pe.com.sammis.vale.models.Empleado;

import java.util.List;
import java.util.Optional;


public interface IEmpleadoService {
    List<Empleado> findAllActive();
    Optional<Empleado> findById(Long id);
    Empleado save(Empleado empleado);
    Empleado update(Long id, Empleado empleado);
    void deleteById(Long id);
    Optional<Empleado> findByDni(String dni);
    String consultaSunat(String dni);
}


