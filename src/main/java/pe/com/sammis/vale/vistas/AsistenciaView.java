package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.interfaces.IAsistenciaService;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;
import pe.com.sammis.vale.util.ComponentsUtils;
import pe.com.sammis.vale.util.ExcelExporter;
import pe.com.sammis.vale.util.PdfExporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Route(value = "asistencia", layout = MainLayout.class)
public class AsistenciaView extends VerticalLayout {

    private final Grid<LocalDate> grid = new Grid<>(LocalDate.class, false);
    private final Grid<Asistencia> gridReporte = new Grid<>(Asistencia.class, false);
    private Grid<Empleado> empleadoGrid = new Grid<>(Empleado.class, false);
    private final Dialog formDialog = new Dialog();
    private final VerticalLayout formLayout = new VerticalLayout();
    private final H4 titulo = new H4();
    private final DatePicker fechaPicker = new DatePicker();
    private final IAsistenciaService asistenciaService;
    private final IEmpleadoService empleadoService;
    private final ITipoAsistenciaService tipoAsistenciaService;
    private final Map<Long, RadioButtonGroup<TipoAsistencia>> asistenciaSeleccionada = new HashMap<>();
    private final Map<Long, Long> asistenciasMap = new HashMap<>(); // Guarda IDs de TipoAsistencia para edición
    private List<TipoAsistencia> tiposAsistenciaCache;
    private List<Empleado> empleadosCache;
    private LocalDate fecha;
    private Button exportExelButton=new Button("XLS");
    private Button exportPDFButton=new Button("PDF");
    private Button addButton=new Button("Nuevo");



    public AsistenciaView(IEmpleadoService empleadoService, IAsistenciaService asistenciaService, ITipoAsistenciaService tipoAsistenciaService) {
        this.empleadoService = empleadoService;
        this.asistenciaService = asistenciaService;
        this.tipoAsistenciaService = tipoAsistenciaService;
        this.tiposAsistenciaCache = tipoAsistenciaService.findAll();
        this.empleadosCache = empleadoService.findAllActive();
        addClassName("main-view");
        fechaPicker.setValue(LocalDate.now());
        setUpTitle();
        setUpToolbar();
        setUpGrid();
        setUpForm();
    }

    private void setUpTitle() {
        ComponentsUtils.setTitulo(this, Asistencia.class);
    }
    private void setUpToolbar() {
        fechaPicker.addValueChangeListener(event -> updateGrid());
        fechaPicker.setLocale(new Locale("es", "ES"));

        addButton.addClickListener(e -> {
            LocalDate fecha = fechaPicker.getValue(); // Obtener la fecha seleccionada

            // Validar antes de continuar
            if (!validarFecha(fecha)) {
                return;
            }

            if (!validarExistenciaDeEmpleadosYTiposDeAsistencia()) {
                return;
            }

            // Todas las validaciones pasaron, se puede navegar
            Map<String, List<String>> parameters = new HashMap<>();
            parameters.put("fecha", List.of(fecha.toString()));
            UI.getCurrent().navigate("registrar-asistencia", new QueryParameters(parameters));
        });

        exportExelButton.addClickListener(e -> exportExcel());
        exportPDFButton.addClickListener(e -> exportPdf());

        HorizontalLayout toolbar = new HorizontalLayout(fechaPicker, addButton, exportExelButton, exportPDFButton);
        add(toolbar);
    }
    private void exportPdf() {
        // 1. Cabeceras del Excel
        List<String> headers = List.of("ID", "Fecha", "Empleado", "Apellido", "Tipo de Asistencia");

        // 2. Obtener los datos visibles (en este caso desde una lista de Asistencias)
        List<Asistencia> asistencias = asistenciaService.findAll();
        gridReporte.setItems(asistencias);
        gridReporte.getListDataView().getItems().toList();

        // 3. Transformar los datos a listas de Strings
        List<List<String>> rows = asistencias.stream()
                .map(a -> List.of(
                        String.valueOf(a.getId()),  // ID
                        a.getFecha() != null ? a.getFecha().toString() : "", // Fecha
                        a.getEmpleado() != null ? a.getEmpleado().getNombre() : "", // Empleado (Nombre)
                        a.getEmpleado() != null ? a.getEmpleado().getApellido() : "", // Empleado (Apellido)
                        a.getTipoAsistencia() != null ? a.getTipoAsistencia().getAlias() : "" // Tipo de Asistencia (Alias)
                ))
                .toList();

        ByteArrayOutputStream pdfStream = PdfExporter.exportToPdf(headers, rows);

        StreamResource resource = new StreamResource("empleados.pdf", () -> new ByteArrayInputStream(pdfStream.toByteArray()));
        resource.setContentType("application/pdf");

        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.getStyle().set("display", "none");
        add(downloadLink);
        downloadLink.getElement().executeJs("this.click();");
        downloadLink.getElement().executeJs("setTimeout(() => this.remove(), 1000);");
    }
    private void exportExcel() {
        long start = System.nanoTime(); // INICIO del temporizador

        // 1. Cabeceras
        List<String> headers = List.of("Fecha", "Empleado", "Apellido", "Tipo de Asistencia");

        // 2. Obtener los datos
        List<Asistencia> asistencias = asistenciaService.findAll();
        gridReporte.setItems(asistencias);

        // 3. Transformar los datos
        List<List<String>> rows = asistencias.stream()
                .map(a -> List.of(
                        a.getFecha() != null ? a.getFecha().toString() : "",
                        a.getEmpleado() != null ? a.getEmpleado().getNombre() : "",
                        a.getEmpleado() != null ? a.getEmpleado().getApellido() : "",
                        a.getTipoAsistencia() != null ? a.getTipoAsistencia().getAlias() : ""
                ))
                .toList();

        // 4. Exportar
        ByteArrayOutputStream stream = ExcelExporter.exportToExcelOptimized(headers, rows);

        // 5. Crear recurso
        StreamResource resource = new StreamResource("asistencias.xlsx", () -> new ByteArrayInputStream(stream.toByteArray()));
        resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // 6. Enlace de descarga
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.getStyle().set("display", "none");
        add(downloadLink);

        // 7. Descargar automáticamente
        downloadLink.getElement().executeJs("this.click();");

        // 8. Limpiar después de un segundo
        downloadLink.getElement().executeJs("setTimeout(() => this.remove(), 1000);");

        // 9. Medir tiempo y mostrar notificación
        long end = System.nanoTime(); // Fin del temporizador
        double durationSeconds = (end - start) / 1_000_000_000.0;

        Notification.show(
                String.format(Locale.US, "Exportación completada en %.1f segundos", durationSeconds),
                3000,
                Notification.Position.BOTTOM_END
        );

    }
    private void setUpGrid() {
        grid.setColumns();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        grid.addColumn(fecha -> {
            if (fecha instanceof LocalDate) {
                return ((LocalDate) fecha).format(dateFormatter);
            } else {
                return fecha.toString(); // O maneja otros tipos de fecha si es necesario
            }
        }).setHeader("Fecha").setWidth("100px").setFlexGrow(0);


        grid.addComponentColumn(fecha -> {

                    Span editSpan = new Span(new Icon(VaadinIcon.EDIT)) {{ getStyle().set("cursor", "pointer"); addClickListener(e -> openFormForEdit(fecha)); }};


                    return editSpan;
                }).setHeader("Editar")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addComponentColumn(fecha -> {


                    Span deleteSpan = new Span(new Icon(VaadinIcon.TRASH)) {{ getStyle().set("cursor", "pointer");addClickListener(e -> delete(fecha)); }};

                    return deleteSpan;
                }).setHeader("Eliminar")
                .setWidth("80px")
                .setFlexGrow(0);

        grid.addComponentColumn(fecha -> {


                    Span viewSpan = new Span(new Icon(VaadinIcon.SEARCH)) {{ getStyle().set("cursor", "pointer");addClickListener(e -> view(fecha)); }};

                    return viewSpan;
                }).setHeader("Ver")
                .setWidth("80px")
                .setFlexGrow(0);

        add(grid);
        updateGrid();
    }
    private void view(LocalDate fecha) {
        List<Asistencia> asistencias = asistenciaService.findByFecha(fecha);
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Listado de Asistencias " + fecha);

        Grid<Asistencia> grid = new Grid<>(Asistencia.class, false);

        grid.addColumn(asistencia -> asistencia.getEmpleado().getNombre())
                .setHeader("Nombre")
                .setSortable(true)
                .setComparator(asistencia -> asistencia.getEmpleado().getNombre());

        grid.addColumn(asistencia -> asistencia.getEmpleado().getApellido())
                .setHeader("Apellido")
                .setSortable(true)
                .setComparator(asistencia -> asistencia.getEmpleado().getApellido());

        // 🎨 Columna con color de fondo personalizado
        grid.addColumn(new ComponentRenderer<>(asistencia -> {
            Span span = new Span(
                    asistencia.getTipoAsistencia() != null
                            ? asistencia.getTipoAsistencia().getAlias()
                            : "Sin tipo"
            );

            // Obtener el color hexadecimal
            String color = asistencia.getTipoAsistencia() != null
                    ? asistencia.getTipoAsistencia().getColorHex()
                    : "#f0f0f0"; // color gris claro por defecto

            // Verificar que el color tenga el prefijo '#', de lo contrario, agregarlo
            if (!color.startsWith("#")) {
                color = "#" + color;
            }

            // Establecer el estilo para la celda con el color de fondo
            span.getStyle()
                    .set("background-color", color)
                    .set("padding", "4px 8px")
                    .set("border-radius", "6px")
                    .set("color", "white")  // Texto blanco
                    .set("text-align", "center"); // Alineación centrada

            return span;
        })).setHeader("Tipo de Asistencia")
                .setSortable(true)
                .setComparator(asistencia -> asistencia.getTipoAsistencia().getNombre());

        grid.setItems(asistencias);

        dialog.add(grid);

        Button closeButton = new Button("Cerrar", event -> dialog.close());
        dialog.getFooter().add(closeButton);

        dialog.setWidth("600px");
        dialog.setHeight("600px");
        dialog.open();
    }
    private String obtenerColorTextoContraste(String colorHex) {
        if (colorHex == null || colorHex.isEmpty()) {
            return "black";
        }
        String hex = colorHex.startsWith("#") ? colorHex.substring(1) : colorHex;
        try {
            int r = Integer.valueOf(hex.substring(0, 2), 16);
            int g = Integer.valueOf(hex.substring(2, 4), 16);
            int b = Integer.valueOf(hex.substring(4, 6), 16);
            double luminancia = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
            return luminancia > 0.55 ? "black" : "white";
        } catch (NumberFormatException e) {
            System.err.println("Error al parsear el color hexadecimal: " + colorHex);
            return "black";
        }
    }
    /*private void setUpForm() {
        TextField searchField = ComponentsUtils.createSearchField("Buscar", this::search);
        Button saveButton = ComponentsUtils.createSaveButton(this::save);
        Button cancelButton = ComponentsUtils.createCancelButton(this::cancelForm);
        Checkbox selectAllPCheckbox = new Checkbox("Seleccionar todos con 'P'");

        HorizontalLayout titleLayout = new HorizontalLayout(titulo);
        titleLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton,searchField);
        buttonLayout.setSpacing(true);

        HorizontalLayout checkLayout = new HorizontalLayout( selectAllPCheckbox);
        buttonLayout.setSpacing(true);

        formLayout.setAlignItems(Alignment.BASELINE);

        empleadoGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        empleadoGrid.getElement().getStyle().set("font-size", "13px");
        empleadoGrid.setWidth("750px");
        empleadoGrid.addColumn(empleado -> "<b>" + empleado.getApellido() + "</b> " + empleado.getNombre())
                .setHeader("Nombre Completo")
                .setWidth("220px")
                .setFlexGrow(0)
                .setRenderer(new ComponentRenderer<>(item -> {
                    Div div = new Div();
                    div.getElement().setProperty("innerHTML", "<b>" + item.getApellido() + "</b> " + item.getNombre());
                    return div;
                })).setSortable(true);









        empleadoGrid.addColumn(new ComponentRenderer<>(empleado -> {
            RadioButtonGroup<TipoAsistencia> radioGroup = new RadioButtonGroup<>();
            radioGroup.setItems(tiposAsistenciaCache);
            radioGroup.setItemLabelGenerator(TipoAsistencia::getAlias);
            radioGroup.setWidthFull();
            radioGroup.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
            radioGroup.getElement().getStyle().set("font-size", "10px");
            radioGroup.getElement().getStyle().set("padding", "2px");
            asistenciaSeleccionada.put(empleado.getId(), radioGroup);

            // Obtener el ID de TipoAsistencia desde el mapa (si existe)
            Long tipoAsistenciaId = asistenciasMap.get(empleado.getId());

            // Intentamos obtener el tipo de asistencia por ID
            TipoAsistencia asistenciaPrevia = tiposAsistenciaCache.stream()
                    .filter(ta -> ta.getId().equals(tipoAsistenciaId))
                    .findFirst()
                    .orElse(null);

            // Si no existe, buscamos "SR"
            if (asistenciaPrevia == null) {
                asistenciaPrevia = tiposAsistenciaCache.stream()
                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))  // Asegurándonos de que "SR" se busca correctamente
                        .findFirst()
                        .orElse(null);
            }

            // Verificar si encontramos el valor "SR" o el valor previo
            if (asistenciaPrevia != null) {
                radioGroup.setValue(asistenciaPrevia);  // Establecer el valor encontrado
            } else {
                // Agregar un log si no se encuentra "SR" ni un valor previo
                System.out.println("No se encontró el tipo de asistencia 'SR' o el valor previo para el empleado con ID: " + empleado.getId());
            }

            return radioGroup;
        })).setHeader("Tipo de Asistencia").setWidth("500px").setFlexGrow(0);

        empleadoGrid.setItems(empleadosCache);
        empleadoGrid.setHeight("400px");

        formLayout.add(titleLayout, buttonLayout,checkLayout, empleadoGrid);
        formDialog.add(formLayout);
        formDialog.setCloseOnOutsideClick(false);

        // **BLOQUE DE CÓDIGO DEL CHECKBOX (CORREGIDO)**
        selectAllPCheckbox.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();
            empleadoGrid.getDataProvider().fetch(new Query<>())
                    .forEach(empleado -> {
                        RadioButtonGroup<TipoAsistencia> radioGroup = asistenciaSeleccionada.get(empleado.getId());
                        if (radioGroup != null) {
                            radioGroup.getDataProvider().fetch(new Query<>())
                                    .forEach(tipoAsistencia -> {
                                        if ("P".equalsIgnoreCase(tipoAsistencia.getAlias())) {
                                            if (isChecked) {
                                                radioGroup.setValue(tipoAsistencia);
                                            } else {
                                                // Buscar y seleccionar "SR" al desactivar
                                                radioGroup.getDataProvider().fetch(new Query<>())
                                                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                                                        .findFirst()
                                                        .ifPresentOrElse(
                                                                radioGroup::setValue,
                                                                radioGroup::clear // Si no se encuentra "SR", deseleccionar
                                                        );
                                            }
                                        }
                                    });
                        }
                    });
        });
        // **FIN DEL BLOQUE DE CÓDIGO DEL CHECKBOX (CORREGIDO)**
    }*/
    private void setUpForm() {
        TextField searchField = ComponentsUtils.createSearchField("Buscar", this::search);
        Button saveButton = ComponentsUtils.createSaveButton(this::save);
        Button cancelButton = ComponentsUtils.createCancelButton(this::cancelForm);
        Checkbox selectAllPCheckbox = new Checkbox("Seleccionar todos con 'P'");

        HorizontalLayout titleLayout = new HorizontalLayout(titulo);
        titleLayout.setSpacing(true);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton, searchField);
        buttonLayout.setSpacing(true);

        HorizontalLayout checkLayout = new HorizontalLayout(selectAllPCheckbox);
        checkLayout.setSpacing(true);

        formLayout.setAlignItems(Alignment.BASELINE);

        empleadoGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        empleadoGrid.getElement().getStyle().set("font-size", "13px");
        empleadoGrid.setWidth("750px");

        empleadoGrid.addColumn(empleado -> "<b>" + empleado.getApellido() + "</b> " + empleado.getNombre())
                .setHeader("Nombre Completo")
                .setWidth("220px")
                .setFlexGrow(0)
                .setRenderer(new ComponentRenderer<>(item -> {
                    Div div = new Div();
                    div.getElement().setProperty("innerHTML", "<b>" + item.getApellido() + "</b> " + item.getNombre());
                    return div;
                }))
                .setSortable(true)
                .setKey("nombreCompleto");

        empleadoGrid.addColumn(new ComponentRenderer<>(empleado -> {
            RadioButtonGroup<TipoAsistencia> radioGroup = new RadioButtonGroup<>();
            radioGroup.setItems(tiposAsistenciaCache);
            radioGroup.setItemLabelGenerator(TipoAsistencia::getAlias);
            radioGroup.setWidthFull();
            radioGroup.getElement().getStyle().set("font-size", "10px");
            radioGroup.getElement().getStyle().set("padding", "2px");

            Map<Long, Span> spanPorTipo = new HashMap<>();
            asistenciaSeleccionada.put(empleado.getId(), radioGroup);

            radioGroup.setRenderer(new ComponentRenderer<>(tipo -> {
                Span span = new Span(tipo.getAlias());
                span.getStyle().set("padding", "3px 6px");
                span.getStyle().set("border-radius", "4px");
                span.getStyle().set("transition", "all 0.3s ease");
                spanPorTipo.put(tipo.getId(), span);
                return span;
            }));

            // Selección inicial
            Long tipoAsistenciaId = asistenciasMap.get(empleado.getId());
            TipoAsistencia asistenciaPrevia = tiposAsistenciaCache.stream()
                    .filter(ta -> ta.getId().equals(tipoAsistenciaId))
                    .findFirst()
                    .orElseGet(() -> tiposAsistenciaCache.stream()
                            .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                            .findFirst()
                            .orElse(null));

            if (asistenciaPrevia != null) {
                radioGroup.setValue(asistenciaPrevia);
                actualizarEstilo(spanPorTipo, asistenciaPrevia);
            }

            radioGroup.addValueChangeListener(event -> {
                actualizarEstilo(spanPorTipo, event.getValue());
            });

            return radioGroup;
        })).setHeader("Tipo de Asistencia").setWidth("500px").setFlexGrow(0);

        empleadoGrid.setItems(empleadosCache);
        empleadoGrid.setHeight("400px");

        formLayout.add(titleLayout, buttonLayout, checkLayout, empleadoGrid);
        formDialog.add(formLayout);
        formDialog.setCloseOnOutsideClick(false);

        // BLOQUE DE CÓDIGO DEL CHECKBOX
        selectAllPCheckbox.addValueChangeListener(event -> {
            boolean isChecked = event.getValue();
            empleadoGrid.getDataProvider().fetch(new Query<>())
                    .forEach(empleado -> {
                        RadioButtonGroup<TipoAsistencia> radioGroup = asistenciaSeleccionada.get(empleado.getId());
                        if (radioGroup != null) {
                            radioGroup.getDataProvider().fetch(new Query<>())
                                    .forEach(tipoAsistencia -> {
                                        if ("P".equalsIgnoreCase(tipoAsistencia.getAlias())) {
                                            if (isChecked) {
                                                radioGroup.setValue(tipoAsistencia);
                                            } else {
                                                radioGroup.getDataProvider().fetch(new Query<>())
                                                        .filter(ta -> "SR".equalsIgnoreCase(ta.getAlias()))
                                                        .findFirst()
                                                        .ifPresentOrElse(
                                                                radioGroup::setValue,
                                                                radioGroup::clear
                                                        );
                                            }
                                        }
                                    });
                        }
                    });
        });
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
            return "#000000"; // color por defecto
        }
        int r = Integer.valueOf(backgroundColor.substring(1, 3), 16);
        int g = Integer.valueOf(backgroundColor.substring(3, 5), 16);
        int b = Integer.valueOf(backgroundColor.substring(5, 7), 16);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return luminance > 0.5 ? "#000000" : "#FFFFFF";

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
                    .toList();

            empleadoGrid.setItems(empleadosFiltrados);
        }
    }
    private void save() {
        List<Asistencia> asistenciasAGuardar = new ArrayList<>();
        List<Asistencia> asistenciasActualizadas = new ArrayList<>();
        LocalDate fechaSeleccionada = fechaPicker.getValue();

        // 1. Obtener todas las asistencias existentes para la fecha seleccionada
        Map<Long, Asistencia> existingAsistenciasMap = asistenciaService.findByFecha(fechaSeleccionada)
                .stream()
                .collect(Collectors.toMap(asistencia -> asistencia.getEmpleado().getId(), asistencia -> asistencia));

        // 2. Iterar sobre las selecciones del usuario
        for (Map.Entry<Long, RadioButtonGroup<TipoAsistencia>> entry : asistenciaSeleccionada.entrySet()) {
            Long empleadoId = entry.getKey();
            RadioButtonGroup<TipoAsistencia> radioGroup = entry.getValue();
            TipoAsistencia tipoAsistencia = radioGroup.getValue();

            if (tipoAsistencia == null) continue;

            Empleado empleado = empleadosCache.stream()
                    .filter(e -> e.getId().equals(empleadoId))
                    .findFirst()
                    .orElse(null);

            if (empleado != null) {
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

        // 3. Guardar todas las nuevas asistencias en lote
        if (!asistenciasAGuardar.isEmpty()) {
            asistenciaService.saveAll(asistenciasAGuardar);
            Notification.show("Asistencias guardadas: " + asistenciasAGuardar.size(), 3000, Notification.Position.TOP_CENTER);
        }

        // 4. Actualizar todas las asistencias existentes en lote
        if (!asistenciasActualizadas.isEmpty()) {
            asistenciaService.saveAll(asistenciasActualizadas);
            Notification.show("Asistencias actualizadas: " + asistenciasActualizadas.size(), 3000, Notification.Position.TOP_CENTER);
        } else if (asistenciasAGuardar.isEmpty()) {
            Notification.show("No hay cambios para guardar.", 3000, Notification.Position.TOP_CENTER);
        }

        formDialog.close();
        updateGrid();
    }
    private void updateGrid() {
        grid.setItems(asistenciaService.findDistinctFechas());
    }
    private void openFormForNew(LocalDate fecha) {
        if (!validarFecha(fecha)) return;
        if (!validarExistenciaDeEmpleadosYTiposDeAsistencia()) return;

        titulo.setText("Nueva Asistencia para el día: " + fecha.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        fechaPicker.setValue(fecha);

        // Resetear las selecciones y el mapa de asistencias para el nuevo formulario
        asistenciaSeleccionada.clear();
        asistenciasMap.clear();

        empleadoGrid.setItems(empleadosCache);
        formDialog.open();
    }
    private void openFormForEdit(LocalDate fecha) {
        System.out.println("Intentando editar asistencias para la fecha: " + fecha);

        formDialog.open();
        titulo.setText("Editar Asistencia para el día: " + fecha);
        fechaPicker.setValue(fecha);

        asistenciasMap.clear();
        List<Asistencia> asistencias = asistenciaService.findByFecha(fecha);
        System.out.println("Número de asistencias encontradas para la fecha " + fecha + ": " + asistencias.size());
        for (Asistencia asistencia : asistencias) {
            System.out.println("  Asistencia para empleado ID: " + asistencia.getEmpleado().getId() + ", Tipo: " + asistencia.getTipoAsistencia().getNombre() + " (TipoAsistencia ID: " + asistencia.getTipoAsistencia().getId() + ")");
            asistenciasMap.put(asistencia.getEmpleado().getId(), asistencia.getTipoAsistencia().getId());
        }

        // Reconfigurar el empleadoGrid para la edición
        Grid<Empleado> nuevoEmpleadoGrid = new Grid<>(Empleado.class, false);
        nuevoEmpleadoGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        nuevoEmpleadoGrid.getElement().getStyle().set("font-size", "13px");
        nuevoEmpleadoGrid.setWidth("750px");
        nuevoEmpleadoGrid.addColumn(empleado -> "<b>" + empleado.getApellido() + "</b> " + empleado.getNombre())
                .setHeader("Nombre Completo")
                .setWidth("220px")
                .setFlexGrow(0)
                .setRenderer(new ComponentRenderer<>(item -> {
                    Div div = new Div();
                    div.getElement().setProperty("innerHTML", "<b>" + item.getApellido() + "</b> " + item.getNombre());
                    return div;
                })).setSortable(true);

        nuevoEmpleadoGrid.addColumn(new ComponentRenderer<>(empleado -> {
            RadioButtonGroup<TipoAsistencia> radioGroup = new RadioButtonGroup<>();
            radioGroup.setItems(tiposAsistenciaCache);
            radioGroup.setItemLabelGenerator(TipoAsistencia::getAlias);
            radioGroup.setWidthFull();
            radioGroup.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
            radioGroup.getElement().getStyle().set("font-size", "10px");
            radioGroup.getElement().getStyle().set("padding", "2px");
            asistenciaSeleccionada.put(empleado.getId(), radioGroup);

            Long tipoAsistenciaIdExistente = asistenciasMap.get(empleado.getId());
            if (tipoAsistenciaIdExistente != null) {
                TipoAsistencia asistenciaExistenteEnOpciones = tiposAsistenciaCache.stream()
                        .filter(tipo -> tipo.getId().equals(tipoAsistenciaIdExistente))
                        .findFirst()
                        .orElse(null);

                if (asistenciaExistenteEnOpciones != null) {
                    radioGroup.setValue(asistenciaExistenteEnOpciones);
                }
            } else {
                TipoAsistencia asistenciaSR = tiposAsistenciaCache.stream()
                        .filter(tipo -> tipo.getAlias().equalsIgnoreCase("SR"))
                        .findFirst()
                        .orElse(null);
                if (asistenciaSR != null) {
                    radioGroup.setValue(asistenciaSR);
                }
            }
            return radioGroup;
        })).setHeader("Tipo de Asistencia").setWidth("500px").setFlexGrow(0);

        nuevoEmpleadoGrid.setItems(empleadosCache);
        formLayout.replace(empleadoGrid, nuevoEmpleadoGrid);
        empleadoGrid = nuevoEmpleadoGrid;
    }
    private void delete(LocalDate fecha) {
        ComponentsUtils.showDeleteConfirmation(
                Asistencia.class,
                fecha.toString(),
                () -> {
                    asistenciaService.deleteByFecha(fecha);
                    updateGrid();
                }
        );
    }
    private boolean validarFecha(LocalDate date) {
        if (asistenciaService.existsByFecha(date)) {
            Notification.show("Ya existe un registro para esta fecha: " + date, 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        if (date.isAfter(LocalDate.now())) {
            Notification.show("No se puede adelantar fecha: " + date, 3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }
    private boolean validarExistenciaDeEmpleadosYTiposDeAsistencia() {
        if (tiposAsistenciaCache.isEmpty() || empleadosCache.isEmpty()) {
            Notification.show("Debe existir al menos un empleado y un tipo de asistencia para registrar asistencia.",
                            3000, Notification.Position.BOTTOM_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return false;
        }
        return true;
    }
    private void cancelForm() {
        formDialog.close();
    }
}