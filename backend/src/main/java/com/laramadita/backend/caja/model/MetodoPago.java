package com.laramadita.backend.caja.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Nationalized;

/** Método de pago (RN-15). Tabla {@code dbo.metodos_pago}, sembrada en V1. */
@Entity
@Table(name = "metodos_pago")
public class MetodoPago {

	public static final String EFECTIVO = "EFECTIVO";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "metodo_pago_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 50)
	private String nombre;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	protected MetodoPago() {
	}

	public boolean esEfectivo() {
		return EFECTIVO.equalsIgnoreCase(nombre);
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
