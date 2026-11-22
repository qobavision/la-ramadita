package com.laramadita.backend.caja.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.turnos.model.Turno;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Nationalized;

/**
 * Caja de un cajero dentro de un turno (M10): apertura con monto inicial,
 * ingresos/egresos, pagos recibidos y cierre con arqueo de efectivo.
 * Tabla {@code dbo.caja_sesiones} (la columna {@code diferencia} es calculada en la base).
 */
@Entity
@Table(name = "caja_sesiones")
public class CajaSesion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "caja_sesion_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turno_id", nullable = false)
	private Turno turno;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_apertura_id", nullable = false)
	private Usuario usuarioApertura;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_cierre_id")
	private Usuario usuarioCierre;

	@Column(name = "fecha_apertura", nullable = false)
	private LocalDateTime fechaApertura;

	@Column(name = "fecha_cierre")
	private LocalDateTime fechaCierre;

	@Column(name = "monto_inicial", nullable = false, precision = 12, scale = 2)
	private BigDecimal montoInicial;

	@Column(name = "efectivo_esperado", precision = 12, scale = 2)
	private BigDecimal efectivoEsperado;

	@Column(name = "efectivo_real", precision = 12, scale = 2)
	private BigDecimal efectivoReal;

	@Nationalized
	@Column(name = "observacion_cierre", length = 500)
	private String observacionCierre;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoCaja estado = EstadoCaja.ABIERTA;

	@OneToMany(mappedBy = "sesion", cascade = CascadeType.ALL)
	@OrderBy("id ASC")
	private List<CajaMovimiento> movimientos = new ArrayList<>();

	protected CajaSesion() {
	}

	public CajaSesion(Turno turno, Usuario cajero, BigDecimal montoInicial) {
		if (montoInicial == null || montoInicial.signum() < 0) {
			throw new ReglaNegocioException("El monto inicial no puede ser negativo");
		}
		this.turno = turno;
		this.usuarioApertura = cajero;
		this.montoInicial = montoInicial;
		this.fechaApertura = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoCaja.ABIERTA;
	}

	public void registrarMovimiento(TipoMovimientoCaja tipo, BigDecimal monto, String motivo, Usuario usuario) {
		exigirAbierta();
		if (monto == null || monto.signum() <= 0) {
			throw new ReglaNegocioException("El monto debe ser mayor a cero");
		}
		if (motivo == null || motivo.isBlank()) {
			throw new ReglaNegocioException("Indica el motivo del movimiento");
		}
		movimientos.add(new CajaMovimiento(this, usuario, tipo, monto, motivo.trim()));
	}

	/** Cierre con arqueo: si el efectivo contado no cuadra, la observación es obligatoria. */
	public void cerrar(BigDecimal efectivoEsperado, BigDecimal efectivoContado, String observacion, Usuario usuario) {
		exigirAbierta();
		if (efectivoContado == null || efectivoContado.signum() < 0) {
			throw new ReglaNegocioException("Indica el efectivo contado");
		}
		String nota = observacion == null || observacion.isBlank() ? null : observacion.trim();
		if (efectivoContado.compareTo(efectivoEsperado) != 0 && nota == null) {
			throw new ReglaNegocioException("Hay diferencia entre el efectivo esperado y el contado: indica una observación");
		}
		this.efectivoEsperado = efectivoEsperado;
		this.efectivoReal = efectivoContado;
		this.observacionCierre = nota;
		this.usuarioCierre = usuario;
		this.fechaCierre = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoCaja.CERRADA;
	}

	public BigDecimal totalMovimientos(TipoMovimientoCaja tipo) {
		return movimientos.stream()
				.filter(m -> m.getTipo() == tipo)
				.map(CajaMovimiento::getMonto)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public void exigirAbierta() {
		if (estado != EstadoCaja.ABIERTA) {
			throw new ReglaNegocioException("La caja ya está cerrada");
		}
	}

	public Integer getId() {
		return id;
	}

	public Turno getTurno() {
		return turno;
	}

	public Usuario getUsuarioApertura() {
		return usuarioApertura;
	}

	public Usuario getUsuarioCierre() {
		return usuarioCierre;
	}

	public LocalDateTime getFechaApertura() {
		return fechaApertura;
	}

	public LocalDateTime getFechaCierre() {
		return fechaCierre;
	}

	public BigDecimal getMontoInicial() {
		return montoInicial;
	}

	public BigDecimal getEfectivoEsperado() {
		return efectivoEsperado;
	}

	public BigDecimal getEfectivoReal() {
		return efectivoReal;
	}

	public String getObservacionCierre() {
		return observacionCierre;
	}

	public EstadoCaja getEstado() {
		return estado;
	}

	public List<CajaMovimiento> getMovimientos() {
		return movimientos;
	}
}
