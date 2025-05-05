package pe.com.sammis.vale.util;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ComponentsUtils {




    public static <T> Button createAddButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button addButton = new Button("Nuevo");
        addButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return addButton;
    }

    public static Button createAddButton(Runnable onClickAction) {
        Button addButton = new Button("Nuevo");
        addButton.addClickListener(e -> onClickAction.run());  // Ejecutar la acción sin parámetros
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return addButton;
    }

    public static <T> Button createSaveButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button saveButton = new Button("Guardar");
        saveButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    public static Button createSaveButton(Runnable onClickAction) {
        Button saveButton = new Button("Guardar");
        saveButton.addClickListener(e -> onClickAction.run());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return saveButton;
    }

    public static <T> Button createEditButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button editButton = new Button("" ,new Icon(VaadinIcon.EDIT));
        editButton.setWidth("50px");
        editButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });

        return editButton;
    }

    public static Button createEditButton(Runnable onClickAction) {
        Button editButton = new Button("", new Icon(VaadinIcon.EDIT));
        editButton.setWidth("50px");
        editButton.addClickListener(e -> onClickAction.run());

        return editButton;
    }

    public static <T> Button createDeleteButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button deleteButton = new Button("",new Icon(VaadinIcon.TRASH));
        deleteButton.setWidth("50px");
        deleteButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });

        return deleteButton;
    }

    public static Button createDeleteButton(Runnable onClickAction) {
            Button deleteButton = new Button("",new Icon(VaadinIcon.TRASH));
            deleteButton.setWidth("50px");
            deleteButton.addClickListener(e -> onClickAction.run());

            return deleteButton;
        }

    public static <T> Button createCancelButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
            Button cancelButton = new Button("Cancelar");
            cancelButton.addClickListener(e -> {
                T value = valueSupplier.get();
                onClickAction.accept(value);
            });
            return cancelButton;
        }

    public static Button createCancelButton(Runnable onClickAction) {
        Button cancelButton = new Button("Cancelar");
        cancelButton.addClickListener(e -> onClickAction.run());
        return cancelButton;
    }

    public static TextField createSearchField(String placeholder, Consumer<String> onSearch) {
        TextField searchField = new TextField();
        searchField.setPlaceholder(placeholder);
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setClearButtonVisible(true);

        searchField.addValueChangeListener(event -> {
            String value = event.getValue();
            onSearch.accept(value);
        });

        return searchField;
    }

    public static void setTitulo(VerticalLayout layout, Class<?> entityClass) {
        String entityName = getEntityName(entityClass);

        H3 title = new H3("Gestión de " + entityName);
        title.getStyle()
                .set("margin", "0 0 16px 0")
                .set("color", "#212529") // negro/gris muy oscuro
                .set("font-weight", "700") // negrita fuerte
                .set("text-shadow", "0 1px 2px rgba(0, 0, 0, 0.1)"); // sombra sutil

        layout.add(title);
    }



    private static String getEntityName(Class<?> entityClass) {
        return entityClass.getSimpleName().replace("Entity", "");
    }

    public static String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        input = input.toLowerCase();
        String[] words = input.split(" ");
        StringBuilder capitalizedString = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalizedString.append(word.substring(0, 1).toUpperCase())
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return capitalizedString.toString().trim();
    }

    public static void showDeleteConfirmation(Class<?> entityClass, String nombreEntidad, Runnable onDelete) {

        String entityName = getEntityName(entityClass);
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");


        Span mensaje = new Span("¿Estás seguro de que deseas eliminar " + entityName + ": ");
        Span nombreResaltado = new Span(nombreEntidad);
        nombreResaltado.getStyle().set("font-weight", "bold");
        HorizontalLayout contenido = new HorizontalLayout(mensaje, nombreResaltado);
        contenido.setAlignItems(FlexComponent.Alignment.CENTER);
        confirmDialog.add(contenido);


        Button confirmar = new Button("Eliminar",  event -> {
            onDelete.run();
            confirmDialog.close();


            Notification notification = Notification.show(entityName + " " + nombreEntidad +" "+ " eliminado exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setPosition(Notification.Position.BOTTOM_END);
            notification.setDuration(3000);
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);


        Button cancelar = new Button("Cancelar", e -> confirmDialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);


        confirmDialog.getFooter().add(cancelar, confirmar);
        confirmDialog.open();
    }

    public static void showDeleteConfirmationAsistencia(Class<?> entityClass, LocalDate date, Runnable onDelete) {

        String entityName = getEntityName(entityClass);
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmar eliminación");


        Span mensaje = new Span("¿Estás seguro de que deseas eliminar " + entityName + ": ");
        Span nombreResaltado = new Span(date.toString());
        nombreResaltado.getStyle().set("font-weight", "bold");
        HorizontalLayout contenido = new HorizontalLayout(mensaje, nombreResaltado);
        contenido.setAlignItems(FlexComponent.Alignment.CENTER);
        confirmDialog.add(contenido);


        Button confirmar = new Button("Eliminar",  event -> {
            onDelete.run();
            confirmDialog.close();


            Notification notification = Notification.show(entityName + " " + date.toString() +" "+ " eliminado exitosamente");
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.setPosition(Notification.Position.BOTTOM_END);
            notification.setDuration(3000);
        });
        confirmar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);


        Button cancelar = new Button("Cancelar", e -> confirmDialog.close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);


        confirmDialog.getFooter().add(cancelar, confirmar);
        confirmDialog.open();
    }

    public static void showSaveSuccess(Class<?> entityClass, String name) {
        String entityName = getEntityName(entityClass);
        String message = "El " + entityName + " \"" + name + "\" ha sido guardado correctamente.";
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);
        notification.setPosition(Notification.Position.BOTTOM_END);
    }

    public static Button createExcelButton(Runnable onClickAction) {
        Button excelButton = new Button("XLS", new Icon(VaadinIcon.FILE_TABLE));
        excelButton.addClickListener(e -> onClickAction.run());
        excelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        return excelButton;
    }

    public static <T> Button createExcelButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button excelButton = new Button("XLS", new Icon(VaadinIcon.FILE_TABLE));
        excelButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });
        excelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,ButtonVariant.LUMO_SUCCESS);
        return excelButton;
    }

    public static Button createPdfButton(Runnable onClickAction) {
        Button pdfButton = new Button("PDF", new Icon(VaadinIcon.FILE_PRESENTATION));
        pdfButton.addClickListener(e -> onClickAction.run());
        pdfButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        return pdfButton;
    }

    public static <T> Button createPdfButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button pdfButton = new Button("PDF", new Icon(VaadinIcon.FILE_PRESENTATION));
        pdfButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });
        pdfButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        return pdfButton;
    }

    public static <T> Button createViewButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button viewButton = new Button("", new Icon(VaadinIcon.SEARCH));
        viewButton.setWidth("50px");
        viewButton.addClickListener(e -> {
            T value = valueSupplier.get();
            onClickAction.accept(value);
        });
        return viewButton;
    }

    public static Button createViewButton(Runnable onClickAction) {
        Button viewButton = new Button("", new Icon(VaadinIcon.SEARCH));
        viewButton.setWidth("50px");
        viewButton.addClickListener(e -> onClickAction.run());
        return viewButton;
    }

    public static <T> Button createSearchButton(Supplier<T> valueSupplier, Consumer<T> onClickAction) {
        Button searchButton = new Button("Buscar");
        searchButton.addClickListener(e -> {
            T value = valueSupplier.get();  // Obtener el valor desde el Supplier
            onClickAction.accept(value);  // Ejecutar la acción con el valor obtenido
        });
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return searchButton;
    }

    public static Button createSearchButton(Runnable onClickAction) {
        Button searchButton = new Button("Buscar");
        searchButton.addClickListener(e -> onClickAction.run());  // Ejecutar la acción sin parámetros
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return searchButton;
    }




}
