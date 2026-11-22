package com.laramadita.backend.cocina.service;

import com.laramadita.backend.shared.error.OperacionNoAutorizadaException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.tiemporeal.CanalTiempoReal;
import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import org.springframework.stereotype.Component;

/**
 * Quién puede usar Cocina (M08). El JWT solo lleva el rol permanente, por eso
 * la función temporal del turno se consulta en cada operación.
 *
 * - Preparar: rol COCINA, función temporal COCINA o ADMINISTRADOR.
 * - Consultar el tablero: además SUPERVISOR, que monitorea la operación.
 *
 * También es el canal WebSocket {@code cocina}: solo quien puede consultar el
 * tablero recibe los avisos de pedidos nuevos, modificados o cancelados.
 */
@Component
public class AccesoCocina implements CanalTiempoReal {

	public static final String CANAL = "cocina";

	private final UsuarioRepository usuarios;
	private final TurnoService turnos;

	public AccesoCocina(UsuarioRepository usuarios, TurnoService turnos) {
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

	public Usuario exigirConsultar(String username) {
		Usuario usuario = buscar(username);
		if (!puedeConsultar(usuario)) {
			throw new OperacionNoAutorizadaException("Solo Cocina o la supervisión pueden ver los pedidos de cocina");
		}
		return usuario;
	}

	public Usuario exigirPreparar(String username) {
		Usuario usuario = buscar(username);
		if (!puedePreparar(usuario)) {
			throw new OperacionNoAutorizadaException("Solo el personal con función de cocina puede marcar productos como preparados");
		}
		return usuario;
	}

	/** SUPERVISOR permanente o asignado como función temporal del turno (V14). */
	private boolean puedeConsultar(Usuario usuario) {
		String funcion = turnos.funcionActualDe(usuario);
		return puedePreparar(usuario, funcion)
				|| usuario.getRol().getNombre() == RolNombre.SUPERVISOR
				|| RolNombre.SUPERVISOR.name().equals(funcion);
	}

	private boolean puedePreparar(Usuario usuario) {
		return puedePreparar(usuario, turnos.funcionActualDe(usuario));
	}

	private static boolean puedePreparar(Usuario usuario, String funcion) {
		RolNombre rol = usuario.getRol().getNombre();
		return rol == RolNombre.ADMINISTRADOR || rol == RolNombre.COCINA || RolNombre.COCINA.name().equals(funcion);
	}

	private Usuario buscar(String username) {
		return usuarios.findByUsername(username)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el usuario"));
	}
}
