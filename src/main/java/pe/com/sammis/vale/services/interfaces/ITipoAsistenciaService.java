package pe.com.sammis.vale.services.interfaces;

import org.springframework.data.repository.query.Param;
import pe.com.sammis.vale.models.TipoAsistencia;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ITipoAsistenciaService {
    List<TipoAsistencia> findAll();
    Optional<TipoAsistencia> findById(Long id);
    TipoAsistencia save(TipoAsistencia tipoAsistencia);
    TipoAsistencia update(Long id, TipoAsistencia tipoAsistencia);
    void deleteById(Long id);
    Optional<TipoAsistencia> findByNombre(String nombre);


}


