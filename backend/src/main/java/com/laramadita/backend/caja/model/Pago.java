package com.laramadita.backend.caja.model;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.Usuario;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Nationalized;

/**
 * Pago de una cuenta (RN-14 a RN-17). Tabla {@code dbo.pagos}. Una atención
 * puede recibir varios pagos con métodos distintos; si se paga por productos,
 * el detalle queda en {@link PagoDetalle}.
 */
@Entity
@Table(name = "pagos")
public class Pago {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pago_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "atencion_id", nullable = false)
	private Atencion atencion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "metodo_pago_id", nullable = false)
	private MetodoPago metodo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "caja_sesion_id", nullable = false)
	private CajaSesion sesion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "monto", nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	@Column(name = "fecha_pago", nullable = false)
	private LocalDateTime fecha;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoPago estado = EstadoPago.CONFIRMADO;

	@Nationalized
	@Column(name = "observacion", length = 300)
	private String observacion;

	@OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
	private List<PagoDetalle> detalles = new ArrayList<>();

	/** V15: evidencia de la anulación. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "anulado_por_usuario_id")
	private Usuario anuladoPor;

	@Column(name = "fecha_anulacion")
	private LocalDateTime fechaAnulacion;

	@Nationalized
	@Column(name = "motivo_anulacion", length = 300)
	private String motivoAnulacion;

	protected Pago() {
	}

	public Pago(Atencion atencion, MetodoPago metodo, CajaSesion sesion, Usuario usuario, BigDecimal monto,
			String observacion) {
		if (monto == null || monto.signum() <= 0) {
			throw new ReglaNegocioException("El monto del pago debe ser mayor a cero");
		}
		this.atencion = atencion;
		this.metodo = metodo;
		this.sesion = sesion;
		this.usuario = usuario;
		this.monto = monto;
		this.observacion = observacion == null || observacion.isBlank() ? null : observacion.trim();
		this.fecha = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoPago.CONFIRMADO;
	}

	public void agregarDetalle(PedidoDetalle linea, int cantidad, BigDecimal montoAplicado) {
		detalles.add(new PagoDetalle(this, linea, cantidad, montoAplicado));
	}

	public boolean confirmado() {
		return estado == EstadoPago.CONFIRMADO;
	}

	/** Solo el ADMINISTRADOR anula (lo valida el servicio). El pago se conserva con su evidencia. */
	public void anular(Usuario administrador, String motivo) {
		if (estado != EstadoPago.CONFIRMADO) {
			throw new ReglaNegocioException("El pago ya está anulado");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new ReglaNegocioException("Indica el motivo de la anulación");
		}
		String limpio = motivo.trim();
		this.estado = EstadoPago.ANULADO;
		this.anuladoPor = administrador;
		this.fechaAnulacion = LocalDateTime.now(ZoneOffset.UTC);
		this.motivoAnulacion = limpio.length() > 300 ? limpio.substring(0, 300) : limpio;
	}

	public Usuario getAnuladoPor() {
		return anuladoPor;
	}

	public LocalDateTime getFechaAnulacion() {
		return fechaAnulacion;
	}

	public String getMotivoAnulacion() {
		return motivoAnulacion;
	}

	public Integer getId() {
		return id;
	}

	public Atencion getAtencion() {
		return atencion;
	}

	public MetodoPago getMetodo() {
		return metodo;
	}

	public CajaSesion getSesion() {
		return sesion;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public EstadoPago getEstado() {
		return estado;
	}

	public String getObservacion() {
		return observacion;
	}

	public List<PagoDetalle> getDetalles() {
		return detalles;
	}
}
