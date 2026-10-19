package com.laramadita.backend.productos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** Combo: un producto compuesto por otros productos. Tabla {@code dbo.producto_componente} de V8. */
@Entity
@Table(name = "producto_componente")
public class ProductoComponente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "producto_componente_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "componente_producto_id", nullable = false)
	private Producto componente;

	@Column(name = "cantidad", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidad;

	protected ProductoComponente() {
	}

	public ProductoComponente(Producto componente, BigDecimal cantidad) {
		if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad del componente debe ser mayor a 0");
		}
		this.componente = componente;
		this.cantidad = cantidad;
	}

	void asignarProducto(Producto producto) {
		this.producto = producto;
	}

	void cambiarCantidad(BigDecimal cantidad) {
		if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad del componente debe ser mayor a 0");
		}
		this.cantidad = cantidad;
	}

	public Integer getId() {
		return id;
	}

	public Producto getProducto() {
		return producto;
	}

	public Producto getComponente() {
		return componente;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}
}
