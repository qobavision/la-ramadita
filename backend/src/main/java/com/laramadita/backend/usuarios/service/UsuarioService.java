package com.laramadita.backend.usuarios.service;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
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
 * Gestión de usuarios (M01).
 *
 * Reglas aplicadas en backend (AGENTS.md §7: no confiar solo en el frontend):
 * <ul>
 * <li>DUEÑO administra cualquier usuario.</li>
 * <li>SUPERVISOR administra usuarios, pero no puede crear usuarios DUEÑO,
 * convertir usuarios a DUEÑO ni modificar/activar/desactivar usuarios DUEÑO.</li>
 * <li>Un usuario no puede desactivarse a sí mismo.</li>
 * <li>Siempre debe quedar al menos un DUEÑO activo.</li>
 * <li>La baja es lógica; nunca se elimina físicamente (RN-33).</li>
 * </ul>
 * El acceso a estos métodos ya está restringido por rol en el controlador.
 */
@Service
@Transactional
public class UsuarioService {

	private final UsuarioRepository usuarios;
	private final RolRepository roles;
	private final PasswordEncoder encoder;

	public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
		this.usuarios = usuarios;
		this.roles = roles;
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
		Usuario actor = buscarPorUsername(actorUsername);
		RolNombre rolNuevo = interpretarRol(r.rol());
		verificarPuedeAsignarRol(actor, rolNuevo);

		String username = normalizarUsername(r.username());
		if (usuarios.existsByUsername(username)) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}

		Usuario nuevo = new Usuario(
				username,
				encoder.encode(r.password()),
				r.nombre().trim(),
				limpiar(r.apellido()),
				rolActivo(rolNuevo));
		if (Boolean.FALSE.equals(r.activo())) {
			nuevo.desactivar();
		}
		usuarios.save(nuevo);
		return UsuarioResponse.de(nuevo);
	}

	public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest r, String actorUsername) {
		Usuario actor = buscarPorUsername(actorUsername);
		Usuario objetivo = buscar(id);
		verificarPuedeAdministrar(actor, objetivo);

		RolNombre rolNuevo = interpretarRol(r.rol());
		verificarPuedeAsignarRol(actor, rolNuevo);

		String username = normalizarUsername(r.username());
		if (usuarios.existsByUsernameAndIdNot(username, id)) {
			throw new ConflictoException("El nombre de usuario ya existe");
		}

		boolean esElMismo = objetivo.getId().equals(actor.getId());
		if (esElMismo && !r.activo()) {
			throw new ReglaNegocioException("Un usuario no puede desactivarse a sí mismo");
		}

		boolean dejaDeSerDuenoActivo = objetivo.esDueno() && objetivo.isActivo()
				&& (rolNuevo != RolNombre.DUENO || !r.activo());
		if (dejaDeSerDuenoActivo) {
			asegurarOtroDuenoActivo(objetivo);
		}

		objetivo.actualizarDatos(username, r.nombre().trim(), limpiar(r.apellido()), rolActivo(rolNuevo));
		if (r.password() != null && !r.password().isBlank()) {
			objetivo.cambiarPassword(encoder.encode(r.password()));
		}
		if (r.activo()) {
			objetivo.activar();
		} else {
			objetivo.desactivar();
		}
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse desactivar(Integer id, String actorUsername) {
		Usuario actor = buscarPorUsername(actorUsername);
		Usuario objetivo = buscar(id);
		verificarPuedeAdministrar(actor, objetivo);

		if (objetivo.getId().equals(actor.getId())) {
			throw new ReglaNegocioException("Un usuario no puede desactivarse a sí mismo");
		}
		if (objetivo.esDueno() && objetivo.isActivo()) {
			asegurarOtroDuenoActivo(objetivo);
		}
		objetivo.desactivar();
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	public UsuarioResponse activar(Integer id, String actorUsername) {
		Usuario actor = buscarPorUsername(actorUsername);
		Usuario objetivo = buscar(id);
		verificarPuedeAdministrar(actor, objetivo);

		objetivo.activar();
		usuarios.save(objetivo);
		return UsuarioResponse.de(objetivo);
	}

	// ------------------------------------------------------------------
	// Reglas
	// ------------------------------------------------------------------

	/** SUPERVISOR no puede tocar usuarios DUEÑO. DUEÑO puede con cualquiera. */
	private void verificarPuedeAdministrar(Usuario actor, Usuario objetivo) {
		if (!actor.esDueno() && objetivo.esDueno()) {
			throw new OperacionNoAutorizadaException(
					"Solo un usuario DUE\u00D1O puede modificar a otro usuario DUE\u00D1O");
		}
	}

	/** Solo DUEÑO puede crear o asignar el rol DUEÑO. */
	private void verificarPuedeAsignarRol(Usuario actor, RolNombre rol) {
		if (rol == RolNombre.DUENO && !actor.esDueno()) {
			throw new OperacionNoAutorizadaException(
					"Solo un usuario DUE\u00D1O puede asignar el rol DUE\u00D1O");
		}
	}

	private void asegurarOtroDuenoActivo(Usuario objetivo) {
		long otros = usuarios.contarActivosPorRolExcluyendo(RolNombre.DUENO, objetivo.getId());
		if (otros == 0) {
			throw new ReglaNegocioException("Debe permanecer al menos un usuario DUE\u00D1O activo en el sistema");
		}
	}

	// ------------------------------------------------------------------
	// Apoyo
	// ------------------------------------------------------------------

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

	private static String limpiar(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}
}
