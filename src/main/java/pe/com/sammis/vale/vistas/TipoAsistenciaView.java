package pe.com.sammis.vale.vistas;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import pe.com.sammis.vale.models.TipoAsistencia;
import pe.com.sammis.vale.services.interfaces.ITipoAsistenciaService;
import pe.com.sammis.vale.util.ComponentsUtils;

import java.util.List;

@Route(value = "tipoAsistencia", layout = MainLayout.class)
public class TipoAsistenciaView extends VerticalLayout {

    private final Grid<TipoAsistencia> grid = new Grid<>(TipoAsistencia.class);
    private final FormLayout form = new FormLayout();
    private final TextField nombreField = new TextField("Nombre");
    private final TextField aliasField = new TextField("Alias");
    private final TextField colorHexField = new TextField("ColorHex");
    private Button saveButton;
    private Button cancelButton;
    private Button newButton = new Button("Nuevo");
    private final TextField searchField = new TextField();
    private final Dialog formDialog = new Dialog();
    private Input colorPicker = new Input();
    private TipoAsistencia tipoAsistencia;


    private ITipoAsistenciaService service;

    public TipoAsistenciaView(ITipoAsistenciaService service) {
        addClassName("main-view");
        this.service = service;
        setUpTitle();
        setUpToolbar();
        setUpGrid();
        setUpForm();
    }
    private void setUpTitle() {
        ComponentsUtils.setTitulo(this,TipoAsistencia.class);
    }
    private void setUpToolbar() {
        TextField searchField = ComponentsUtils.createSearchField("Buscar", this::search);
        newButton = ComponentsUtils.createAddButton(this::openFormForNew);
        HorizontalLayout toolbar = new HorizontalLayout(searchField,newButton);
        toolbar.getStyle().set("margin-bottom", "20px");
        add(toolbar);
    }
    private void setUpGrid() {
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COMPACT);
        grid.setColumns("nombre","alias");
        grid.getColumnByKey("nombre").setWidth("100px").setFlexGrow(0);
        grid.getColumnByKey("alias").setWidth("60px").setFlexGrow(0);
        grid.getElement().getStyle().set("font-size", "13px");
        grid.addComponentColumn(tipo -> {
                    Div colorPreview = new Div();
                    String colorHex = tipo.getColorHex();
                    if (colorHex != null && !colorHex.startsWith("#")) {
                        colorHex = "#" + colorHex;
                    }
                    colorPreview.getStyle().set("background-color", colorHex);
                    colorPreview.getStyle().set("width", "20px");
                    colorPreview.getStyle().set("height", "20px");
                    colorPreview.getStyle().set("border-radius", "50%");
                    return colorPreview;
                }).setHeader("Color").setWidth("60px").setFlexGrow(0);
        grid.addComponentColumn(tipoAsistencia -> {
            Button editButton = ComponentsUtils.createEditButton(()->tipoAsistencia,this::openFormForEdit);
            Button deleteButton = ComponentsUtils.createDeleteButton(()->tipoAsistencia,this::delete);
            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Acciones");
        add(grid);
        updateGrid();
    }
    private void setUpForm() {
        saveButton=ComponentsUtils.createSaveButton(this::save);
        cancelButton=ComponentsUtils.createCancelButton(this::cancelForm);


        colorPicker.setType("color");
        colorPicker.addValueChangeListener(event -> {
            if (!event.getValue().equals(colorHexField.getValue())) {
                colorHexField.setValue(event.getValue());
            }
        });

        colorHexField.addValueChangeListener(event -> {
            if (event.getValue().matches("^#([A-Fa-f0-9]{6})$")) {
                colorPicker.setValue(event.getValue());
            }
        });


        HorizontalLayout colorLayout = new HorizontalLayout(colorHexField, colorPicker);
        colorLayout.setAlignItems(FlexComponent.Alignment.BASELINE);


        VerticalLayout formLayout = new VerticalLayout();
        formLayout.add(nombreField,aliasField, colorLayout);
        formLayout.setAlignItems(Alignment.START);


        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setSpacing(true);

        formLayout.add(buttonLayout);
        form.add(formLayout);
        form.setVisible(false);
        formDialog.add(form);
        formDialog.setCloseOnOutsideClick(false);
    }
    private void updateGrid() {
        grid.setItems(service.findAll());
    }
    private void openFormForNew() {
        tipoAsistencia = null;
        nombreField.clear();
        aliasField.clear();
        colorHexField.clear();
        form.setVisible(true);
        formDialog.open();
    }
    private void openFormForEdit(TipoAsistencia tipoAsistencia) {
        this.tipoAsistencia = tipoAsistencia;
        nombreField.setValue(tipoAsistencia.getNombre());
        aliasField.setValue(tipoAsistencia.getAlias());
        colorHexField.setValue(tipoAsistencia.getColorHex());
        form.setVisible(true);
        formDialog.open();
    }
    private void delete(TipoAsistencia tipoAsistencia) {
            ComponentsUtils.showDeleteConfirmation(
                    TipoAsistencia.class,              // Tipo de entidad (texto que aparece en la pregunta)
                    tipoAsistencia.getNombre(),         // Nombre específico a mostrar en negrita
                    () -> {                     // Acción que se ejecutará si se confirma
                        service.deleteById(tipoAsistencia.getId());
                        updateGrid();
                    }
            );

    }
    private void save() {
            String nombre = nombreField.getValue();
            String alias = aliasField.getValue();
            String colorHex = colorHexField.getValue();


            if (tipoAsistencia != null) {
                tipoAsistencia.setNombre(nombre);
                tipoAsistencia.setAlias(alias);
                tipoAsistencia.setColorHex(colorHex);
                service.save(tipoAsistencia);
            } else {
                TipoAsistencia tipoAsistencia = new TipoAsistencia();
                tipoAsistencia.setNombre(nombre);
                tipoAsistencia.setAlias(alias);
                tipoAsistencia.setColorHex(colorHex);
                service.save(tipoAsistencia);
                ComponentsUtils.showSaveSuccess(TipoAsistencia.class,tipoAsistencia.getNombre());
            }

            updateGrid();
            formDialog.close();

    }
    private void search(String s) {
        String filtro = s.trim().toLowerCase();

        if (filtro.isEmpty()) {
            updateGrid(); // Mostrar todos los empleados si el campo está vacío
        } else {
            List<TipoAsistencia> tipoAsistencias = service.findAll().stream()
                    .filter(emp -> emp.getNombre().toLowerCase().contains(filtro)
                            || emp.getColorHex().toLowerCase().contains(filtro))
                    .toList();

            grid.setItems(tipoAsistencias);
        }
    }
    private void cancelForm() {
        nombreField.clear();
        colorHexField.clear();
        formDialog.close();
    }
}
