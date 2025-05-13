package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.json.JSONException;
import org.json.JSONObject;
import pe.com.sammis.vale.models.Empleado;
import pe.com.sammis.vale.services.interfaces.IEmpleadoService;
import pe.com.sammis.vale.util.ComponentsUtils;
import pe.com.sammis.vale.util.ExcelExporter;
import pe.com.sammis.vale.util.PdfExporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Route(value = "empleado", layout = MainLayout.class)
public class EmpleadoView extends VerticalLayout {

    private final Grid<Empleado> grid = new Grid<>(Empleado.class);
    private final Span totalRegistros=new Span();
    private final FormLayout form = new FormLayout();
    private final TextField dniField = new TextField("DNI");
    private final TextField nombreField = new TextField("Nombre");
    private final TextField apellidoField = new TextField("Apellido");
    private Button saveButton;
    private Button cancelButton ;
    private Button addButton=new Button("Nuevo");
    private Button exportExelButton=new Button("XLS");
    private Button exportPDFButton=new Button("PDF");
    private final Dialog formDialog = new Dialog();
    private Empleado empleado;
    private Binder<Empleado> binder = new Binder<>(Empleado.class);


    private IEmpleadoService service;

    public EmpleadoView(IEmpleadoService service) {
        addClassName("main-view");
        this.service = service;
        setUpTitle();
        setUpToolbar();
        setUpGrid();
        setUpForm();
    }
    private void setUpTitle() {
        ComponentsUtils.setTitulo(this,Empleado.class);
    }
    private void setUpToolbar() {
        TextField searchField = ComponentsUtils.createSearchField("Buscar", this::search);

        addButton.addClickListener(e -> {openFormForNew();});
        exportExelButton.addClickListener(e -> {exportExcel();});
        exportPDFButton.addClickListener(e -> {exportPdf();});



        HorizontalLayout toolbar = new HorizontalLayout(searchField, addButton,exportExelButton,exportPDFButton);

        add(toolbar);
    }
    private void exportPdf() {
        // 1. Cabeceras del Excel
        List<String> headers = List.of("ID", "Nombres","Apellido", "DNI","Estado");

        // 2. Obtener los datos visibles del Grid (por ejemplo, desde su data view)
        List<Empleado> empleados = grid.getListDataView().getItems().toList();

        // 3. Transformar los datos a listas de Strings
        List<List<String>> rows = empleados.stream()
                .map(e -> List.of(
                        String.valueOf(e.getId()),
                        e.getNombre(),
                        e.getApellido(),
                        e.getDni(),
                        String.valueOf(e.isEstado())
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
        // 1. Cabeceras del Excel
        List<String> headers = List.of("ID", "Nombres","Apellido", "DNI","Estado");

        // 2. Obtener los datos visibles del Grid (por ejemplo, desde su data view)
        List<Empleado> empleados = grid.getListDataView().getItems().toList();

        // 3. Transformar los datos a listas de Strings
        List<List<String>> rows = empleados.stream()
                .map(e -> List.of(
                        String.valueOf(e.getId()),
                        e.getNombre(),
                        e.getApellido(),
                        e.getDni(),
                        String.valueOf(e.isEstado())
                ))
                .toList();

        // 4. Llamar al exportador
        ByteArrayOutputStream stream = ExcelExporter.exportToExcel(headers, rows);

        // 5. Crear recurso para descarga
        StreamResource resource = new StreamResource("empleados.xlsx", () -> new ByteArrayInputStream(stream.toByteArray()));
        resource.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        // 6. Enlace de descarga temporal
        Anchor downloadLink = new Anchor(resource, "");
        downloadLink.getElement().setAttribute("download", true);
        downloadLink.getStyle().set("display", "none");
        add(downloadLink); // Añadir al layout

        // 7. Hacer clic automáticamente
        downloadLink.getElement().executeJs("this.click();");

        // Opcional: remover luego de descargar
        downloadLink.getElement().executeJs("setTimeout(() => this.remove(), 1000);");
    }
    private void setUpGrid() {

        grid.setColumns();
        grid.addColumn("dni").setWidth("80px").setFlexGrow(0);
        grid.addColumn(empleado -> {
                    String apellido = ComponentsUtils.capitalizeFirstLetter(empleado.getApellido());
                    String nombre = ComponentsUtils.capitalizeFirstLetter(empleado.getNombre());

                    // Concatenar el apellido y nombre, sin formato adicional
                    return apellido + " " + nombre;
                }).setHeader("Nombre Completo")
                .setWidth("220px")
                .setFlexGrow(0)
                .setSortable(true);


        grid.addComponentColumn(empleado -> {

            Span editSpan = new Span(new Icon(VaadinIcon.EDIT)) {{ getStyle().set("cursor", "pointer"); addClickListener(e -> openFormForEdit(empleado)); }};


            return editSpan;
        }).setHeader("Editar")
        .setWidth("80px")
        .setFlexGrow(0);

        grid.addComponentColumn(empleado -> {


            Span deleteSpan = new Span(new Icon(VaadinIcon.TRASH)) {{ getStyle().set("cursor", "pointer");addClickListener(e -> delete(empleado)); }};

            return deleteSpan;
        }).setHeader("Eliminar")
        .setWidth("80px")
        .setFlexGrow(0);


        add(grid);
        updateGrid();

    }
    private void setUpForm() {

        saveButton=ComponentsUtils.createSaveButton(this::save);
        cancelButton=ComponentsUtils.createCancelButton(this::cancelForm);
        TextField searchEmpleadoField=ComponentsUtils.createSearchField("Buscar",this::searchSunat);

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(searchEmpleadoField, dniField, nombreField, apellidoField);
        formLayout.setAlignItems(Alignment.BASELINE);

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        formLayout.add(buttonLayout);
        form.add(formLayout);
        form.setVisible(false);
        formDialog.add(form);
        formDialog.setCloseOnOutsideClick(false);
    }
    private void save() {
        String dni = dniField.getValue();
        String nombre = nombreField.getValue();
        String apellido = apellidoField.getValue();

        // Verificar si el DNI es numérico
        if (dni == null || dni.isEmpty() || !dni.matches("\\d+")) {
            Notification.show("El DNI debe ser numérico.", 3000, Notification.Position.BOTTOM_CENTER);
            return;
        }
        nombre=ComponentsUtils.capitalizeFirstLetter(nombre);
        apellido=ComponentsUtils.capitalizeFirstLetter(apellido);

        if (empleado == null) {
            // Registro nuevo
            if (dni == null || dni.isEmpty() || nombre == null || nombre.isEmpty() || apellido == null || apellido.isEmpty()) {
                Notification.show("Todos los campos son obligatorios.", 3000, Notification.Position.BOTTOM_CENTER);
                return;
            }

            Optional<Empleado> existente = service.findByDni(dni);
            if (existente.isPresent()) {
                Notification.show("El DNI ya está registrado.", 3000, Notification.Position.BOTTOM_CENTER);
                return;
            }

            Empleado nuevoEmpleado = new Empleado();
            nuevoEmpleado.setDni(dni);
            nuevoEmpleado.setNombre(nombre);
            nuevoEmpleado.setApellido(apellido);
            nuevoEmpleado.setEstado(true);
            service.save(nuevoEmpleado);
            ComponentsUtils.showSaveSuccess(Empleado.class,nuevoEmpleado.getNombre());
        } else {
            // Edición
            if (dni == null || dni.isEmpty() || nombre == null || nombre.isEmpty() || apellido == null || apellido.isEmpty()) {
                Notification.show("Todos los campos son obligatorios.", 3000, Notification.Position.BOTTOM_CENTER);
                return;
            }

            boolean dniCambiado = !dni.equals(empleado.getDni());

            if (dniCambiado) {
                Optional<Empleado> existente = service.findByDni(dni);
                if (existente.isPresent() && !existente.get().getId().equals(empleado.getId())) {
                    Notification.show("El DNI ya está registrado por otro empleado.", 3000, Notification.Position.BOTTOM_CENTER);
                    return;
                }
            }

            empleado.setDni(dni);
            empleado.setNombre(nombre);
            empleado.setApellido(apellido);
            empleado.setEstado(true);
            service.save(empleado);
        }

        updateGrid();
        formDialog.close();
    }
    private void updateGrid() {
        grid.setItems(service.findAllActive());
    }
    private void openFormForNew() {
        empleado = null;
        dniField.clear();
        nombreField.clear();
        apellidoField.clear();
        form.setVisible(true);
        formDialog.open();
    }
    private void openFormForEdit(Empleado empleado) {
        this.empleado=empleado;
        dniField.setValue(empleado.getDni());
        nombreField.setValue(empleado.getNombre());
        apellidoField.setValue(empleado.getApellido());
        form.setVisible(true);
        formDialog.open();
    }
    private void search(String s) {
            String filtro = s.trim().toLowerCase();

            if (filtro.isEmpty()) {
                updateGrid();
            } else {
                List<Empleado> empleadosFiltrados = service.findAllActive().stream()
                        .filter(emp -> emp.getDni().toLowerCase().contains(filtro)
                                || emp.getNombre().toLowerCase().contains(filtro)
                                || emp.getApellido().toLowerCase().contains(filtro))
                        .toList();

                grid.setItems(empleadosFiltrados);
            }
    }
    private void searchSunat(String dni) {

        if (dni.length() == 8) {
            try {
                String responseData = service.consultaSunat(dni); // Llamar al Service
                if (responseData != null) {
                    try {
                        JSONObject json = new JSONObject(responseData);
                        dniField.setValue(dni);
                        nombreField.setValue(json.optString("nombres", ""));
                        apellidoField.setValue(json.optString("apellidoPaterno", "") + " " + json.optString("apellidoMaterno", ""));
                    } catch (JSONException e) {
                        Notification.show("Error al procesar la respuesta del servidor. Respuesta no es válida.", 3000, Notification.Position.MIDDLE);
                        e.printStackTrace();
                    }
                } else {
                    Notification.show("No se encontraron datos", 3000, Notification.Position.MIDDLE);
                }
            } catch (Exception e) {
                Notification.show("Hubo un error al realizar la consulta", 3000, Notification.Position.MIDDLE);
                e.printStackTrace();
            }
        } else {
            Notification.show("El DNI debe tener 8 caracteres", 3000, Notification.Position.MIDDLE);
        }

    }
    private void delete(Empleado empleado) {
        ComponentsUtils.showDeleteConfirmation(
                Empleado.class,
                empleado.getNombre(),
                () -> {
                    empleado.setEstado(false);        // Desactivas al empleado
                    service.save(empleado);           // Guardas los cambios
                    updateGrid();                     // Refrescas la tabla
                }
        );
    }
    private void cancelForm() {
        dniField.clear();
        nombreField.clear();
        apellidoField.clear();
        formDialog.close();
    }




}

