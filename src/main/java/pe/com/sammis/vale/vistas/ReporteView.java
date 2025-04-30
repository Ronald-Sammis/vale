package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.implement.AsistenciaServiceImpl;
import pe.com.sammis.vale.util.ComponentsUtils;

import java.time.LocalDate;
import java.util.Map;

@Route("reporte")
public class ReporteView extends VerticalLayout {

    private final AsistenciaServiceImpl asistenciaService;

    private DatePicker fechaInicio = new DatePicker("Fecha de inicio");
    private DatePicker fechaFin = new DatePicker("Fecha de fin");
    private Button buscarButton;
    private TextField searchField = new TextField("Buscar Empleado");

    private Grid<Empleado> grid = new Grid<>(Empleado.class);

    @Autowired
    public ReporteView(AsistenciaServiceImpl asistenciaService) {
        this.asistenciaService = asistenciaService;

        setUpToolbar();
        setUpGrid();
        setUpForm();





    }

    private void setUpGrid() {
        // Configurar el Grid de Empleados
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        grid.setColumns("nombre", "apellido", "dni", "estado");
        grid.setHeight("550px");

        // Fijar la columna de "nombre" a la izquierda (como en Excel)
        grid.getColumnByKey("nombre").setFrozen(true);

        // Alineamos todos los componentes al final (derecha)

        // Botón de buscar
        buscarButton.addClickListener(event -> buscarAsistencias());

        // Añadir componentes a la vista
        add( grid);
    }

    private void setUpForm() {
    }

    private void setUpToolbar() {
        buscarButton = ComponentsUtils.createSearchButton(this::buscarAsistencias);
        HorizontalLayout toolbar = new HorizontalLayout(fechaInicio, fechaFin, searchField, buscarButton);
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        add(toolbar);
    }

    private void buscarAsistencias() {
        LocalDate fechaInicioValor = fechaInicio.getValue();
        LocalDate fechaFinValor = fechaFin.getValue();

        if (fechaInicioValor == null || fechaFinValor == null) {
            Notification.show("Por favor, seleccione ambas fechas.");
            return;
        }

        // Obtener los datos de las asistencias agrupadas por empleado y fecha
        Map<Empleado, Map<LocalDate, TipoAsistencia>> asistencias = asistenciaService.findByFechaBetween2(fechaInicioValor, fechaFinValor);

        // Construir el grid dinámicamente
        grid.removeAllColumns();
        grid.addColumn(Empleado::getNombre).setHeader("Empleado").setWidth("150px").setSortable(true).setFrozen(true); // Fijar columna

        // Agregar una columna para cada fecha en el rango
        for (LocalDate fecha : obtenerRangoFechas(fechaInicioValor, fechaFinValor)) {
            final LocalDate fechaFinal = fecha;
            grid.addColumn(new ComponentRenderer<>(empleado -> {
                        Map<LocalDate, TipoAsistencia> asistenciasPorFecha = asistencias.get(empleado);
                        TipoAsistencia tipo = asistenciasPorFecha != null ? asistenciasPorFecha.get(fechaFinal) : null;

                        Span span = new Span(tipo != null ? tipo.getAlias() : "");

                        // Obtener el color hexadecimal
                        String color = tipo != null ? tipo.getColorHex() : "#f0f0f0"; // color gris claro por defecto

                        // Verificar que el color tenga el prefijo '#', de lo contrario, agregarlo
                        if (!color.startsWith("#")) {
                            color = "#" + color;
                        }

                        // Establecer el estilo para la celda con el color de fondo y el tamaño de la letra
                        span.getStyle()
                                .set("background-color", color)
                                .set("padding", "4px 6px")
                                .set("border-radius", "6px")
                                .set("color", "white")  // Texto blanco
                                .set("text-align", "center") // Alineación centrada
                                .set("font-size", "11px"); // Tamaño de la letra en 11px

                        return span;
                    })).setHeader(String.valueOf(fecha.getDayOfMonth()))  // Mostrar solo el número del día
                    .setWidth("25px");  // Hacer la columna más angosta
        }

        grid.setItems(asistencias.keySet());
    }

    private Iterable<LocalDate> obtenerRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        // Generar el rango de fechas entre fechaInicio y fechaFin
        return fechaInicio.datesUntil(fechaFin.plusDays(1)).toList();
    }
}
