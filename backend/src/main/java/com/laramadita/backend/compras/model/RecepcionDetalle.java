package com.laramadita.backend.compras.model;

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

@Entity
@Table(name = "recepcion_detalle")
public class RecepcionDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recepcion_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recepcion_id", nullable = false)
	private Recepcion recepcion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "compra_detalle_id", nullable = false)
	private CompraDetalle compraDetalle;

	@Column(name = "cantidad", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidad;

	protected RecepcionDetalle() {
	}

	public RecepcionDetalle(CompraDetalle compraDetalle, BigDecimal cantidad) {
		this.compraDetalle = compraDetalle;
		this.cantidad = cantidad;
	}

	void asignarRecepcion(Recepcion recepcion) {
		this.recepcion = recepcion;
	}

	public Integer getId() {
		return id;
	}

	public Recepcion getRecepcion() {
		return recepcion;
	}

	public CompraDetalle getCompraDetalle() {
		return compraDetalle;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}
}
