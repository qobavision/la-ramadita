package com.laramadita.backend.empleados.model;

import com.laramadita.backend.usuarios.model.Usuario;
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
import org.hibernate.annotations.Nationalized;

/**
 * Trabajador de La Ramadita. Tabla {@code dbo.empleados} de V3.
 *
 * Puede no tener usuario. {@code activo} es la desactivación temporal;
 * {@code archivado} es la baja lógica (Eliminar), sin DELETE físico.
 */
@Entity
@Table(name = "empleados")
public class Empleado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "empleado_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "cargo_id", nullable = false)
	private Cargo cargo;

	@Nationalized
	@Column(name = "nombres", nullable = false, length = 80)
	private String nombres;

	@Nationalized
	@Column(name = "apellidos", nullable = false, length = 80)
	private String apellidos;

	@Nationalized
	@Column(name = "dni", length = 8)
	private String dni;

	@Nationalized
	@Column(name = "telefono", length = 20)
	private String telefono;

	@Nationalized
	@Column(name = "correo_personal", length = 120)
	private String correoPersonal;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "archivado", nullable = false)
	private boolean archivado = false;

	@Column(name = "fecha_archivado")
	private LocalDateTime fechaArchivado;

	@Column(name = "fecha_creacion", nullable = false, updatable = false)
	private LocalDateTime fechaCreacion;

	@OneToOne(mappedBy = "empleado", fetch = FetchType.LAZY)
	private Usuario usuario;

	protected Empleado() {
	}

	public Empleado(String nombres, String apellidos, String dni, String telefono, String correoPersonal, Cargo cargo) {
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.dni = dni;
		this.telefono = telefono;
		this.correoPersonal = correoPersonal;
		this.cargo = cargo;
		this.activo = true;
	}

	@PrePersist
	void alCrear() {
		if (fechaCreacion == null) {
			fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void actualizarDatos(String nombres, String apellidos, String dni, String telefono, String correoPersonal,
			Cargo cargo) {
		this.nombres = nombres;
		this.apellidos = apellidos;
		this.dni = dni;
		this.telefono = telefono;
		this.correoPersonal = correoPersonal;
		this.cargo = cargo;
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

	public boolean estaOperativo() {
		return activo && !archivado;
	}

	public Integer getId() {
		return id;
	}

	public Cargo getCargo() {
		return cargo;
	}

	public String getNombres() {
		return nombres;
	}

	public String getApellidos() {
		return apellidos;
	}

	public String getDni() {
		return dni;
	}

	public String getTelefono() {
		return telefono;
	}

	public String getCorreoPersonal() {
		return correoPersonal;
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

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public Usuario getUsuario() {
		return usuario;
	}
}
