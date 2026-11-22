package com.laramadita.backend.pedidos.model;

import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.shared.error.ConflictoException;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.hibernate.annotations.Nationalized;

/**
 * Línea de pedido. Tabla {@code dbo.pedido_detalle} de V1. Guarda el precio
 * histórico del momento en que se agregó (la columna {@code subtotal} es
 * calculada en la base y aquí se obtiene con {@link #subtotal()}).
 */
@Entity
@Table(name = "pedido_detalle")
public class PedidoDetalle {

	public static final int CANTIDAD_MAXIMA = 99;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pedido_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_id", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@Column(name = "cantidad", nullable = false)
	private int cantidad;

	@Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
	private BigDecimal precioUnitario;

	/** Nombre del producto al pedirlo (V15): el historial no cambia si el producto se renombra. */
	@Nationalized
	@Column(name = "nombre_producto", length = 150)
	private String nombreProducto;

	@Nationalized
	@Column(name = "observacion", length = 500)
	private String observacion;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado_cocina", nullable = false, length = 20)
	private EstadoCocina estadoCocina = EstadoCocina.PENDIENTE;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado_item", nullable = false, length = 20)
	private EstadoItem estadoItem = EstadoItem.ACTIVO;

	/** M08: quién marcó la línea como preparada y cuándo (trazabilidad). */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "preparado_por_usuario_id")
	private Usuario preparadoPor;

	@Column(name = "fecha_preparado")
	private LocalDateTime fechaPreparado;

	/** RN-06: cambios y cancelaciones posteriores al envío a Cocina. */
	@OneToMany(mappedBy = "detalle", cascade = CascadeType.ALL)
	@OrderBy("id ASC")
	private List<PedidoHistorial> historial = new ArrayList<>();

	protected PedidoDetalle() {
	}

	/**
	 * M08 / RN-13: Cocina marca la línea como Preparada. Solo aplica a líneas
	 * activas de un pedido ya enviado; queda registrado quién y cuándo.
	 */
	public void marcarPreparado(Usuario cocinero) {
		if (!pedido.enviado()) {
			throw new ReglaNegocioException(producto.getNombre() + " todavía no fue enviado a Cocina");
		}
		if (!activo()) {
			throw new ReglaNegocioException(producto.getNombre() + " fue cancelado y no debe prepararse");
		}
		if (estadoCocina == EstadoCocina.PREPARADO) {
			throw new ConflictoException(producto.getNombre() + " ya fue marcado como preparado");
		}
		this.estadoCocina = EstadoCocina.PREPARADO;
		this.preparadoPor = cocinero;
		this.fechaPreparado = LocalDateTime.now(ZoneOffset.UTC);
	}

	/**
	 * RN-06: cambio de cantidad u observación después del envío. Solo mientras
	 * Cocina no lo preparó; queda en el historial. Devuelve la descripción del
	 * cambio, o vacío si no cambió nada.
	 */
	Optional<String> modificarEnviado(int nuevaCantidad, String nuevaObservacion, Usuario usuario) {
		exigirCambiableEnCocina();
		int cantidadNueva = exigirCantidad(nuevaCantidad);
		String observacionNueva = limpiar(nuevaObservacion);
		boolean cambiaCantidad = cantidadNueva != cantidad;
		boolean cambiaObservacion = !Objects.equals(observacionNueva, observacion);
		if (!cambiaCantidad && !cambiaObservacion) {
			return Optional.empty();
		}
		List<String> partes = new ArrayList<>();
		if (cambiaCantidad) {
			partes.add(cantidad + " → " + cantidadNueva);
		}
		if (cambiaObservacion) {
			partes.add(observacionNueva == null ? "sin observación" : "\"" + observacionNueva + "\"");
		}
		String texto = producto.getNombre() + ": " + String.join(", ", partes);
		historial.add(PedidoHistorial.modificacion(this, usuario, cantidad, cantidadNueva, texto, null));
		this.cantidad = cantidadNueva;
		this.observacion = observacionNueva;
		return Optional.of(texto);
	}

	/** RN-06: cancelación después del envío. La línea se conserva como CANCELADO (trazabilidad). */
	void cancelarEnviado(Usuario usuario) {
		exigirCambiableEnCocina();
		historial.add(PedidoHistorial.cancelacion(this, usuario, null));
		this.estadoItem = EstadoItem.CANCELADO;
	}

	private void exigirCambiableEnCocina() {
		if (!activo()) {
			throw new ReglaNegocioException(producto.getNombre() + " ya fue cancelado");
		}
		if (estadoCocina == EstadoCocina.PREPARADO) {
			throw new ReglaNegocioException(
					producto.getNombre() + " ya fue preparado en Cocina; no se puede modificar ni cancelar");
		}
	}

	/** Línea que Cocina todavía tiene que preparar. */
	public boolean pendienteEnCocina() {
		return activo() && estadoCocina == EstadoCocina.PENDIENTE;
	}

	/** Último cambio de cantidad u observación después del envío (RN-06), si lo hubo. */
	public Optional<PedidoHistorial> ultimaModificacion() {
		return historial.stream()
				.filter(h -> h.getTipoEvento() == TipoEventoPedido.MODIFICACION)
				.reduce((primero, segundo) -> segundo);
	}

	public Optional<PedidoHistorial> cancelacion() {
		return historial.stream().filter(h -> h.getTipoEvento() == TipoEventoPedido.CANCELACION).findFirst();
	}

	PedidoDetalle(Pedido pedido, Producto producto, int cantidad, BigDecimal precioUnitario, String observacion) {
		this.pedido = pedido;
		this.producto = producto;
		this.nombreProducto = producto.getNombre();
		this.cantidad = exigirCantidad(cantidad);
		this.precioUnitario = precioUnitario;
		this.observacion = limpiar(observacion);
	}

	void actualizar(int cantidad, String observacion) {
		this.cantidad = exigirCantidad(cantidad);
		this.observacion = limpiar(observacion);
	}

	void sumar(int cantidad) {
		this.cantidad = exigirCantidad(this.cantidad + cantidad);
	}

	boolean coincide(Producto otro, String otraObservacion) {
		return producto.getId().equals(otro.getId()) && Objects.equals(observacion, limpiar(otraObservacion));
	}

	public boolean activo() {
		return estadoItem == EstadoItem.ACTIVO;
	}

	public BigDecimal subtotal() {
		return activo() ? precioUnitario.multiply(BigDecimal.valueOf(cantidad)) : BigDecimal.ZERO;
	}

	static int exigirCantidad(int cantidad) {
		if (cantidad < 1 || cantidad > CANTIDAD_MAXIMA) {
			throw new ReglaNegocioException("La cantidad debe estar entre 1 y " + CANTIDAD_MAXIMA);
		}
		return cantidad;
	}

	static String limpiar(String texto) {
		if (texto == null || texto.isBlank()) {
			return null;
		}
		String limpio = texto.trim().replaceAll("\\s+", " ");
		if (limpio.length() > 500) {
			throw new ReglaNegocioException("La observación no puede superar 500 caracteres");
		}
		return limpio;
	}

	public Integer getId() {
		return id;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public Producto getProducto() {
		return producto;
	}

	/** Nombre histórico de la línea; las líneas previas a V15 usan el nombre actual. */
	public String nombre() {
		return nombreProducto != null ? nombreProducto : producto.getNombre();
	}

	public int getCantidad() {
		return cantidad;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public String getObservacion() {
		return observacion;
	}

	public EstadoCocina getEstadoCocina() {
		return estadoCocina;
	}

	public EstadoItem getEstadoItem() {
		return estadoItem;
	}

	public Usuario getPreparadoPor() {
		return preparadoPor;
	}

	public LocalDateTime getFechaPreparado() {
		return fechaPreparado;
	}

	public List<PedidoHistorial> getHistorial() {
		return historial;
	}
}
