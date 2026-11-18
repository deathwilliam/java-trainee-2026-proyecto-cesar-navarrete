package sv.resguardo.api.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false, unique = true)
    private String codigo;

    @Column(length = 60, nullable = false)
    private String nombre;

    @Column(name = "monto_minimo", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoMinimo;

    @Column(name = "monto_maximo", precision = 12, scale = 2, nullable = false)
    private BigDecimal montoMaximo;

    @Column(name = "porcentaje_comision", precision = 6, scale = 4, nullable = false)
    private BigDecimal porcentajeComision;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected Categoria() {
    }

    public Categoria(String codigo, String nombre, BigDecimal montoMinimo,
                     BigDecimal montoMaximo, BigDecimal porcentajeComision) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.montoMinimo = montoMinimo;
        this.montoMaximo = montoMaximo;
        this.porcentajeComision = porcentajeComision;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getMontoMinimo() { return montoMinimo; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setMontoMinimo(BigDecimal montoMinimo) { this.montoMinimo = montoMinimo; }
    public void setMontoMaximo(BigDecimal montoMaximo) { this.montoMaximo = montoMaximo; }
    public void setPorcentajeComision(BigDecimal porcentajeComision) { this.porcentajeComision = porcentajeComision; }
    public void setActivo(boolean activo) { this.activo = activo; }
}