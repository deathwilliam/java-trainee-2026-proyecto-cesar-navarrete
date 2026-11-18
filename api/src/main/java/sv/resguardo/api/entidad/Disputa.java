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
import sv.resguardo.api.entidad.enums.EstadoDisputa;
import sv.resguardo.api.entidad.enums.MotivoDisputa;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputa")
public class Disputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private ContratoGarantia contrato;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MotivoDisputa motivo;

    @Column(length = 500, nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private EstadoDisputa estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asesor_id")
    private Asesor asesor;

    @Column(length = 500)
    private String resolucion;

    @Column(name = "abierta_en", nullable = false, updatable = false)
    private LocalDateTime abiertaEn = LocalDateTime.now();

    @Column(name = "resuelta_en")
    private LocalDateTime resueltaEn;

    protected Disputa() {
    }

    public Disputa(ContratoGarantia contrato, MotivoDisputa motivo, String descripcion) {
        this.contrato = contrato;
        this.motivo = motivo;
        this.descripcion = descripcion;
        this.estado = EstadoDisputa.ABIERTA;
    }

    public Long getId() { return id; }
    public ContratoGarantia getContrato() { return contrato; }
    public MotivoDisputa getMotivo() { return motivo; }
    public String getDescripcion() { return descripcion; }
    public EstadoDisputa getEstado() { return estado; }
    public Asesor getAsesor() { return asesor; }
    public String getResolucion() { return resolucion; }
    public LocalDateTime getAbiertaEn() { return abiertaEn; }
    public LocalDateTime getResueltaEn() { return resueltaEn; }

    public void setEstado(EstadoDisputa estado) { this.estado = estado; }
    public void setAsesor(Asesor asesor) { this.asesor = asesor; }
    public void setResolucion(String resolucion) { this.resolucion = resolucion; }
    public void setResueltaEn(LocalDateTime resueltaEn) { this.resueltaEn = resueltaEn; }
}
