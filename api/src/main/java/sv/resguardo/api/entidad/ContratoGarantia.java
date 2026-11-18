package sv.resguardo.api.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import sv.resguardo.api.entidad.enums.EstadoContrato;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "contrato_garantia")
public class ContratoGarantia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_contrato", length = 15, nullable = false, unique = true)
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Cliente vendedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comprador_id", nullable = false)
    private Cliente comprador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_pago_id", nullable = false)
    private CuentaBancaria cuentaPago;

    @Column(length = 255, nullable = false)
    private String descripcion;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal monto;

    // Copia de la comision de la categoria al crear el contrato
    @Column(name = "porcentaje_comision", precision = 6, scale = 4, nullable = false, updatable = false)
    private BigDecimal porcentajeComision;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private EstadoContrato estado;

    @Column(name = "empresa_envio", length = 60)
    private String empresaEnvio;

    @Column(name = "numero_guia", length = 30)
    private String numeroGuia;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected ContratoGarantia() {
    }

    public ContratoGarantia(String numeroContrato, Cliente vendedor, Cliente comprador,
                            Categoria categoria, CuentaBancaria cuentaPago,
                            String descripcion, BigDecimal monto) {
        this.numeroContrato = numeroContrato;
        this.vendedor = vendedor;
        this.comprador = comprador;
        this.categoria = categoria;
        this.cuentaPago = cuentaPago;
        this.descripcion = descripcion;
        this.monto = monto;
        this.porcentajeComision = categoria.getPorcentajeComision();
        this.estado = EstadoContrato.CREADO;
    }

    public Long getId() { return id; }
    public String getNumeroContrato() { return numeroContrato; }
    public Cliente getVendedor() { return vendedor; }
    public Cliente getComprador() { return comprador; }
    public Categoria getCategoria() { return categoria; }
    public CuentaBancaria getCuentaPago() { return cuentaPago; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getPorcentajeComision() { return porcentajeComision; }
    public EstadoContrato getEstado() { return estado; }
    public String getEmpresaEnvio() { return empresaEnvio; }
    public String getNumeroGuia() { return numeroGuia; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public LocalDateTime getCreadoEn() { return creadoEn; }

    public void setEstado(EstadoContrato estado) { this.estado = estado; }
    public void setEmpresaEnvio(String empresaEnvio) { this.empresaEnvio = empresaEnvio; }
    public void setNumeroGuia(String numeroGuia) { this.numeroGuia = numeroGuia; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
