package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;


public class MainLayout extends AppLayout {


    public MainLayout() {
        createHeader();
        createSidebar();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        H1 logo = new H1("VALE");
        logo.getStyle().set("margin", "0");

        HorizontalLayout header = new HorizontalLayout(toggle, logo);
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        addToNavbar(header);
    }

    private void createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.addClassName("custom-sidebar");
        sidebar.setSizeFull();
        sidebar.setPadding(true);
        sidebar.setSpacing(true);

        // Links de navegación
        RouterLink portalView = new RouterLink("Portal", MainView.class);
        RouterLink empleadoView = new RouterLink("Gestión de empleados", EmpleadoView.class);
        RouterLink tipoView = new RouterLink("Gestión de tipos de asistencia", TipoAsistenciaView.class);
        RouterLink asistencia = new RouterLink("Gestión de asistencias", AsistenciaView.class);
        RouterLink dashBoardView = new RouterLink("DashBoard",DashboardView.class);





        sidebar.add( portalView, empleadoView,tipoView,asistencia,dashBoardView);

        addToDrawer(sidebar);
    }
}
