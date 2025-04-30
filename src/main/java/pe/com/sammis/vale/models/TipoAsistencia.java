package pe.com.sammis.vale.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tb_tipo_asistencias")
public class TipoAsistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String alias;

    @Column(nullable = false)
    private String colorHex;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }


    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return "TipoAsistencia{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", colorHex='" + colorHex + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        TipoAsistencia that = (TipoAsistencia) object;
        return Objects.equals(id, that.id) && Objects.equals(nombre, that.nombre) && Objects.equals(alias, that.alias) && Objects.equals(colorHex, that.colorHex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nombre, alias, colorHex);
    }
}
