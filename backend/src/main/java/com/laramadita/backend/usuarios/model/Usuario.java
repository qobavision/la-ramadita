package com.laramadita.backend.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.util.Locale;
import org.hibernate.annotations.Nationalized;

/**
 * Usuario del sistema. Tabla {@code dbo.usuarios} de V1.
 *
 * La contraseña solo se guarda como hash BCrypt. La baja es lógica mediante
 * {@code activo} (RN-33: no eliminar físicamente usuarios con historial).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usuario_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "rol_id", nullable = false)
	private Rol rol;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 80)
	private String nombre;

	@Nationalized
	@Column(name = "apellido", length = 80)
	private String apellido;

	@Nationalized
	@Column(name = "username", nullable = false, length = 60)
	private String username;

	@Nationalized
	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	protected Usuario() {
	}

	public Usuario(String username, String passwordHash, String nombre, String apellido, Rol rol) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.nombre = nombre;
		this.apellido = apellido;
		this.rol = rol;
		this.activo = true;
	}

	@PrePersist
	void alCrear() {
		if (fechaCreacion == null) {
			// Coherente con el DEFAULT SYSUTCDATETIME() definido en V1.
			fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void actualizarDatos(String username, String nombre, String apellido, Rol rol) {
		this.username = username;
		this.nombre = nombre;
		this.apellido = apellido;
		this.rol = rol;
	}

	public void cambiarPassword(String nuevoHash) {
		this.passwordHash = nuevoHash;
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public boolean esDueno() {
		return rol != null && rol.getNombre() == RolNombre.DUENO;
	}

	/** Forma canónica del username: sin espacios en los extremos y en minúsculas. */
	public static String normalizarUsername(String username) {
		return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
	}

	public Integer getId() {
		return id;
	}

	public Rol getRol() {
		return rol;
	}

	public String getNombre() {
		return nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public boolean isActivo() {
		return activo;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}
}
