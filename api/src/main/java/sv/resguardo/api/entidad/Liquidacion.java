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
import sv.resguardo.api.entidad.enums.TipoLiquidacion;

import java.time.LocalDateTime;

@Entity
@Table(name = "liquidacion")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private ContratoGarantia contrato;

    @Column(name = "numero_comprobante", length = 15, nullable = false, unique = true)
    private String numeroComprobante;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TipoLiquidacion tipo;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    protected Liquidacion() {
    }

    public Liquidacion(ContratoGarantia contrato, String numeroComprobante, TipoLiquidacion tipo) {
        this.contrato = contrato;
        this.numeroComprobante = numeroComprobante;
        this.tipo = tipo;
    }

    public Long getId() { return id; }
    public ContratoGarantia getContrato() { return contrato; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public TipoLiquidacion getTipo() { return tipo; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
