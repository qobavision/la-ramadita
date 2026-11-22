package com.laramadita.backend.pedidos.model;

import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
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
import java.util.Optional;
import org.hibernate.annotations.Nationalized;

/**
 * Pedido (envío) dentro de una atención. Tabla {@code dbo.pedidos} de V1.
 * Mientras está en BORRADOR se edita libremente (RN-04); al enviarse a Cocina
 * queda registrado con sus cantidades y observaciones (RN-05).
 */
@Entity
@Table(name = "pedidos")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pedido_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "atencion_id", nullable = false)
	private Atencion atencion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "fecha_creacion", nullable = false)
	private LocalDateTime fechaCreacion;

	@Column(name = "fecha_envio")
	private LocalDateTime fechaEnvio;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 30)
	private EstadoPedido estado = EstadoPedido.BORRADOR;

	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<PedidoDetalle> detalles = new ArrayList<>();

	protected Pedido() {
	}

	Pedido(Atencion atencion, Usuario usuario) {
		this.atencion = atencion;
		this.usuario = usuario;
		this.fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoPedido.BORRADOR;
	}

	public boolean esBorrador() {
		return estado == EstadoPedido.BORRADOR;
	}

	public boolean enviado() {
		return estado == EstadoPedido.ENVIADO_COCINA || estado == EstadoPedido.CERRADO;
	}

	/** Agrega un producto. Si ya está con la misma observación, suma la cantidad. */
	void agregar(Producto producto, int cantidad, BigDecimal precio, String observacion) {
		exigirBorrador();
		for (PedidoDetalle linea : detalles) {
			if (linea.coincide(producto, observacion)) {
				linea.sumar(cantidad);
				return;
			}
		}
		detalles.add(new PedidoDetalle(this, producto, cantidad, precio, observacion));
	}

	/** RN-04 en borrador (edición libre); RN-06 si ya está en Cocina (queda en el historial). */
	Optional<CambioEnCocina> actualizar(Integer detalleId, int cantidad, String observacion, Usuario usuario) {
		PedidoDetalle linea = detalle(detalleId);
		if (esBorrador()) {
			linea.actualizar(cantidad, observacion);
			return Optional.empty();
		}
		exigirEnCocina();
		return linea.modificarEnviado(cantidad, observacion, usuario)
				.map(texto -> CambioEnCocina.de(CambioEnCocina.Tipo.MODIFICADO, this, texto));
	}

	/**
	 * En borrador se quita la línea. Si ya está en Cocina se cancela (RN-06); si
	 * no queda ningún producto activo, el pedido completo pasa a CANCELADO.
	 */
	Optional<CambioEnCocina> quitar(Integer detalleId, Usuario usuario) {
		PedidoDetalle linea = detalle(detalleId);
		if (esBorrador()) {
			detalles.remove(linea);
			return Optional.empty();
		}
		exigirEnCocina();
		linea.cancelarEnviado(usuario);
		if (detalles.stream().noneMatch(PedidoDetalle::activo)) {
			this.estado = EstadoPedido.CANCELADO;
			return Optional.of(CambioEnCocina.de(CambioEnCocina.Tipo.CANCELADO, this, "Pedido cancelado"));
		}
		return Optional.of(CambioEnCocina.de(CambioEnCocina.Tipo.MODIFICADO, this,
				linea.nombre() + ": cancelado"));
	}

	private void exigirEnCocina() {
		if (estado != EstadoPedido.ENVIADO_COCINA) {
			throw new ReglaNegocioException("El pedido ya no admite cambios");
		}
	}

	boolean contiene(Integer detalleId) {
		return detalles.stream().anyMatch(d -> d.getId() != null && d.getId().equals(detalleId));
	}

	void enviar() {
		exigirBorrador();
		if (detalles.isEmpty()) {
			throw new ReglaNegocioException("Agrega al menos un producto antes de enviar a Cocina");
		}
		this.estado = EstadoPedido.ENVIADO_COCINA;
		this.fechaEnvio = LocalDateTime.now(ZoneOffset.UTC);
	}

	/** M09: la cuenta de la atención quedó pagada. */
	void cerrar() {
		if (estado == EstadoPedido.ENVIADO_COCINA) {
			this.estado = EstadoPedido.CERRADO;
		}
	}

	/** Se anuló un pago de la cuenta ya pagada: el pedido vuelve a estar enviado. */
	void reabrirPorAnulacion() {
		if (estado == EstadoPedido.CERRADO) {
			this.estado = EstadoPedido.ENVIADO_COCINA;
		}
	}

	public BigDecimal total() {
		return detalles.stream().map(PedidoDetalle::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private PedidoDetalle detalle(Integer detalleId) {
		return detalles.stream()
				.filter(d -> d.getId() != null && d.getId().equals(detalleId))
				.findFirst()
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el producto en el pedido"));
	}

	private void exigirBorrador() {
		if (!esBorrador()) {
			throw new ReglaNegocioException("El pedido ya fue enviado a Cocina y no se puede editar");
		}
	}

	public Integer getId() {
		return id;
	}

	public Atencion getAtencion() {
		return atencion;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public LocalDateTime getFechaEnvio() {
		return fechaEnvio;
	}

	public EstadoPedido getEstado() {
		return estado;
	}

	public List<PedidoDetalle> getDetalles() {
		return detalles;
	}
}
