package com.laramadita.backend.unidadesnegocio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

/** Tabla {@code dbo.unidades_negocio} de V1. */
@Entity
@Table(name = "unidades_negocio")
public class UnidadNegocio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "unidad_negocio_id")
	private Integer id;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "nombre", nullable = false, length = 50)
	private NombreUnidad nombre;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	protected UnidadNegocio() {
	}

	public Integer getId() {
		return id;
	}

	public NombreUnidad getNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}
}
