package com.laramadita.backend.empleados.service;

import com.laramadita.backend.empleados.api.dto.CargoResponse;
import com.laramadita.backend.empleados.api.dto.EmpleadoActualizarRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoRequest;
import com.laramadita.backend.empleados.api.dto.EmpleadoResponse;
import com.laramadita.backend.empleados.model.Cargo;
import com.laramadita.backend.empleados.model.Empleado;
import com.laramadita.backend.empleados.repository.CargoRepository;
import com.laramadita.backend.empleados.repository.EmpleadoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import com.laramadita.backend.usuarios.service.UsuarioService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de empleados. Solo el ADMINISTRADOR llega aquí (controlador).
 *
 * Inactivar (Editar) deja el registro en el listado operativo.
 * Archivar (Eliminar) es baja lógica: sale de Todos/Activos.
 * Inactivar o archivar un empleado aplica lo mismo a su usuario.
 * Reactivar un empleado no reactiva la cuenta.
 */
@Service
@Transactional
public class EmpleadoService {

	private static final List<String> ORDEN_CARGOS = List.of(
			"Supervisor",
			"Cajero",
			"Ayudante de cajero",
			"Cocinero",
			"Ayudante de cocina",
			"Mesero",
			"Motorizado",
			"Almacenero",
			"Seguridad",
			"Otro");

	private final EmpleadoRepository empleados;
	private final CargoRepository cargos;
	private final UsuarioRepository usuarios;
	private final UsuarioService usuariosService;

	public EmpleadoService(EmpleadoRepository empleados, CargoRepository cargos, UsuarioRepository usuarios,
			UsuarioService usuariosService) {
		this.empleados = empleados;
		this.cargos = cargos;
		this.usuarios = usuarios;
		this.usuariosService = usuariosService;
	}

	@Transactional(readOnly = true)
	public List<CargoResponse> listarCargos() {
		return cargos.findAllByActivoTrueOrderByNombreAsc().stream()
				.sorted(Comparator.comparingInt(cargo -> indiceOrdenCargo(cargo.getNombre())))
				.map(CargoResponse::de)
				.toList();
	}

	private static int indiceOrdenCargo(String nombre) {
		int indice = ORDEN_CARGOS.indexOf(nombre);
		return indice < 0 ? ORDEN_CARGOS.size() : indice;
	}

	@Transactional(readOnly = true)
	public List<EmpleadoResponse> listar() {
		return empleados.findAllConUsuario().stream().map(EmpleadoResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public EmpleadoResponse obtener(Integer id) {
		return EmpleadoResponse.de(buscar(id));
	}

	public EmpleadoResponse crear(EmpleadoRequest r) {
		String dni = exigirDniNuevo(r.dni());
		verificarDniDisponible(dni, null);
		Empleado empleado = new Empleado(
				r.nombres().trim(),
				r.apellidos().trim(),
				dni,
				limpiar(r.telefono()),
				limpiarCorreo(r.correoPersonal()),
				cargoActivo(r.cargoId()));
		if (Boolean.FALSE.equals(r.activo())) {
			empleado.desactivar();
		}
		empleados.save(empleado);
		return EmpleadoResponse.de(empleado);
	}

	public EmpleadoResponse actualizar(Integer id, EmpleadoActualizarRequest r, String actorUsername) {
		Empleado empleado = buscar(id);
		String dni = resolverDniEnEdicion(empleado, r.dni());
		verificarDniDisponible(dni, id);

		empleado.actualizarDatos(
				r.nombres().trim(),
				r.apellidos().trim(),
				dni,
				limpiar(r.telefono()),
				limpiarCorreo(r.correoPersonal()),
				cargoActivo(r.cargoId()));

		usuarios.findByEmpleado_Id(id).ifPresent(Usuario::sincronizarNombreDesdeEmpleado);

		if (empleado.isArchivado()) {
			if (Boolean.TRUE.equals(r.activo())) {
				throw new ReglaNegocioException("Un empleado archivado no puede reactivarse desde Editar");
			}
		} else if (Boolean.FALSE.equals(r.activo()) && empleado.isActivo()) {
			desactivarInterno(empleado, actorUsername);
		} else if (Boolean.TRUE.equals(r.activo()) && !empleado.isActivo()) {
			empleado.activar();
		}
		empleados.save(empleado);
		return EmpleadoResponse.de(buscar(id));
	}

	public EmpleadoResponse desactivar(Integer id, String actorUsername) {
		Empleado empleado = buscar(id);
		if (empleado.isArchivado() || !empleado.isActivo()) {
			return EmpleadoResponse.de(empleado);
		}
		desactivarInterno(empleado, actorUsername);
		empleados.save(empleado);
		return EmpleadoResponse.de(buscar(id));
	}

	public EmpleadoResponse archivar(Integer id, String actorUsername) {
		Empleado empleado = buscar(id);
		if (empleado.isArchivado()) {
			return EmpleadoResponse.de(empleado);
		}
		archivarInterno(empleado, actorUsername);
		empleados.save(empleado);
		return EmpleadoResponse.de(buscar(id));
	}

	public EmpleadoResponse activar(Integer id) {
		Empleado empleado = buscar(id);
		if (empleado.isArchivado()) {
			throw new ReglaNegocioException("Un empleado archivado no puede reactivarse desde esta operación");
		}
		empleado.activar();
		empleados.save(empleado);
		return EmpleadoResponse.de(buscar(id));
	}

	private void desactivarInterno(Empleado empleado, String actorUsername) {
		impedirAccionSobreSiMismo(empleado, actorUsername, "inactivar");
		usuarios.findByEmpleado_Id(empleado.getId()).ifPresent(usuariosService::inactivarPorBajaDeEmpleado);
		empleado.desactivar();
	}

	private void archivarInterno(Empleado empleado, String actorUsername) {
		impedirAccionSobreSiMismo(empleado, actorUsername, "archivar");
		usuarios.findByEmpleado_Id(empleado.getId()).ifPresent(usuariosService::archivarPorBajaDeEmpleado);
		empleado.archivar();
	}

	private void impedirAccionSobreSiMismo(Empleado empleado, String actorUsername, String accion) {
		Usuario actor = usuarios.findByUsername(Usuario.normalizarUsername(actorUsername))
				.orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + actorUsername));
		usuarios.findByEmpleado_Id(empleado.getId()).ifPresent(cuenta -> {
			if (cuenta.getId().equals(actor.getId())) {
				throw new ReglaNegocioException("Un usuario no puede " + accion + " al empleado de su propia cuenta");
			}
		});
	}

	private String resolverDniEnEdicion(Empleado actual, String dniNuevo) {
		String propuesto = normalizarDni(dniNuevo);
		if (propuesto != null) {
			return exigirDniNuevo(propuesto);
		}
		if (actual.getDni() == null) {
			return null;
		}
		throw new ReglaNegocioException("El DNI no puede quitarse una vez registrado");
	}

	private void verificarDniDisponible(String dni, Integer exceptoId) {
		if (dni == null) {
			return;
		}
		boolean usado = exceptoId == null ? empleados.existsByDni(dni) : empleados.existsByDniAndIdNot(dni, exceptoId);
		if (usado) {
			throw new ConflictoException("Ya existe un empleado con ese DNI");
		}
	}

	private Cargo cargoActivo(Integer cargoId) {
		Cargo cargo = cargos.findById(cargoId)
				.orElseThrow(() -> new RecursoNoEncontradoException("Cargo no encontrado: " + cargoId));
		if (!cargo.isActivo()) {
			throw new ReglaNegocioException("El cargo indicado no está activo");
		}
		return cargo;
	}

	private Empleado buscar(Integer id) {
		return empleados.findByIdConUsuario(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("Empleado no encontrado: " + id));
	}

	private static String exigirDniNuevo(String dni) {
		String normalizado = normalizarDni(dni);
		if (normalizado == null || !normalizado.matches("\\d{8}")) {
			throw new ReglaNegocioException("El DNI debe tener exactamente 8 dígitos");
		}
		return normalizado;
	}

	private static String normalizarDni(String dni) {
		if (dni == null || dni.isBlank()) {
			return null;
		}
		return dni.trim();
	}

	private static String limpiar(String valor) {
		return valor == null || valor.isBlank() ? null : valor.trim();
	}

	private static String limpiarCorreo(String valor) {
		String limpio = limpiar(valor);
		return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
	}
}
