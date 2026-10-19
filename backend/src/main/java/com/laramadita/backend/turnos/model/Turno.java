package com.laramadita.backend.turnos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.unidadesnegocio.model.UnidadNegocio;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

/** Turno operativo. Tabla {@code dbo.turnos} de V1, estado ampliado en V9. */
@Entity
@Table(name = "turnos")
public class Turno {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "turno_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidad_negocio_id", nullable = false)
	private UnidadNegocio unidad;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_apertura_id")
	private Usuario usuarioApertura;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "usuario_cierre_id")
	private Usuario usuarioCierre;

	@Column(name = "fecha_apertura", nullable = false)
	private LocalDateTime fechaApertura;

	@Column(name = "fecha_cierre")
	private LocalDateTime fechaCierre;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoTurno estado = EstadoTurno.ABIERTO;

	protected Turno() {
	}

	public Turno(UnidadNegocio unidad, Usuario usuarioApertura) {
		this.unidad = unidad;
		this.usuarioApertura = usuarioApertura;
		this.fechaApertura = LocalDateTime.now(ZoneOffset.UTC);
		this.estado = EstadoTurno.ABIERTO;
	}

	public static Turno automatico(UnidadNegocio unidad) {
		return new Turno(unidad, null);
	}

	public void prepararCierre() {
		if (estado != EstadoTurno.ABIERTO) {
			throw new ReglaNegocioException("Solo un turno abierto puede pasar a cierre");
		}
		this.estado = EstadoTurno.EN_CIERRE;
	}

	public void cerrar(Usuario usuarioCierre) {
		if (estado == EstadoTurno.CERRADO) {
			throw new ReglaNegocioException("El turno ya está cerrado");
		}
		this.estado = EstadoTurno.CERRADO;
		this.usuarioCierre = usuarioCierre;
		this.fechaCierre = LocalDateTime.now(ZoneOffset.UTC);
	}

	public Integer getId() {
		return id;
	}

	public UnidadNegocio getUnidad() {
		return unidad;
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

	public EstadoTurno getEstado() {
		return estado;
	}

	public LocalDateTime getFechaCierre() {
		return fechaCierre;
	}
}
