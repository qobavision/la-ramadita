package com.laramadita.backend.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

/**
 * Rol del sistema. Tabla {@code dbo.roles} de V1.
 *
 * Es dato de referencia sembrado por la migración: la aplicación solo lo lee.
 */
@Entity
@Table(name = "roles")
public class Rol {

	@Id
	@Column(name = "rol_id")
	private Integer id;

	@Nationalized
	@Convert(converter = RolNombreConverter.class)
	@Column(name = "nombre", nullable = false, length = 50)
	private RolNombre nombre;

	@Nationalized
	@Column(name = "descripcion", length = 200)
	private String descripcion;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	protected Rol() {
	}

	public Integer getId() {
		return id;
	}

	public RolNombre getNombre() {
		return nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public boolean isActivo() {
		return activo;
	}
}
