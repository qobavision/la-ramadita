package com.laramadita.backend.caja.model;

import com.laramadita.backend.pedidos.model.PedidoDetalle;
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

/** Producto pagado en una división de cuenta por productos (RN-17). Tabla {@code dbo.pago_detalle}. */
@Entity
@Table(name = "pago_detalle")
public class PagoDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pago_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pago_id", nullable = false)
	private Pago pago;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "pedido_detalle_id", nullable = false)
	private PedidoDetalle linea;

	@Column(name = "cantidad_pagada")
	private Integer cantidadPagada;

	@Column(name = "monto_aplicado", nullable = false, precision = 12, scale = 2)
	private BigDecimal montoAplicado;

	protected PagoDetalle() {
	}

	PagoDetalle(Pago pago, PedidoDetalle linea, int cantidad, BigDecimal monto) {
		this.pago = pago;
		this.linea = linea;
		this.cantidadPagada = cantidad;
		this.montoAplicado = monto;
	}

	public PedidoDetalle getLinea() {
		return linea;
	}

	public int getCantidadPagada() {
		return cantidadPagada == null ? 0 : cantidadPagada;
	}

	public BigDecimal getMontoAplicado() {
		return montoAplicado;
	}
}
