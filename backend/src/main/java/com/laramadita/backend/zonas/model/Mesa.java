package com.laramadita.backend.zonas.model;

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
import org.hibernate.annotations.Nationalized;

/** Mesa del salón. Tabla {@code dbo.mesas} de V1. */
@Entity
@Table(name = "mesas")
public class Mesa {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "mesa_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "zona_id", nullable = false)
	private Zona zona;

	@Nationalized
	@Column(name = "numero", nullable = false, length = 20)
	private String numero;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 30)
	private EstadoMesa estado = EstadoMesa.DISPONIBLE;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	protected Mesa() {
	}

	public Mesa(String numero) {
		this.numero = numero;
		this.estado = EstadoMesa.DISPONIBLE;
		this.activo = true;
	}

	void asignarZona(Zona zona) {
		this.zona = zona;
	}

	public boolean disponible() {
		return activo && estado == EstadoMesa.DISPONIBLE;
	}

	public Integer getId() {
		return id;
	}

	public Zona getZona() {
		return zona;
	}

	public String getNumero() {
		return numero;
	}

	public EstadoMesa getEstado() {
		return estado;
	}

	public boolean isActivo() {
		return activo;
	}
}
