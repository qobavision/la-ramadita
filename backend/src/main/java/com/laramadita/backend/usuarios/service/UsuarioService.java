package com.laramadita.backend.usuarios.service;

import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.empleados.repository.EmpleadoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.api.dto.PasswordRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioActualizarRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import com.laramadita.backend.usuarios.model.Rol;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.RolRepository;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de cuentas de acceso (M01).
 *
 * Solo ADMINISTRADOR administra usuarios (AGENTS.md §7). El controlador ya
 * restringe el acceso; aquí se refuerzan las reglas de negocio.
 */
@Service
@Transactional
public class UsuarioService {

	private final UsuarioRepository usuarios;
	private final RolRepository roles;
	private final EmpleadoRepository empleados;
	private final PasswordEncoder encoder;

	public UsuarioService(UsuarioRepository usuarios, RolRepository roles, EmpleadoRepository empleados,
			PasswordEncoder encoder) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.empleados = empleados;
		this.encoder = encoder;
	}

	@Transactional(readOnly = true)
	public List<UsuarioResponse> listar() {
		return usuarios.findAllByOrderByUsernameAsc().stream().map(UsuarioResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtener(Integer id) {
		return UsuarioResponse.de(buscar(id));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse obtenerPorUsername(String username) {
		return UsuarioResponse.de(buscarPorUsername(username));
	}

	public UsuarioResponse crear(UsuarioRequest r, String actorUsername) {
		exigirAdministrador(actorUsername);

		Empleado empleado = empleados.findByIdConUsuario(r.empleadoId())
				.orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + r.empleadoId()));
		if (!empleado.estaOperativo()) {
			throw new ReglaNegocioException("No se puede crear un usuario para un empleado inactivo o archivado");
		}
		if (usuarios.existsByEmpleado_Id(empleado.getId())) {
			throw new ConflictoException("Ese empleado ya tiene una cuenta de acceso");
		}

		RolNombre rolNuevo = interpretarRol(r.rol());
		String username = normalizarUsername(r.username());
		if (usuarios.existsByUsername(username)) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}

		Usuario nuevo = new Usuario(username, encoder.encode(r.password()), empleado, rolActivo(rolNuevo));
		if (Boolean.FALSE.equals(r.activo())) {
			nuevo.desactivar();
		}
		usuarios.save(nuevo);
		return UsuarioResponse.de(nuevo);
	}

	public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest r, String actorUsername) {
		Usuario actor = exigirAdministrador(actorUsername);
		Usuario objetivo = buscar(id);

		RolNombre rolNuevo = interpretarRol(r.rol());
		String username = normalizarUsername(r.username());
		if (usuarios.existsByUsernameAndIdNot(username, id)) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}

		boolean esElMismo = objetivo.getId().equals(actor.getId());
		if (esElMismo && !r.activo()) {
			throw new ReglaNegocioException("Un usuario no puede desactivarse a sí mismo");
		}
		if (objetivo.isArchivado() && r.activo()) {
			throw new ReglaNegocioException("Un usuario archivado no puede reactivarse desde Editar");
		}
		if (r.activo() && !objetivo.getEmpleado().estaOperativo()) {
			throw new ReglaNegocioException("No se puede activar un usuario cuyo empleado está inactivo o archivado");
		}

		boolean dejaDeSerAdministradorActivo = objetivo.esAdministrador() && objetivo.isActivo()
				&& !objetivo.isArchivado()
				&& (rolNuevo != RolNombre.ADMINISTRADOR || !r.activo());
		if (dejaDeSerAdministradorActivo) {
			asegurarOtroAdministradorActivo(objetivo);
		}

		objetivo.actualizarCuenta(username, rolActivo(rolNuevo));
		objetivo.sincronizarNombreDesdeEmpleado();
		if (r.password() != null && !r.password().isBlank()) {
			objetivo.cambiarPassword(encoder.encode(r.password()));
		}
		if (!objetivo.isArchivado()) {
			if (r.activo()) {
				objetivo.activar();
			} else {
				objetivo.desactivar();
			}
		}
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse restablecerPassword(Integer id, PasswordRequest r, String actorUsername) {
		exigirAdministrador(actorUsername);
		Usuario objetivo = buscar(id);
		objetivo.cambiarPassword(encoder.encode(r.password()));
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse desactivar(Integer id, String actorUsername) {
		Usuario actor = exigirAdministrador(actorUsername);
		Usuario objetivo = buscar(id);
		if (objetivo.getId().equals(actor.getId())) {
			throw new ReglaNegocioException("Un usuario no puede desactivarse a sí mismo");
		}
		inactivarPorBajaDeEmpleado(objetivo);
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse activar(Integer id, String actorUsername) {
		exigirAdministrador(actorUsername);
		Usuario objetivo = buscar(id);
		if (objetivo.isArchivado()) {
			throw new ReglaNegocioException("Un usuario archivado no puede reactivarse desde esta operación");
		}
		if (!objetivo.getEmpleado().estaOperativo()) {
			throw new ReglaNegocioException("No se puede activar un usuario cuyo empleado está inactivo o archivado");
		}
		objetivo.activar();
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse archivar(Integer id, String actorUsername) {
		Usuario actor = exigirAdministrador(actorUsername);
		Usuario objetivo = buscar(id);
		if (objetivo.getId().equals(actor.getId())) {
			throw new ReglaNegocioException("Un usuario no puede archivar su propia cuenta");
		}
		archivarPorBajaDeEmpleado(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	/**
	 * Inactiva la cuenta porque se inactivó el empleado. Conserva la protección
	 * del último ADMINISTRADOR activo. No archiva.
	 */
	public void inactivarPorBajaDeEmpleado(Usuario objetivo) {
		if (objetivo.isArchivado()) {
			return;
		}
		if (objetivo.esAdministrador() && objetivo.isActivo()) {
			asegurarOtroAdministradorActivo(objetivo);
		}
		objetivo.desactivar();
		usuarios.save(objetivo);
	}

	/**
	 * Archiva la cuenta (baja lógica de Eliminar). Conserva la protección del
	 * último ADMINISTRADOR activo.
	 */
	public void archivarPorBajaDeEmpleado(Usuario objetivo) {
		if (objetivo.isArchivado()) {
			return;
		}
		if (objetivo.esAdministrador() && objetivo.isActivo()) {
			asegurarOtroAdministradorActivo(objetivo);
		}
		objetivo.archivar();
		usuarios.save(objetivo);
	}

	private Usuario exigirAdministrador(String actorUsername) {
		Usuario actor = buscarPorUsername(actorUsername);
		if (!actor.esAdministrador()) {
			throw new OperacionNoAutorizadaException(
					"Solo un usuario ADMINISTRADOR puede administrar cuentas de acceso");
		}
		return actor;
	}

	private void asegurarOtroAdministradorActivo(Usuario objetivo) {
		long otros = usuarios.contarActivosPorRolExcluyendo(RolNombre.ADMINISTRADOR, objetivo.getId());
		if (otros == 0) {
			throw new ReglaNegocioException(
					"Debe permanecer al menos un usuario ADMINISTRADOR activo en el sistema");
		}
	}

	private Usuario buscar(Integer id) {
		return usuarios.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
	}

	private Usuario buscarPorUsername(String username) {
		return usuarios.findByUsername(normalizarUsername(username))
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + username));
	}

	private Rol rolActivo(RolNombre nombre) {
		return roles.findByNombre(nombre)
				.filter(Rol::isActivo)
				.orElseThrow(() -> new ReglaNegocioException("El rol indicado no existe o está inactivo"));
	}

	private RolNombre interpretarRol(String texto) {
		return RolNombre.desde(texto)
				.orElseThrow(() -> new ReglaNegocioException(
						"Rol no válido. Valores aceptados: " + RolNombre.valoresAceptados()));
	}

	private static String normalizarUsername(String username) {
		return Usuario.normalizarUsername(username);
	}
}
