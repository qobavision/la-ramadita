package com.laramadita.backend.insumos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import org.hibernate.annotations.Nationalized;

/** Insumo de inventario. Tabla {@code dbo.insumos} de V1. */
@Entity
@Table(name = "insumos")
public class Insumo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "insumo_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 100, unique = true)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "unidad_medida", nullable = false, length = 30)
	private UnidadMedida unidadMedida;

	@Column(name = "stock_actual", nullable = false, precision = 18, scale = 3)
	private BigDecimal stockActual = BigDecimal.ZERO;

	@Column(name = "stock_minimo", nullable = false, precision = 18, scale = 3)
	private BigDecimal stockMinimo = BigDecimal.ZERO;

	@Column(name = "costo_referencial", precision = 18, scale = 4)
	private BigDecimal costoReferencial;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	protected Insumo() {
	}

	public Insumo(String nombre, UnidadMedida unidadMedida, BigDecimal stockInicial, BigDecimal stockMinimo) {
		this.nombre = nombre;
		this.unidadMedida = unidadMedida;
		this.stockActual = exigirNoNegativo(stockInicial, "El stock actual no puede ser negativo");
		this.stockMinimo = exigirNoNegativo(stockMinimo, "El stock mínimo no puede ser negativo");
		this.activo = true;
	}

	public void actualizar(String nombre, UnidadMedida unidadMedida, BigDecimal stockMinimo) {
		this.nombre = nombre;
		this.unidadMedida = unidadMedida;
		this.stockMinimo = exigirNoNegativo(stockMinimo, "El stock mínimo no puede ser negativo");
	}

	public BigDecimal incrementarStock(BigDecimal cantidad) {
		if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ReglaNegocioException("La cantidad a ingresar debe ser mayor a 0");
		}
		BigDecimal anterior = this.stockActual;
		this.stockActual = anterior.add(cantidad);
		return anterior;
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public EstadoStock estadoStock() {
		return EstadoStock.de(stockActual, stockMinimo);
	}

	private static BigDecimal exigirNoNegativo(BigDecimal valor, String mensaje) {
		BigDecimal limpio = valor == null ? BigDecimal.ZERO : valor;
		if (limpio.compareTo(BigDecimal.ZERO) < 0) {
			throw new ReglaNegocioException(mensaje);
		}
		return limpio;
	}

	public Integer getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public UnidadMedida getUnidadMedida() {
		return unidadMedida;
	}

	public BigDecimal getStockActual() {
		return stockActual;
	}

	public BigDecimal getStockMinimo() {
		return stockMinimo;
	}

	public BigDecimal getCostoReferencial() {
		return costoReferencial;
	}

	public boolean isActivo() {
		return activo;
	}
}
