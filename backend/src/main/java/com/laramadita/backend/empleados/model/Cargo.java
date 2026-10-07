package com.laramadita.backend.empleados.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

/**
 * Cargo laboral. Tabla {@code dbo.cargos} de V3.
 *
 * Es catálogo sembrado por la migración. No equivale a un rol del sistema.
 */
@Entity
@Table(name = "cargos")
public class Cargo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cargo_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	public Cargo() {
	}

	public Integer getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}
}
