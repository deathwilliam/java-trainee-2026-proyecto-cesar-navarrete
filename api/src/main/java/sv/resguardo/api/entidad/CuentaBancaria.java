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
import jakarta.persistence.UniqueConstraint;
import sv.resguardo.api.entidad.enums.TipoCuenta;

import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta_bancaria",
        uniqueConstraints = @UniqueConstraint(columnNames = {"banco_id", "numero_cuenta"}))
public class CuentaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "banco_id", nullable = false)
    private Banco banco;

    @Column(name = "numero_cuenta", length = 20, nullable = false)
    private String numeroCuenta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cuenta", length = 10, nullable = false)
    private TipoCuenta tipoCuenta;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    protected CuentaBancaria() {
    }

    public CuentaBancaria(Cliente cliente, Banco banco, String numeroCuenta, TipoCuenta tipoCuenta) {
        this.cliente = cliente;
        this.banco = banco;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Banco getBanco() { return banco; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public boolean isActivo() { return activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }

    public void setActivo(boolean activo) { this.activo = activo; }
}
