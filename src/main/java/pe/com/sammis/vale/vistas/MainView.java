package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;


@CssImport("./themes/mi-tema/styles.css")
@Route(value = "", layout = MainLayout.class)


public class MainView extends VerticalLayout {

    public MainView() {
        addClassName("main-view");
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);


        add(new com.vaadin.flow.component.html.H2("Bienvenido a la vista MainView"));

    }
}
