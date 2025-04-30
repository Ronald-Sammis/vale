package pe.com.sammis.vale.models.dto;

public class EmpleadoDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;

    // Constructor (con o sin argumentos)
    public EmpleadoDTO() {
    }

    public EmpleadoDTO(Long id, String nombre, String apellido, String dni) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
    }

    // Getters y Setters
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

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}