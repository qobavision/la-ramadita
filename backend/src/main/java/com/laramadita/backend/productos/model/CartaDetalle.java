package com.laramadita.backend.productos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
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
import java.math.BigDecimal;
import org.hibernate.annotations.Nationalized;

/** Línea de carta. Tabla {@code dbo.carta_detalle} de V1, extendida en V8. */
@Entity
@Table(name = "carta_detalle")
public class CartaDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "carta_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "carta_id", nullable = false)
	private Carta carta;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "seccion", nullable = false, length = 40)
	private SeccionCarta seccion;

	@Column(name = "precio_dia", precision = 12, scale = 2)
	private BigDecimal precioDia;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado_producto", nullable = false, length = 20)
	private EstadoProductoCarta estado = EstadoProductoCarta.DISPONIBLE;

	protected CartaDetalle() {
	}

	public CartaDetalle(Producto producto, SeccionCarta seccion, BigDecimal precioDia, EstadoProductoCarta estado) {
		this.producto = producto;
		this.seccion = seccion;
		this.precioDia = exigirPrecioDia(precioDia);
		this.estado = estado == null ? EstadoProductoCarta.DISPONIBLE : estado;
	}

	void asignarCarta(Carta carta) {
		this.carta = carta;
	}

	public void cambiarEstado(EstadoProductoCarta estado) {
		this.estado = estado;
	}

	void actualizar(BigDecimal precioDia, EstadoProductoCarta estado) {
		this.precioDia = exigirPrecioDia(precioDia);
		if (estado != null) {
			this.estado = estado;
		}
	}

	public BigDecimal precioEfectivo() {
		return precioDia != null ? precioDia : producto.getPrecio();
	}

	private static BigDecimal exigirPrecioDia(BigDecimal valor) {
		if (valor == null) {
			return null;
		}
		if (valor.compareTo(BigDecimal.ZERO) < 0) {
			throw new ReglaNegocioException("El precio del día no puede ser negativo");
		}
		return valor;
	}

	public Integer getId() {
		return id;
	}

	public Carta getCarta() {
		return carta;
	}

	public Producto getProducto() {
		return producto;
	}

	public SeccionCarta getSeccion() {
		return seccion;
	}

	public BigDecimal getPrecioDia() {
		return precioDia;
	}

	public EstadoProductoCarta getEstado() {
		return estado;
	}
}
