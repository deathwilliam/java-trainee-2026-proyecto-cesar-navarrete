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
@Table(name = "deposito")
public class Deposito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private ContratoGarantia contrato;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    private CuentaBancaria cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asesor_id", nullable = false)
    private Asesor asesor;

    @Column(length = 30, nullable = false, unique = true)
    private String referencia;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected Deposito() {
    }

    public Deposito(ContratoGarantia contrato, CuentaBancaria cuentaOrigen, Asesor asesor, String referencia) {
        this.contrato = contrato;
        this.cuentaOrigen = cuentaOrigen;
        this.asesor = asesor;
        this.referencia = referencia;
    }

    public Long getId() { return id; }
    public ContratoGarantia getContrato() { return contrato; }
    public CuentaBancaria getCuentaOrigen() { return cuentaOrigen; }
    public Asesor getAsesor() { return asesor; }
    public String getReferencia() { return referencia; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
