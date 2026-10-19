package com.laramadita.backend.compras.model;

import com.laramadita.backend.proveedores.model.Proveedor;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Nationalized;

/** Cabecera de compra. Tabla {@code dbo.compras} de V1, extendida en V7. */
@Entity
@Table(name = "compras")
public class Compra {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "compra_id")
	private Integer id;

	@Nationalized
	@Column(name = "codigo", nullable = false, length = 20, unique = true)
	private String codigo;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "proveedor_id", nullable = false)
	private Proveedor proveedor;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario registradoPor;

	@Column(name = "fecha_compra", nullable = false, updatable = false)
	private LocalDateTime fechaCompra;

	@Column(name = "fecha_recepcion")
	private LocalDateTime fechaRecepcion;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 30)
	private EstadoCompra estado = EstadoCompra.PENDIENTE;

	@Nationalized
	@Column(name = "observacion", length = 500)
	private String observacion;

	@OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CompraDetalle> detalles = new ArrayList<>();

	protected Compra() {
	}

	public Compra(String codigo, Proveedor proveedor, Usuario registradoPor) {
		this.codigo = codigo;
		this.proveedor = proveedor;
		this.registradoPor = registradoPor;
		this.estado = EstadoCompra.PENDIENTE;
	}

	@PrePersist
	void alCrear() {
		if (fechaCompra == null) {
			fechaCompra = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void reemplazarProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public void reemplazarDetalles(List<CompraDetalle> nuevos) {
		detalles.clear();
		nuevos.forEach(this::agregarDetalle);
	}

	public void agregarDetalle(CompraDetalle detalle) {
		detalle.asignarCompra(this);
		detalles.add(detalle);
	}

	public void exigirEditable() {
		if (estado != EstadoCompra.PENDIENTE || tieneRecepcion()) {
			throw new ReglaNegocioException("Solo se puede editar una compra pendiente sin recepción");
		}
	}

	public void exigirCancelable() {
		if (estado != EstadoCompra.PENDIENTE || tieneRecepcion()) {
			throw new ReglaNegocioException("Solo se puede cancelar una compra pendiente sin recepción");
		}
	}

	public void exigirRecepcionable() {
		if (estado == EstadoCompra.CANCELADA) {
			throw new ReglaNegocioException("No se puede recibir una compra cancelada");
		}
		if (estado == EstadoCompra.RECIBIDA) {
			throw new ReglaNegocioException("La compra ya fue recibida por completo");
		}
	}

	public void cancelar() {
		exigirCancelable();
		this.estado = EstadoCompra.CANCELADA;
	}

	public void registrarRecepcion() {
		if (fechaRecepcion == null) {
			fechaRecepcion = LocalDateTime.now(ZoneOffset.UTC);
		}
		recalcularEstado();
	}

	public void recalcularEstado() {
		if (estado == EstadoCompra.CANCELADA) {
			return;
		}
		boolean hayRecibido = detalles.stream().anyMatch(CompraDetalle::tieneRecepcion);
		boolean todoRecibido = !detalles.isEmpty() && detalles.stream().allMatch(CompraDetalle::estaCompleto);
		if (todoRecibido) {
			estado = EstadoCompra.RECIBIDA;
		} else if (hayRecibido) {
			estado = EstadoCompra.PARCIAL;
		} else {
			estado = EstadoCompra.PENDIENTE;
		}
	}

	public boolean tieneRecepcion() {
		return detalles.stream().anyMatch(CompraDetalle::tieneRecepcion);
	}

	public BigDecimal total() {
		return detalles.stream().map(CompraDetalle::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public Integer getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public Usuario getRegistradoPor() {
		return registradoPor;
	}

	public LocalDateTime getFechaCompra() {
		return fechaCompra;
	}

	public LocalDateTime getFechaRecepcion() {
		return fechaRecepcion;
	}

	public EstadoCompra getEstado() {
		return estado;
	}

	public String getObservacion() {
		return observacion;
	}

	public List<CompraDetalle> getDetalles() {
		return detalles;
	}
}
