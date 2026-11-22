package com.laramadita.backend.caja.model;

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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

/** Ingreso o egreso de caja (M10). Tabla {@code dbo.caja_movimientos}. */
@Entity
@Table(name = "caja_movimientos")
public class CajaMovimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "movimiento_caja_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "caja_sesion_id", nullable = false)
	private CajaSesion sesion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo", nullable = false, length = 20)
	private TipoMovimientoCaja tipo;

	@Column(name = "monto", nullable = false, precision = 12, scale = 2)
	private BigDecimal monto;

	@Nationalized
	@Column(name = "motivo", nullable = false, length = 300)
	private String motivo;

	@Column(name = "fecha_movimiento", nullable = false)
	private LocalDateTime fecha;

	protected CajaMovimiento() {
	}

	CajaMovimiento(CajaSesion sesion, Usuario usuario, TipoMovimientoCaja tipo, BigDecimal monto, String motivo) {
		this.sesion = sesion;
		this.usuario = usuario;
		this.tipo = tipo;
		this.monto = monto;
		this.motivo = motivo;
		this.fecha = LocalDateTime.now(ZoneOffset.UTC);
	}

	public Integer getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public TipoMovimientoCaja getTipo() {
		return tipo;
	}

	public BigDecimal getMonto() {
		return monto;
	}

	public String getMotivo() {
		return motivo;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}
}
