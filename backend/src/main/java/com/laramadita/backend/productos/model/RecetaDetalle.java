package com.laramadita.backend.productos.model;

import com.laramadita.backend.insumos.model.Insumo;
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

/** Línea de receta. Tabla {@code dbo.receta_detalle} de V1. */
@Entity
@Table(name = "receta_detalle")
public class RecetaDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "receta_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "producto_id", nullable = false)
	private Producto producto;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "insumo_id", nullable = false)
	private Insumo insumo;

	@Column(name = "cantidad_requerida", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidadRequerida;

	protected RecetaDetalle() {
	}

	public RecetaDetalle(Insumo insumo, BigDecimal cantidadRequerida) {
		if (cantidadRequerida == null || cantidadRequerida.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
		}
		this.insumo = insumo;
		this.cantidadRequerida = cantidadRequerida;
	}

	void asignarProducto(Producto producto) {
		this.producto = producto;
	}

	void cambiarCantidad(BigDecimal cantidadRequerida) {
		if (cantidadRequerida == null || cantidadRequerida.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
		}
		this.cantidadRequerida = cantidadRequerida;
	}

	public Integer getId() {
		return id;
	}

	public Producto getProducto() {
		return producto;
	}

	public Insumo getInsumo() {
		return insumo;
	}

	public BigDecimal getCantidadRequerida() {
		return cantidadRequerida;
	}
}
