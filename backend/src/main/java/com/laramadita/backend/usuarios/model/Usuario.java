package com.laramadita.backend.usuarios.model;

import com.laramadita.backend.empleados.model.Empleado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import org.hibernate.annotations.Nationalized;

/**
 * Cuenta de acceso. Tabla {@code dbo.usuarios} (V1 + empleado_id en V3).
 *
 * Pertenece a un empleado (1 → 0..1). {@code nombre} y {@code apellido} se
 * conservan como copia sincronizada; la fuente funcional es el empleado.
 * La contraseña solo se guarda como hash BCrypt. {@code activo} es la
 * desactivación temporal; {@code archivado} es la baja lógica (Eliminar).
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usuario_id")
	private Integer id;

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "empleado_id", nullable = false, unique = true)
	private Empleado empleado;

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

	@Column(name = "archivado", nullable = false)
	private boolean archivado = false;

	@Column(name = "fecha_archivado")
	private LocalDateTime fechaArchivado;

	@Column(name = "fecha_ultimo_acceso")
	private LocalDateTime fechaUltimoAcceso;

	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	protected Usuario() {
	}

	public Usuario(String username, String passwordHash, Empleado empleado, Rol rol) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.empleado = empleado;
		this.rol = rol;
		this.activo = true;
		sincronizarNombreDesdeEmpleado();
	}

	@PrePersist
	void alCrear() {
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void actualizarCuenta(String username, Rol rol) {
		this.username = username;
		this.rol = rol;
	}

	public void sincronizarNombreDesdeEmpleado() {
		if (empleado != null) {
			this.nombre = empleado.getNombres();
			String apellidos = empleado.getApellidos();
			this.apellido = apellidos == null || apellidos.isBlank() ? null : apellidos;
		}
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

	public void archivar() {
		this.activo = false;
		this.archivado = true;
		if (this.fechaArchivado == null) {
			this.fechaArchivado = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void registrarUltimoAcceso(LocalDateTime momento) {
		this.fechaUltimoAcceso = momento;
	}

	public boolean puedeIniciarSesion() {
		return activo && !archivado && (empleado == null || !empleado.isArchivado());
	}

	public boolean esAdministrador() {
		return rol != null && rol.getNombre() == RolNombre.ADMINISTRADOR;
	}

	/** Forma canónica del username: sin espacios en los extremos y en minúsculas. */
	public static String normalizarUsername(String username) {
		return username == null ? null : username.trim().toLowerCase(Locale.ROOT);
	}

	public Integer getId() {
		return id;
	}

	public Empleado getEmpleado() {
		return empleado;
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

	public boolean isArchivado() {
		return archivado;
	}

	public LocalDateTime getFechaArchivado() {
		return fechaArchivado;
	}

	public LocalDateTime getFechaUltimoAcceso() {
		return fechaUltimoAcceso;
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}
}
