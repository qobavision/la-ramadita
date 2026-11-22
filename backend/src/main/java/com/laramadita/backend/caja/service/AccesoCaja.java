package com.laramadita.backend.caja.service;

import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.tiemporeal.CanalTiempoReal;
import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

/**
 * Quién puede usar Caja (M09/M10). El JWT solo lleva el rol permanente, por eso
 * la función temporal del turno se consulta en cada operación.
 *
 * - Cobrar y operar la caja: rol CAJA, función temporal CAJA o ADMINISTRADOR.
 * - Consultar cajas y cuentas: además SUPERVISOR (monitoreo).
 *
 * También es el canal WebSocket {@code caja}: cuentas solicitadas y pagadas.
 */
@Component
public class AccesoCaja implements CanalTiempoReal {

	public static final String CANAL = "caja";

	private final UsuarioRepository usuarios;
	private final TurnoService turnos;

	public AccesoCaja(UsuarioRepository usuarios, TurnoService turnos) {
		this.usuarios = usuarios;
		this.turnos = turnos;
	}

	@Override
	public String nombre() {
		return CANAL;
	}

	@Override
	public boolean permite(Usuario usuario) {
		return puedeConsultar(usuario);
	}

	public Usuario exigirCobrar(String username) {
		Usuario usuario = buscar(username);
		if (!puedeCobrar(usuario)) {
			throw new OperacionNoAutorizadaException("Solo el personal con función de caja puede registrar cobros");
		}
		return usuario;
	}

	public Usuario exigirConsultar(String username) {
		Usuario usuario = buscar(username);
		if (!puedeConsultar(usuario)) {
			throw new OperacionNoAutorizadaException("Solo Caja o la supervisión pueden consultar las cuentas");
		}
		return usuario;
	}

	/** Decisión del equipo: solo el ADMINISTRADOR (rol permanente) anula pagos. */
	public Usuario exigirAdministrador(String username) {
		Usuario usuario = buscar(username);
		if (usuario.getRol().getNombre() != RolNombre.ADMINISTRADOR) {
			throw new OperacionNoAutorizadaException("Solo el administrador puede anular pagos");
		}
		return usuario;
	}

	/** Además de quien cobra: SUPERVISOR por rol permanente o por función temporal del turno. */
	private boolean puedeConsultar(Usuario usuario) {
		return puedeCobrar(usuario) || usuario.getRol().getNombre() == RolNombre.SUPERVISOR
				|| RolNombre.SUPERVISOR.name().equals(turnos.funcionActualDe(usuario));
	}

	private boolean puedeCobrar(Usuario usuario) {
		RolNombre rol = usuario.getRol().getNombre();
		return rol == RolNombre.ADMINISTRADOR || rol == RolNombre.CAJA
				|| RolNombre.CAJA.name().equals(turnos.funcionActualDe(usuario));
	}

	private Usuario buscar(String username) {
		return usuarios.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
	}
}
