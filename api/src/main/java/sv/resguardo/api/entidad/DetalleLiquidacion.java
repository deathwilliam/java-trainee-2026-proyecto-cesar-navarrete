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
import sv.resguardo.api.entidad.enums.ConceptoLiquidacion;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_liquidacion",
        uniqueConstraints = @UniqueConstraint(columnNames = {"liquidacion_id", "concepto"}))
public class DetalleLiquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liquidacion_id", nullable = false)
    private Liquidacion liquidacion;

    @Enumerated(EnumType.STRING)
    @Column(length = 15, nullable = false)
    private ConceptoLiquidacion concepto;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal monto;

    protected DetalleLiquidacion() {
    }

    public DetalleLiquidacion(Liquidacion liquidacion, ConceptoLiquidacion concepto, BigDecimal monto) {
        this.liquidacion = liquidacion;
        this.concepto = concepto;
        this.monto = monto;
    }

    public Long getId() { return id; }
    public Liquidacion getLiquidacion() { return liquidacion; }
    public ConceptoLiquidacion getConcepto() { return concepto; }
    public BigDecimal getMonto() { return monto; }
}
