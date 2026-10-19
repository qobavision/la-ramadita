package com.laramadita.backend.turnos.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.RolNombre;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import org.hibernate.annotations.Nationalized;

/** Persona que ingresó a un turno abierto. Tabla {@code dbo.turno_asistencia} de V12. */
@Entity
@Table(name = "turno_asistencia")
public class TurnoAsistencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "turno_asistencia_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "turno_id", nullable = false)
	private Turno turno;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "hora_ingreso", nullable = false, updatable = false)
	private LocalDateTime horaIngreso;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "funcion_temporal", length = 20)
	private RolNombre funcionTemporal;

	protected TurnoAsistencia() {
	}

	public TurnoAsistencia(Turno turno, Usuario usuario) {
		this.turno = turno;
		this.usuario = usuario;
	}

	@PrePersist
	void alCrear() {
		if (horaIngreso == null) {
			horaIngreso = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public Integer getId() {
		return id;
	}

	public Turno getTurno() {
		return turno;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public LocalDateTime getHoraIngreso() {
		return horaIngreso;
	}

	public RolNombre getFuncionTemporal() {
		return funcionTemporal;
	}

	public RolNombre funcionActual() {
		return funcionTemporal != null ? funcionTemporal : usuario.getRol().getNombre();
	}

	public void asignarFuncion(RolNombre funcion) {
		if (funcion == null || funcion == usuario.getRol().getNombre()) {
			this.funcionTemporal = null;
			return;
		}
		if (!DEL_TURNO.contains(funcion)) {
			throw new ReglaNegocioException("La función temporal debe ser SUPERVISOR, MESERO, COCINA o CAJA");
		}
		this.funcionTemporal = funcion;
	}

	private static final Set<RolNombre> DEL_TURNO =
			Set.of(RolNombre.SUPERVISOR, RolNombre.MESERO, RolNombre.COCINA, RolNombre.CAJA);
}
