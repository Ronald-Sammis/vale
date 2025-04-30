package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Component;
import pe.com.sammis.vale.services.interfaces.IDashboardService;

@Route(value = "dashBoard", layout = MainLayout.class)
@UIScope
@Component
public class DashboardView extends VerticalLayout {

    private final IDashboardService dashboardService;

    public DashboardView(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;

        setSpacing(true);
        setPadding(true);

        H2 titulo = new H2("Resumen de Asistencias");

        HorizontalLayout layoutTarjetas = new HorizontalLayout();
        layoutTarjetas.setSpacing(true);

        // Tarjetas con datos actuales
        Span totalAsistencias = crearTarjeta("Total Asistencias", dashboardService.countTotalAsistencias());
        Span asistenciasHoy = crearTarjeta("Asistencias Hoy", dashboardService.countAsistenciasHoy());
        Span pesoTablaAsistencias = crearTarjeta("Peso Tabla Asistencias", dashboardService.obtenerPesoTablaAsistencias());
        Span totalFechasTomadas = crearTarjeta("Fechas Tomadas", dashboardService.countFechasTomadas()); // ✅ Nueva tarjeta

        // Tarjeta empleados activos
        Span totalEmpleadosActivos = crearTarjeta("Empleados Activos", dashboardService.countEmpleadosActivos());

        layoutTarjetas.add(totalAsistencias, asistenciasHoy, totalEmpleadosActivos, pesoTablaAsistencias, totalFechasTomadas);

        add(titulo, layoutTarjetas);
    }

    // Método modificado para recibir String en lugar de double
    private Span crearTarjeta(String titulo, String valor) {
        Span tarjeta = new Span(titulo + ": " + valor);
        tarjeta.getStyle().set("padding", "1rem")
                .set("border", "1px solid #ccc")
                .set("border-radius", "8px")
                .set("background-color", "#f9f9f9")
                .set("font-size", "1.2rem")
                .set("min-width", "150px")
                .set("text-align", "center")
                .set("box-shadow", "2px 2px 5px rgba(0,0,0,0.1)");

        return tarjeta;
    }
}
