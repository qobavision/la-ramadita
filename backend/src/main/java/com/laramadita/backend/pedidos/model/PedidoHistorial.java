package com.laramadita.backend.pedidos.model;

import com.laramadita.backend.usuarios.model.Usuario;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

/**
 * Cambio de una línea ya enviada a Cocina (RN-06). Tabla
 * {@code dbo.pedido_historial} de V1. Cocina lo usa para distinguir
 * productos modificados o cancelados y no preparar información desactualizada.
 */
@Entity
@Table(name = "pedido_historial")
public class PedidoHistorial {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "historial_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_detalle_id", nullable = false)
	private PedidoDetalle detalle;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo_evento", nullable = false, length = 30)
	private TipoEventoPedido tipoEvento;

	@Column(name = "cantidad_anterior")
	private Integer cantidadAnterior;

	@Column(name = "cantidad_nueva")
	private Integer cantidadNueva;

	@Nationalized
	@Column(name = "estado_anterior", length = 30)
	private String estadoAnterior;

	@Nationalized
	@Column(name = "estado_nuevo", length = 30)
	private String estadoNuevo;

	@Nationalized
	@Column(name = "detalle_cambio", length = 500)
	private String detalleCambio;

	@Nationalized
	@Column(name = "motivo", length = 500)
	private String motivo;

	@Column(name = "fecha_evento", nullable = false)
	private LocalDateTime fechaEvento;

	protected PedidoHistorial() {
	}

	private PedidoHistorial(PedidoDetalle detalle, Usuario usuario, TipoEventoPedido tipoEvento) {
		this.detalle = detalle;
		this.usuario = usuario;
		this.tipoEvento = tipoEvento;
		this.fechaEvento = LocalDateTime.now(ZoneOffset.UTC);
	}

	/** Cambio de cantidad u observación de una línea enviada. */
	public static PedidoHistorial modificacion(PedidoDetalle detalle, Usuario usuario, int cantidadAnterior,
			int cantidadNueva, String detalleCambio, String motivo) {
		PedidoHistorial h = new PedidoHistorial(detalle, usuario, TipoEventoPedido.MODIFICACION);
		h.cantidadAnterior = cantidadAnterior;
		h.cantidadNueva = cantidadNueva;
		h.detalleCambio = recortar(detalleCambio);
		h.motivo = recortar(motivo);
		return h;
	}

	private static String recortar(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		String limpio = texto.trim();
		return limpio.length() > 500 ? limpio.substring(0, 499) + "…" : limpio;
	}

	/** Cancelación de una línea enviada (pasa de ACTIVO a CANCELADO). */
	public static PedidoHistorial cancelacion(PedidoDetalle detalle, Usuario usuario, String motivo) {
		PedidoHistorial h = new PedidoHistorial(detalle, usuario, TipoEventoPedido.CANCELACION);
		h.cantidadAnterior = detalle.getCantidad();
		h.estadoAnterior = EstadoItem.ACTIVO.name();
		h.estadoNuevo = EstadoItem.CANCELADO.name();
		h.motivo = recortar(motivo);
		return h;
	}

	public Integer getId() {
		return id;
	}

	public PedidoDetalle getDetalle() {
		return detalle;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public TipoEventoPedido getTipoEvento() {
		return tipoEvento;
	}

	public Integer getCantidadAnterior() {
		return cantidadAnterior;
	}

	public Integer getCantidadNueva() {
		return cantidadNueva;
	}

	public String getEstadoAnterior() {
		return estadoAnterior;
	}

	public String getEstadoNuevo() {
		return estadoNuevo;
	}

	public String getDetalleCambio() {
		return detalleCambio;
	}

	public String getMotivo() {
		return motivo;
	}

	public LocalDateTime getFechaEvento() {
		return fechaEvento;
	}
}
