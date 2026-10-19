package com.laramadita.backend.inventario.model;

import com.laramadita.backend.compras.model.CompraDetalle;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.usuarios.model.Usuario;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

@Entity
@Table(name = "inventario_movimientos")
public class InventarioMovimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "movimiento_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "insumo_id", nullable = false)
	private Insumo insumo;

	@Column(name = "unidad_negocio_id")
	private Integer unidadNegocioId;

	@Column(name = "turno_id")
	private Integer turnoId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "compra_detalle_id")
	private CompraDetalle compraDetalle;

	@Column(name = "pedido_detalle_id")
	private Integer pedidoDetalleId;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo_movimiento", nullable = false, length = 30)
	private TipoMovimientoInventario tipoMovimiento;

	@Column(name = "cantidad", nullable = false, precision = 18, scale = 3)
	private BigDecimal cantidad;

	@Column(name = "stock_anterior", nullable = false, precision = 18, scale = 3)
	private BigDecimal stockAnterior;

	@Column(name = "stock_nuevo", nullable = false, precision = 18, scale = 3)
	private BigDecimal stockNuevo;

	@Nationalized
	@Column(name = "motivo", length = 300)
	private String motivo;

	@Column(name = "fecha_movimiento", nullable = false, updatable = false)
	private LocalDateTime fechaMovimiento;

	protected InventarioMovimiento() {
	}

	public static InventarioMovimiento entradaCompra(Insumo insumo, Usuario usuario, CompraDetalle detalle,
			BigDecimal cantidad, BigDecimal stockAnterior, BigDecimal stockNuevo, String origen) {
		InventarioMovimiento movimiento = new InventarioMovimiento();
		movimiento.insumo = insumo;
		movimiento.usuario = usuario;
		movimiento.compraDetalle = detalle;
		movimiento.tipoMovimiento = TipoMovimientoInventario.ENTRADA_COMPRA;
		movimiento.cantidad = cantidad;
		movimiento.stockAnterior = stockAnterior;
		movimiento.stockNuevo = stockNuevo;
		movimiento.motivo = origen;
		return movimiento;
	}

	@PrePersist
	void alCrear() {
		if (fechaMovimiento == null) {
			fechaMovimiento = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public Integer getId() {
		return id;
	}

	public Insumo getInsumo() {
		return insumo;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public CompraDetalle getCompraDetalle() {
		return compraDetalle;
	}

	public TipoMovimientoInventario getTipoMovimiento() {
		return tipoMovimiento;
	}

	public BigDecimal getCantidad() {
		return cantidad;
	}

	public BigDecimal getStockAnterior() {
		return stockAnterior;
	}

	public BigDecimal getStockNuevo() {
		return stockNuevo;
	}

	public String getMotivo() {
		return motivo;
	}

	public LocalDateTime getFechaMovimiento() {
		return fechaMovimiento;
	}
}
