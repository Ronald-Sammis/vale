package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;
import pe.com.sammis.vale.util.TipoAsistenciaRadioButtonView;

import java.util.List;

@Route("grid-con-radiobuttons")
public class PruebasView extends VerticalLayout {

    private final IEmpleadoService empleadoService;
    private final ITipoAsistenciaService tipoAsistenciaService;

    @Autowired
    public PruebasView(IEmpleadoService empleadoService, ITipoAsistenciaService tipoAsistenciaService) {
        this.empleadoService = empleadoService;
        this.tipoAsistenciaService = tipoAsistenciaService;

        List<Empleado> empleados = empleadoService.findAllActive();
        List<TipoAsistencia> tiposAsistencia = tipoAsistenciaService.findAll();

        Grid<Empleado> grid = new Grid<>(Empleado.class, false); // sin columnas automáticas

        grid.addColumn(Empleado::getNombre).setHeader("Nombre").setSortable(true);
        // Columna con el componente personalizado
        grid.addComponentColumn(empleado -> {
            TipoAsistenciaRadioButtonView radioView = new TipoAsistenciaRadioButtonView();
            radioView.setItems(tiposAsistencia);
            return radioView;
        }).setHeader("Tipo de Asistencia");

        grid.setItems(empleados);

        add(grid);
    }
}
