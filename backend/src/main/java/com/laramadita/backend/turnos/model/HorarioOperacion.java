package com.laramadita.backend.turnos.model;

import com.laramadita.backend.unidadesnegocio.model.UnidadNegocio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;

/** Horario base por unidad. Tabla {@code dbo.horario_operacion} de V9. */
@Entity
@Table(name = "horario_operacion")
public class HorarioOperacion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "horario_operacion_id")
	private Integer id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unidad_negocio_id", nullable = false, unique = true)
	private UnidadNegocio unidad;

	@Column(name = "hora_inicio", nullable = false)
	private LocalTime horaInicio;

	@Column(name = "hora_fin", nullable = false)
	private LocalTime horaFin;

	protected HorarioOperacion() {
	}

	public void actualizar(LocalTime horaInicio, LocalTime horaFin) {
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
	}

	public Integer getId() {
		return id;
	}

	public UnidadNegocio getUnidad() {
		return unidad;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}
}
