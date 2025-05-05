package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.component.icon.Icon;


public class MainLayout extends AppLayout {


    public MainLayout() {

        createHeader();
        createSidebar();
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();
        H2 logo = new H2("VALE");
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

        // Links de navegación con iconos
        RouterLink portalView = new RouterLink("Portal", MainView.class);
        portalView.addComponentAsFirst(new Icon(VaadinIcon.HOME));
        RouterLink empleadoView = new RouterLink("Empleados", EmpleadoView.class);
        empleadoView.addComponentAsFirst(new Icon(VaadinIcon.USER));
        RouterLink tipoView = new RouterLink("Tipos de asistencia", TipoAsistenciaView.class);
        tipoView.addComponentAsFirst(new Icon(VaadinIcon.LIST));
        RouterLink asistencia = new RouterLink("Asistencias", AsistenciaView.class);
        asistencia.addComponentAsFirst(new Icon(VaadinIcon.CHECK_SQUARE_O));
        RouterLink dashBoardView = new RouterLink("DashBoard", DashboardView.class);
        dashBoardView.addComponentAsFirst(new Icon(VaadinIcon.CHART));


        portalView.addClassName("sidebar-link");
        empleadoView.addClassName("sidebar-link");
        tipoView.addClassName("sidebar-link");
        asistencia.addClassName("sidebar-link");
        dashBoardView.addClassName("sidebar-link");




        sidebar.add( portalView, empleadoView,tipoView,asistencia,dashBoardView);

        addToDrawer(sidebar);
    }
}