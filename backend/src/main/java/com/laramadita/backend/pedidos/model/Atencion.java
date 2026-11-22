package com.laramadita.backend.pedidos.model;

import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.zonas.model.Mesa;
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
import java.util.Optional;
import org.hibernate.annotations.Nationalized;

/**
 * Atención: toda la visita de una mesa o un pedido para llevar (M06).
 * Tabla {@code dbo.atenciones} de V1. Puede tener varios pedidos (envíos).
 */
@Entity
@Table(name = "atenciones")
public class Atencion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "atencion_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "mesa_id")
	private Mesa mesa;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turno_id", nullable = false)
	private Turno turno;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "mesero_id", nullable = false)
	private Usuario mesero;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo_atencion", nullable = false, length = 20)
	private TipoAtencion tipo;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDateTime fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDateTime fechaFin;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 30)
	private EstadoAtencion estado = EstadoAtencion.ABIERTA;

	@OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<Pedido> pedidos = new ArrayList<>();

	protected Atencion() {
	}

	private Atencion(TipoAtencion tipo, Mesa mesa, Turno turno, Usuario mesero) {
		this.tipo = tipo;
		this.mesa = mesa;
		this.turno = turno;
		this.mesero = mesero;
		this.fechaInicio = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoAtencion.ABIERTA;
	}

	/** RN-01: la mesa debe estar Disponible; al iniciar la atención pasa a Ocupada. */
	public static Atencion enMesa(Mesa mesa, Turno turno, Usuario mesero) {
		if (!mesa.disponible()) {
			throw new ReglaNegocioException(mesa.nombre() + " no está disponible");
		}
		mesa.ocupar();
		return new Atencion(TipoAtencion.MESA, mesa, turno, mesero);
	}

	/** RN-03: pedido sin mesa con identificación propia. */
	public static Atencion paraLlevar(Turno turno, Usuario mesero) {
		return new Atencion(TipoAtencion.PARA_LLEVAR, null, turno, mesero);
	}

	public Optional<Pedido> borrador() {
		return pedidos.stream().filter(Pedido::esBorrador).findFirst();
	}

	public List<Pedido> enviados() {
		return pedidos.stream().filter(Pedido::enviado).toList();
	}

	public void agregar(Producto producto, int cantidad, BigDecimal precio, String observacion, Usuario usuario) {
		exigirAbierta();
		Pedido pedido = borrador().orElseGet(() -> {
			Pedido nuevo = new Pedido(this, usuario);
			pedidos.add(nuevo);
			return nuevo;
		});
		pedido.agregar(producto, cantidad, precio, observacion);
	}

	/** Devuelve el cambio para Cocina cuando la línea ya había sido enviada (RN-06). */
	public Optional<CambioEnCocina> actualizarDetalle(Integer detalleId, int cantidad, String observacion,
			Usuario usuario) {
		exigirAbierta();
		return pedidoConDetalle(detalleId).actualizar(detalleId, cantidad, observacion, usuario);
	}

	/** En borrador quita la línea; si ya fue enviada la cancela y devuelve el cambio para Cocina (RN-06). */
	public Optional<CambioEnCocina> quitarDetalle(Integer detalleId, Usuario usuario) {
		exigirAbierta();
		Pedido pedido = pedidoConDetalle(detalleId);
		Optional<CambioEnCocina> cambio = pedido.quitar(detalleId, usuario);
		if (pedido.esBorrador() && pedido.getDetalles().isEmpty()) {
			pedidos.remove(pedido);
		}
		return cambio;
	}

	/** RN-05: el borrador deja de serlo y queda registrado para Cocina. */
	public Pedido enviarBorrador() {
		exigirAbierta();
		Pedido pedido = borrador()
				.orElseThrow(() -> new ReglaNegocioException("No hay productos en borrador para enviar a Cocina"));
		pedido.enviar();
		return pedido;
	}

	/** El mesero pide la cuenta: la atención y la mesa quedan pendientes de pago para Caja (M09). */
	public void solicitarCuenta() {
		exigirAbierta();
		if (borrador().filter(p -> !p.getDetalles().isEmpty()).isPresent()) {
			throw new ReglaNegocioException("Envía a Cocina o quita los productos en borrador antes de pedir la cuenta");
		}
		if (enviados().isEmpty()) {
			throw new ReglaNegocioException("La atención no tiene consumo. Usa \"Liberar sin consumo\".");
		}
		this.estado = EstadoAtencion.PENDIENTE_PAGO;
		if (mesa != null) {
			mesa.pasarAPendientePago();
		}
		// Consumo de S/ 0 (cortesías): no hay nada que cobrar, se cierra y libera la mesa.
		if (totalEnviado().signum() == 0) {
			cerrarPorPago();
		}
	}

	/**
	 * RN-07: después de pedir la cuenta el cliente pide algo más. La atención
	 * vuelve a ABIERTA (los pagos parciales ya registrados se conservan) y la
	 * mesa a OCUPADA.
	 */
	public void reabrir() {
		if (estado != EstadoAtencion.PENDIENTE_PAGO) {
			throw new ReglaNegocioException("Solo se puede reabrir una cuenta pendiente de pago");
		}
		this.estado = EstadoAtencion.ABIERTA;
		if (mesa != null) {
			mesa.ocupar();
		}
	}

	/** Atención sin nada enviado a Cocina (el cliente se retiró): se cierra y libera la mesa. */
	public void liberarSinConsumo() {
		exigirAbierta();
		if (!enviados().isEmpty()) {
			throw new ReglaNegocioException("La atención ya tiene pedidos enviados a Cocina; debe cobrarse en Caja");
		}
		// Solo se descarta el borrador: los pedidos cancelados (RN-06) se conservan como historial.
		borrador().ifPresent(pedidos::remove);
		this.estado = EstadoAtencion.CERRADA;
		this.fechaFin = LocalDateTime.now(ZoneOffset.UTC);
		if (mesa != null) {
			mesa.liberar();
		}
	}

	/**
	 * M09 / RN-16: Caja registró pagos hasta saldo cero. La atención se cierra,
	 * sus pedidos quedan CERRADO y la mesa vuelve a Disponible (RN-02).
	 */
	public void cerrarPorPago() {
		if (estado != EstadoAtencion.PENDIENTE_PAGO) {
			throw new ReglaNegocioException("La atención no está pendiente de pago");
		}
		enviados().forEach(Pedido::cerrar);
		this.estado = EstadoAtencion.CERRADA;
		this.fechaFin = LocalDateTime.now(ZoneOffset.UTC);
		if (mesa != null) {
			mesa.liberar();
		}
	}

	/**
	 * El ADMINISTRADOR anuló un pago de una cuenta ya pagada: la atención vuelve a
	 * pendiente de pago y la mesa también, salvo que ya la ocupe otra atención.
	 */
	public void reabrirPorAnulacion() {
		if (estado != EstadoAtencion.CERRADA || enviados().isEmpty()) {
			return;
		}
		if (mesa != null) {
			if (!mesa.disponible()) {
				throw new ConflictoException(mesa.nombre()
						+ " ya tiene otra atención. Anula el pago cuando la mesa esté libre.");
			}
			mesa.pasarAPendientePago();
		}
		pedidos.forEach(Pedido::reabrirPorAnulacion);
		this.estado = EstadoAtencion.PENDIENTE_PAGO;
		this.fechaFin = null;
	}

	public BigDecimal totalEnviado() {
		return enviados().stream().map(Pedido::total).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal totalBorrador() {
		return borrador().map(Pedido::total).orElse(BigDecimal.ZERO);
	}

	public String identificacion() {
		return mesa != null ? mesa.nombre() : "Para llevar #" + id;
	}

	public void exigirAbierta() {
		if (estado == EstadoAtencion.PENDIENTE_PAGO) {
			throw new ReglaNegocioException("La cuenta ya fue solicitada; la atención está pendiente de pago");
		}
		if (estado != EstadoAtencion.ABIERTA) {
			throw new ReglaNegocioException("La atención ya está cerrada");
		}
	}

	private Pedido pedidoConDetalle(Integer detalleId) {
		return pedidos.stream()
				.filter(p -> p.contiene(detalleId))
				.findFirst()
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en la atención"));
	}

	public Integer getId() {
		return id;
	}

	public Mesa getMesa() {
		return mesa;
	}

	public Turno getTurno() {
		return turno;
	}

	public Usuario getMesero() {
		return mesero;
	}

	public TipoAtencion getTipo() {
		return tipo;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public EstadoAtencion getEstado() {
		return estado;
	}

	public List<Pedido> getPedidos() {
		return pedidos;
	}
}
