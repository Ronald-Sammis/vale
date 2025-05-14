package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.interfaces.IAsistenciaService;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;
import pe.com.sammis.vale.util.ComponentsUtils;
import pe.com.sammis.vale.util.TipoAsistenciaRadioButtonView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route("registrar-asistencia")
public class RegistrarAsistencia extends VerticalLayout implements HasUrlParameter<String> {

    private final IEmpleadoService empleadoService;
    private final Grid<Empleado> grid = new Grid<>(Empleado.class, false);
    private final VerticalLayout formLayout = new VerticalLayout();
    private final H4 titulo = new H4();
    private List<TipoAsistencia> tiposAsistenciaCache;
    // Usaremos este mapa para almacenar la selección por empleado ID
    private final Map<Long, Long> asistenciaSeleccionada = new HashMap<>();
    private final IAsistenciaService asistenciaService;
    private final ITipoAsistenciaService tipoAsistenciaService;
    private List<Empleado> empleadosCache;
    private LocalDate fechaSeleccionada;
    private final Checkbox selectAllPCheckbox = new Checkbox("Seleccionar todos con 'P'", false);
    private final Map<RadioButtonGroup<TipoAsistencia>, Map<Long, Span>> radioButtonSpanMap = new HashMap<>();
    private H4 getTitulo = new H4();
    private Button cancelButton = new Button("Cancelar");

    public RegistrarAsistencia(IEmpleadoService empleadoService, IAsistenciaService asistenciaService, ITipoAsistenciaService tipoAsistenciaService) {
        this.empleadoService = empleadoService;
        this.asistenciaService = asistenciaService;
        this.tipoAsistenciaService = tipoAsistenciaService;
        this.empleadosCache = empleadoService.findAllActive();
        setSizeFull();
        setUpToolbar();
        add(grid);
        updateGrid();
    }

    private void setUpToolbar() {
        cancelButton.addClickListener(e -> {
            cancel();
        });

        Button saveButton = ComponentsUtils.createSaveButton(this::save);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, selectAllPCheckbox);
        buttonLayout.setSpacing(true);

        add(buttonLayout);
    }
    private void cancel() {
        UI.getCurrent().navigate(AsistenciaView.class);
    }
    private void updateGrid() {
        if (tiposAsistenciaCache == null) {
            tiposAsistenciaCache = tipoAsistenciaService.findAll();
        }

        grid.setItems(empleadosCache);

        grid.addColumn(empleado -> {
                    String apellido = ComponentsUtils.capitalizeFirstLetter(empleado.getApellido());
                    String nombre = ComponentsUtils.capitalizeFirstLetter(empleado.getNombre());
                    return apellido + " " + nombre;
                }).setHeader("Nombre Completo")
                .setWidth("200px")
                .setFlexGrow(0)
                .setSortable(true);

        /*grid.addColumn(new ComponentRenderer<>(empleado -> {
            RadioButtonGroup<TipoAsistencia> radioGroup = new RadioButtonGroup<>();
            radioGroup.addClassName("mi-radio-group-pequeno");
            radioGroup.setItems(tiposAsistenciaCache);
            radioGroup.setItemLabelGenerator(TipoAsistencia::getAlias);

            Map<Long, Span> spanPorTipo = new HashMap<>();
            radioButtonSpanMap.put(radioGroup, spanPorTipo); // Almacena la relación

            radioGroup.setRenderer(new ComponentRenderer<>(tipo -> {
                Span span = new Span(tipo.getAlias());
                span.getStyle().set("padding", "3px 6px");
                span.getStyle().set("border-radius", "4px");
                span.getStyle().set("transition", "all 0.3s ease");
                spanPorTipo.put(tipo.getId(), span);
                return span;
            }));

            // Restaurar la selección si existe
            Long tipoAsistenciaIdGuardado = asistenciaSeleccionada.get(empleado.getId());
            if (tipoAsistenciaIdGuardado != null) {
                tiposAsistenciaCache.stream()
                        .filter(ta -> ta.getId().equals(tipoAsistenciaIdGuardado))
                        .findFirst()
                        .ifPresent(asistenciaPrevia -> {
                            radioGroup.setValue(asistenciaPrevia);
                            actualizarEstilo(spanPorTipo, asistenciaPrevia);
                        });
            } else {
                // Establecer el valor por defecto "SR" si no hay selección previa
                tiposAsistenciaCache.stream()
                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                        .findFirst()
                        .ifPresent(asistenciaSR -> {
                            radioGroup.setValue(asistenciaSR);
                            actualizarEstilo(spanPorTipo, asistenciaSR);
                            asistenciaSeleccionada.put(empleado.getId(), asistenciaSR.getId()); // Guardar la selección inicial
                        });
            }

            radioGroup.addValueChangeListener(event -> {
                if (event.getValue() != null) {
                    asistenciaSeleccionada.put(empleado.getId(), event.getValue().getId());
                    actualizarEstilo(spanPorTipo, event.getValue());
                } else {
                    asistenciaSeleccionada.remove(empleado.getId()); // Eliminar si se deselecciona (opcional)
                    actualizarEstilo(spanPorTipo, null);
                }
            });

            return radioGroup;
        })).setHeader("Tipo de Asistencia").setWidth("650px").setFlexGrow(0);*/

        grid.addColumn(new ComponentRenderer<>(empleado -> {
            TipoAsistenciaRadioButtonView radioView = new TipoAsistenciaRadioButtonView();
            radioView.setItems(tiposAsistenciaCache);

            // Buscar y establecer selección guardada
            Long tipoAsistenciaIdGuardado = asistenciaSeleccionada.get(empleado.getId());
            if (tipoAsistenciaIdGuardado != null) {
                tiposAsistenciaCache.stream()
                        .filter(ta -> ta.getId().equals(tipoAsistenciaIdGuardado))
                        .findFirst()
                        .ifPresent(radioView::setValue);
            } else {
                // Valor por defecto "SR"
                tiposAsistenciaCache.stream()
                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                        .findFirst()
                        .ifPresent(sr -> {
                            radioView.setValue(sr);
                            asistenciaSeleccionada.put(empleado.getId(), sr.getId());
                        });
            }

            // Listener para cambios de selección
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

            return radioView;
        })).setHeader("Tipo de Asistencia").setWidth("650px").setFlexGrow(0);


        grid.setHeight("380px");

        // CHECKBOX: "Seleccionar todos con 'P'"
        selectAllPCheckbox.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();

            Optional<TipoAsistencia> tipoP = tiposAsistenciaCache.stream()
                    .filter(ta -> "P".equalsIgnoreCase(ta.getAlias()))
                    .findFirst();

            Optional<TipoAsistencia> tipoSR = tiposAsistenciaCache.stream()
                    .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                    .findFirst();

            if (tipoP.isEmpty() || tipoSR.isEmpty()) return;

            grid.getDataProvider().fetch(new Query<>()).forEach(empleado -> {
                // Actualizar el mapa de selección directamente
                asistenciaSeleccionada.put(empleado.getId(), isChecked ? tipoP.get().getId() : tipoSR.get().getId());

                // Forzar la re-renderización de la fila para que el RadioButtonGroup se actualice
                grid.getDataProvider().refreshItem(empleado);
            });
        });
    }
    private void registrarAsistencia(Empleado empleado) {
        // Implementación de la lógica para registrar asistencia de un empleado.
    }
    private void setUpForm() {
        // Código para configurar el formulario (ya estaba implementado)
    }
    private void search(String s) {
        String filtro = s.trim().toLowerCase();

        if (filtro.isEmpty()) {
            updateGrid();
        } else {
            List<Empleado> empleadosFiltrados = empleadoService.findAllActive().stream()
                    .filter(emp -> emp.getDni().toLowerCase().contains(filtro)
                            || emp.getNombre().toLowerCase().contains(filtro)
                            || emp.getApellido().toLowerCase().contains(filtro))
                    .collect(Collectors.toList());

            grid.setItems(empleadosFiltrados);
        }
    }
    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();
        List<String> fechaParam = queryParameters.getParameters().get("fecha");

        if (fechaParam != null && !fechaParam.isEmpty()) {
            this.fechaSeleccionada = LocalDate.parse(fechaParam.get(0));
        } else {
            Notification.show("No se ha recibido una fecha válida.", 3000, Notification.Position.MIDDLE);
        }

        getTitulo.getStyle().set("margin-top", "20px");
        getTitulo.setText("Toma de asistencia del : " + fechaSeleccionada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        add(getTitulo);
    }
    private void actualizarEstilo(Map<Long, Span> spanPorTipo, TipoAsistencia seleccionada) {
        for (Map.Entry<Long, Span> entry : spanPorTipo.entrySet()) {
            Span span = entry.getValue();
            span.getStyle().remove("background-color");
            span.getStyle().remove("color");
        }

        if (seleccionada != null && spanPorTipo.containsKey(seleccionada.getId())) {
            Span spanSeleccionado = spanPorTipo.get(seleccionada.getId());
            String backgroundColor = seleccionada.getColorHex();
            spanSeleccionado.getStyle().set("background-color", backgroundColor);
            spanSeleccionado.getStyle().set("color", determineTextColor(backgroundColor));
        }
    }
    private String determineTextColor(String backgroundColor) {
        if (backgroundColor == null || backgroundColor.isEmpty()) {
            return "#000000";
        }
        int r = Integer.valueOf(backgroundColor.substring(1, 3), 16);
        int g = Integer.valueOf(backgroundColor.substring(3, 5), 16);
        int b = Integer.valueOf(backgroundColor.substring(5, 7), 16);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return luminance > 0.5 ? "#000000" : "#FFFFFF";
    }
    private void setUpTitle() {
        ComponentsUtils.setTitulo(this, RegistrarAsistencia.class, String.valueOf(fechaSeleccionada.format((DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
    }
    private void save() {
        List<Asistencia> asistenciasAGuardar = new ArrayList<>();
        List<Asistencia> asistenciasActualizadas = new ArrayList<>();

        // 1. Obtener todas las asistencias existentes para la fecha seleccionada
        Map<Long, Asistencia> existingAsistenciasMap = asistenciaService.findByFecha(fechaSeleccionada)
                .stream()
                .collect(Collectors.toMap(asistencia -> asistencia.getEmpleado().getId(), asistencia -> asistencia));

        // 2. Iterar sobre las selecciones guardadas
        for (Map.Entry<Long, Long> entry : asistenciaSeleccionada.entrySet()) {
            Long empleadoId = entry.getKey();
            Long tipoAsistenciaId = entry.getValue();

            Empleado empleado = empleadosCache.stream()
                    .filter(e -> e.getId().equals(empleadoId))
                    .findFirst()
                    .orElse(null);

            TipoAsistencia tipoAsistencia = tiposAsistenciaCache.stream()
                    .filter(ta -> ta.getId().equals(tipoAsistenciaId))
                    .findFirst()
                    .orElse(null);

            if (empleado != null && tipoAsistencia != null) {
                Asistencia existingAsistencia = existingAsistenciasMap.get(empleadoId);

                if (existingAsistencia != null) {
                    if (!existingAsistencia.getTipoAsistencia().equals(tipoAsistencia)) {
                        existingAsistencia.setTipoAsistencia(tipoAsistencia);
                        asistenciasActualizadas.add(existingAsistencia);
                    }
                } else {
                    Asistencia nuevaAsistencia = new Asistencia();
                    nuevaAsistencia.setEmpleado(empleado);
                    nuevaAsistencia.setTipoAsistencia(tipoAsistencia);
                    nuevaAsistencia.setFecha(fechaSeleccionada);
                    asistenciasAGuardar.add(nuevaAsistencia);
                }
            }
        }

        boolean savedOrUpdated = false;

        // 3. Guardar todas las nuevas asistencias en lote
        if (!asistenciasAGuardar.isEmpty()) {
            asistenciaService.saveAll(asistenciasAGuardar);
            Notification.show("Asistencias guardadas: " + asistenciasAGuardar.size(), 3000, Notification.Position.TOP_CENTER);
            savedOrUpdated = true;
        }

        // 4. Actualizar todas las asistencias existentes en lote
        if (!asistenciasActualizadas.isEmpty()) {
            asistenciaService.saveAll(asistenciasActualizadas);
            Notification.show("Asistencias actualizadas: " + asistenciasActualizadas.size(), 3000, Notification.Position.TOP_CENTER);
            savedOrUpdated = true;
        } else if (asistenciasAGuardar.isEmpty()) {
            Notification.show("No hay cambios para guardar.", 3000, Notification.Position.TOP_CENTER);
        }

        if (savedOrUpdated) {
            UI.getCurrent().navigate(AsistenciaView.class); // Redirige a AsistenciaView
        } else {
            updateGrid(); // Si no se guardaron cambios, podrías simplemente actualizar el grid o no hacer nada.
        }
    }
}