package sv.resguardo.api.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "asesor")
public class Asesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_empleado", length = 10, nullable = false, unique = true)
    private String codigoEmpleado;

    @Column(length = 60, nullable = false)
    private String nombres;

    @Column(length = 60, nullable = false)
    private String apellidos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Asesor() {
    }

    public Asesor(String codigoEmpleado, String nombres, String apellidos, Usuario usuario) {
        this.codigoEmpleado = codigoEmpleado;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public String getCodigoEmpleado() { return codigoEmpleado; }
    public String getNombres() { return nombres; }
    public String getApellidos() { return apellidos; }
    public Usuario getUsuario() { return usuario; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
