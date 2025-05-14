package pe.com.sammis.vale.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import pe.com.sammis.vale.models.TipoAsistencia;

import java.util.List;
import java.util.stream.Collectors;

@SpringComponent
@UIScope
public class TipoAsistenciaRadioButtonView extends HorizontalLayout {

    private TipoAsistencia selectedTipo;

    public TipoAsistenciaRadioButtonView() {
        addClassName("tipo-asistencia-radio-group");
        // Disposición horizontal con espacio entre opciones
        getStyle()
                .set("display", "flex")
                .set("gap", "5px") // Reducido el espacio entre los spans
                .set("flex-direction", "row");
    }

    // Configuramos las opciones
    public void setItems(List<TipoAsistencia> tiposAsistencia) {
        for (TipoAsistencia tipo : tiposAsistencia) {
            Span span = createRadioButtonSpan(tipo);
            span.addClickListener(event -> setValue(tipo));
            add(span);
        }

        // Establecer el valor por defecto "SR" si está presente en la lista
        TipoAsistencia tipoSR = tiposAsistencia.stream()
                .filter(tipo -> "SR".equals(tipo.getAlias()))
                .findFirst()
                .orElse(null);

        // Si se encuentra "SR", lo establecemos como el seleccionado
        if (tipoSR != null) {
            setValue(tipoSR);
        }
    }

    // Crear un Span como un radio button
    private Span createRadioButtonSpan(TipoAsistencia tipo) {
        Span span = new Span(tipo.getAlias());
        span.addClassName("tipo-asistencia-radio-button");
        span.getStyle()
                .set("padding", "2px 6px") // Reducido el padding
                .set("border-radius", "8px")
                .set("cursor", "pointer")
                .set("font-weight", "normal")
                .set("transition", "all 0.2s ease")
                .set("border", "1px solid #ccc");

        // Establecer color de fondo según el tipo de asistencia
        span.getStyle().set("background-color", tipo.getColorHex());
        span.getStyle().set("color", getContrastingTextColor(tipo.getColorHex()));

        return span;
    }

    // Establecer el valor seleccionado
    public void setValue(TipoAsistencia tipo) {
        // Solo actualizamos si el valor es distinto
        if (this.selectedTipo != tipo) {
            this.selectedTipo = tipo;
            updateSelectionStyles(tipo);
        }
    }

    // Actualizar estilos de selección
    private void updateSelectionStyles(TipoAsistencia selectedTipo) {
        for (Component component : this.getChildren().collect(Collectors.toList())) {
            if (component instanceof Span) {
                Span span = (Span) component;
                // Comprobamos si el span representa el tipo seleccionado
                if (span.getText().equals(selectedTipo.getAlias())) {
                    span.getStyle()
                            .set("background-color", selectedTipo.getColorHex())
                            .set("color", getContrastingTextColor(selectedTipo.getColorHex()))
                            .set("border", "2px solid " + selectedTipo.getColorHex());

                } else {
                    span.getStyle()
                            .set("background-color", "transparent")
                            .set("color", "black")
                            .set("border", "1px solid #ccc")
                            .set("font-weight", "normal");
                }
            }
        }
    }

    // Obtener el color de texto contrastante
    private String getContrastingTextColor(String hexColor) {
        hexColor = hexColor.replace("#", "");
        int r = Integer.parseInt(hexColor.substring(0, 2), 16);
        int g = Integer.parseInt(hexColor.substring(2, 4), 16);
        int b = Integer.parseInt(hexColor.substring(4, 6), 16);
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        return luminance > 0.5 ? "black" : "white";
    }
}
