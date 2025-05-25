package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;

import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.interfaces.IAsistenciaService;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;
import pe.com.sammis.vale.util.TipoAsistenciaRadioButtonView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("editar-asistencia")
public class EditarAsistencia extends VerticalLayout implements HasUrlParameter<String> {

    private final IEmpleadoService empleadoService;
    private final IAsistenciaService asistenciaService;
    private final ITipoAsistenciaService tipoAsistenciaService;

    private List<Empleado> empleadosCache;
    private List<TipoAsistencia> tiposAsistenciaCache;

    private LocalDate fechaSeleccionada;
    private final Grid<Empleado> grid = new Grid<>(Empleado.class, false);
    private final VerticalLayout formLayout = new VerticalLayout();
    private final H4 titulo = new H4();
    private final Checkbox selectAllPCheckbox = new Checkbox("Seleccionar todos con 'P'");
    private final Map<Long, Long> asistenciaSeleccionada = new HashMap<>();
    private final Map<Long, TipoAsistenciaRadioButtonView> radioButtonsPorEmpleado = new HashMap<>();

    public EditarAsistencia(IEmpleadoService empleadoService, IAsistenciaService asistenciaService, ITipoAsistenciaService tipoAsistenciaService) {
        this.empleadoService = empleadoService;
        this.asistenciaService = asistenciaService;
        this.tipoAsistenciaService = tipoAsistenciaService;

        setSizeFull();

        this.empleadosCache = empleadoService.findAllActive();
        this.tiposAsistenciaCache = tipoAsistenciaService.findAll();

        setUpToolbar();
        setUpGrid();
        add(formLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        List<String> fechaParam = event.getLocation().getQueryParameters().getParameters().get("fecha");

        if (fechaParam != null && !fechaParam.isEmpty()) {
            this.fechaSeleccionada = LocalDate.parse(fechaParam.get(0));
            Notification.show("Editando asistencia del " + fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        } else {
            Notification.show("No se recibió una fecha válida.");
            return;
        }

        titulo.setText("Editar asistencia del: " + fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        titulo.getStyle().set("margin-top", "20px");
        if (!formLayout.getChildren().anyMatch(c -> c.equals(titulo))) {
            formLayout.addComponentAtIndex(0, titulo);
        }

        updateGrid();
    }

    private void setUpToolbar() {
        Button saveButton = new Button("Guardar", e -> save());
        Button cancelButton = new Button("Cancelar", e -> UI.getCurrent().navigate("asistencia"));

        selectAllPCheckbox.addValueChangeListener(event -> {
            if (event.getValue()) {
                TipoAsistencia tipoP = tiposAsistenciaCache.stream()
                        .filter(t -> t.getAlias().equalsIgnoreCase("P"))
                        .findFirst()
                        .orElse(null);

                if (tipoP != null) {
                    radioButtonsPorEmpleado.forEach((id, radioView) -> {
                        radioView.setValue(tipoP);
                        asistenciaSeleccionada.put(id, tipoP.getId());
                    });
                    // Refrescar el grid para que los cambios se vean en UI:
                    grid.getDataProvider().refreshAll();
                }
            } else {
                // Si deselecciona el checkbox, podrías dejarlo sin efecto o implementar lógica aquí si quieres
            }
        });

        HorizontalLayout toolbar = new HorizontalLayout(saveButton, cancelButton, selectAllPCheckbox);
        toolbar.setSpacing(true);
        add(toolbar);
    }

    private void setUpGrid() {
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        grid.setWidth("750px");

        grid.addColumn(new ComponentRenderer<>(empleado -> {
            Div div = new Div();
            div.getElement().setProperty("innerHTML", "<b>" + empleado.getApellido() + "</b> " + empleado.getNombre());
            return div;
        })).setHeader("Nombre Completo").setAutoWidth(true);

        grid.addColumn(new ComponentRenderer<>(empleado -> {
            TipoAsistenciaRadioButtonView radioView = new TipoAsistenciaRadioButtonView();
            radioView.setItems(tiposAsistenciaCache);

            Long tipoAsistenciaIdGuardado = asistenciaSeleccionada.get(empleado.getId());
            if (tipoAsistenciaIdGuardado != null) {
                tiposAsistenciaCache.stream()
                        .filter(ta -> ta.getId().equals(tipoAsistenciaIdGuardado))
                        .findFirst()
                        .ifPresent(radioView::setValue);
            } else {
                tiposAsistenciaCache.stream()
                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                        .findFirst()
                        .ifPresent(sr -> {
                            radioView.setValue(sr);
                            asistenciaSeleccionada.put(empleado.getId(), sr.getId());
                        });
            }

            radioView.getChildren().forEach(component -> {
                if (component instanceof Span span) {
                    span.addClickListener(event -> {
                        tiposAsistenciaCache.stream()
                                .filter(ta -> ta.getAlias().equals(span.getText()))
                                .findFirst()
                                .ifPresent(tipoSeleccionado -> {
                                    radioView.setValue(tipoSeleccionado);
                                    asistenciaSeleccionada.put(empleado.getId(), tipoSeleccionado.getId());
                                });
                    });
                }
            });

            radioButtonsPorEmpleado.put(empleado.getId(), radioView);
            return radioView;
        })).setHeader("Tipo de Asistencia").setWidth("650px").setFlexGrow(0);

        formLayout.add(grid);
    }

    private void updateGrid() {
        List<Asistencia> asistencias = asistenciaService.findByFecha(fechaSeleccionada);

        asistenciaSeleccionada.clear();
        radioButtonsPorEmpleado.clear();

        for (Asistencia asistencia : asistencias) {
            asistenciaSeleccionada.put(asistencia.getEmpleado().getId(), asistencia.getTipoAsistencia().getId());
        }

        // Solo actualizamos los items, NO tocamos las columnas:
        grid.setItems(empleadosCache);
        // Refresca para que la UI se actualice con la nueva data:
        grid.getDataProvider().refreshAll();
    }


    private void save() {
        List<Asistencia> asistencias = new ArrayList<>();
        Map<Long, Asistencia> existentes = asistenciaService.findByFecha(fechaSeleccionada)
                .stream()
                .collect(Collectors.toMap(a -> a.getEmpleado().getId(), a -> a));

        for (Empleado empleado : empleadosCache) {
            TipoAsistenciaRadioButtonView radioView = radioButtonsPorEmpleado.get(empleado.getId());
            TipoAsistencia seleccionada = radioView != null ? radioView.getValue() : null;

            if (seleccionada == null) continue;

            if (existentes.containsKey(empleado.getId())) {
                Asistencia existente = existentes.get(empleado.getId());
                if (!existente.getTipoAsistencia().getId().equals(seleccionada.getId())) {
                    existente.setTipoAsistencia(seleccionada);
                    asistencias.add(existente);
                }
            } else {
                Asistencia nueva = new Asistencia();
                nueva.setEmpleado(empleado);
                nueva.setTipoAsistencia(seleccionada);
                nueva.setFecha(fechaSeleccionada);
                asistencias.add(nueva);
            }
        }

        asistenciaService.saveAll(asistencias);
        Notification.show("Asistencias actualizadas", 3000, Notification.Position.TOP_CENTER);
        UI.getCurrent().navigate("asistencia");
    }
}
