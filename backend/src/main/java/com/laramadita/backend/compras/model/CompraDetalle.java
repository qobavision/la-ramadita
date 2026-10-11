package com.laramadita.backend.compras.model;

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

/** Detalle de compra. El subtotal lo calcula SQL Server (columna persistida). */
@Entity
@Table(name = "compra_detalle")
public class CompraDetalle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "compra_detalle_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "compra_id", nullable = false)
	private Compra compra;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "insumo_id", nullable = false)
	private Insumo insumo;

	@Column(name = "cantidad_comprada", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidadComprada;

	@Column(name = "cantidad_recibida", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidadRecibida = BigDecimal.ZERO;

	@Column(name = "costo_unitario", nullable = false, precision = 18, scale = 4)
	private BigDecimal costoUnitario;

	@Column(name = "subtotal", insertable = false, updatable = false, precision = 18, scale = 4)
	private BigDecimal subtotalPersistido;

	protected CompraDetalle() {
	}

	public CompraDetalle(Insumo insumo, BigDecimal cantidadComprada, BigDecimal costoUnitario) {
		if (cantidadComprada == null || cantidadComprada.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad debe ser mayor a 0");
		}
		if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) < 0) {
			throw new ReglaNegocioException("El precio no puede ser negativo");
		}
		this.insumo = insumo;
		this.cantidadComprada = cantidadComprada;
		this.costoUnitario = costoUnitario;
		this.cantidadRecibida = BigDecimal.ZERO;
	}

	void asignarCompra(Compra compra) {
		this.compra = compra;
	}

	public BigDecimal pendiente() {
		return cantidadComprada.subtract(cantidadRecibida);
	}

	public boolean tieneRecepcion() {
		return cantidadRecibida.compareTo(BigDecimal.ZERO) > 0;
	}

	public boolean estaCompleto() {
		return cantidadRecibida.compareTo(cantidadComprada) >= 0;
	}

	public BigDecimal recibir(BigDecimal cantidad) {
		if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad recibida debe ser mayor a 0");
		}
		if (cantidad.compareTo(pendiente()) > 0) {
			throw new ReglaNegocioException(
					"No puedes recibir más de " + pendiente().stripTrailingZeros().toPlainString()
							+ " pendientes de " + insumo.getNombre());
		}
		this.cantidadRecibida = this.cantidadRecibida.add(cantidad);
		return cantidad;
	}

	public BigDecimal subtotal() {
		if (subtotalPersistido != null) {
			return subtotalPersistido;
		}
		return cantidadComprada.multiply(costoUnitario);
	}

	public String estadoItem() {
		if (!tieneRecepcion()) {
			return "PENDIENTE";
		}
		if (estaCompleto()) {
			return "RECIBIDO";
		}
		return "PARCIAL";
	}

	public Integer getId() {
		return id;
	}

	public Compra getCompra() {
		return compra;
	}

	public Insumo getInsumo() {
		return insumo;
	}

	public BigDecimal getCantidadComprada() {
		return cantidadComprada;
	}

	public BigDecimal getCantidadRecibida() {
		return cantidadRecibida;
	}

	public BigDecimal getCostoUnitario() {
		return costoUnitario;
	}
}
