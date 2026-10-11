package com.laramadita.backend.proveedores.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

/** Proveedor. Tabla {@code dbo.proveedores} de V1, extendida en V7. */
@Entity
@Table(name = "proveedores")
public class Proveedor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "proveedor_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre_razon_social", nullable = false, length = 150)
	private String nombreRazonSocial;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo_documento", length = 3)
	private TipoDocumento tipoDocumento;

	@Nationalized
	@Column(name = "documento", length = 20)
	private String documento;

	@Nationalized
	@Column(name = "telefono", length = 20)
	private String telefono;

	@Nationalized
	@Column(name = "direccion", length = 250)
	private String direccion;

	@Nationalized
	@Column(name = "persona_contacto", length = 120)
	private String personaContacto;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	protected Proveedor() {
	}

	public Proveedor(String nombreRazonSocial, TipoDocumento tipoDocumento, String documento, String telefono,
			String personaContacto) {
		this.nombreRazonSocial = nombreRazonSocial;
		this.tipoDocumento = tipoDocumento;
		this.documento = documento;
		this.telefono = telefono;
		this.personaContacto = personaContacto;
		this.activo = true;
	}

	@PrePersist
	void alCrear() {
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void actualizar(String nombreRazonSocial, TipoDocumento tipoDocumento, String documento, String telefono,
			String personaContacto, boolean activo) {
		this.nombreRazonSocial = nombreRazonSocial;
		this.tipoDocumento = tipoDocumento;
		this.documento = documento;
		this.telefono = telefono;
		this.personaContacto = personaContacto;
		this.activo = activo;
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public Integer getId() {
		return id;
	}

	public String getNombreRazonSocial() {
		return nombreRazonSocial;
	}

	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public String getDocumento() {
		return documento;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getDireccion() {
		return direccion;
	}

	public String getPersonaContacto() {
		return personaContacto;
	}

	public boolean isActivo() {
		return activo;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}
}
