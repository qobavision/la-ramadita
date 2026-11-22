package com.laramadita.backend.seguridad.service;

import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Puente entre la tabla {@code usuarios} y Spring Security.
 *
 * La primera autoridad es el rol permanente, expresado como {@code ROLE_ADMINISTRADOR},
 * {@code ROLE_SUPERVISOR}, {@code ROLE_MESERO}, {@code ROLE_COCINA} o
 * {@code ROLE_CAJA}. Si el turno en curso le asignó otra función temporal
 * (MESERO, COCINA, CAJA o SUPERVISOR), se agrega también esa autoridad: la
 * función otorga el panel operativo correspondiente (AGENTS.md §7) y sus
 * endpoints con {@code @PreAuthorize}. No cambia el rol permanente ni el JWT,
 * y se recalcula en cada petición, así que expulsar del turno la retira al instante.
 *
 * Un usuario inactivo o archivado se marca como deshabilitado, lo que impide
 * tanto el login como el uso de tokens ya emitidos.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarios;
	private final TurnoService turnos;

	public UsuarioDetailsService(UsuarioRepository usuarios, TurnoService turnos) {
		this.usuarios = usuarios;
		this.turnos = turnos;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		Usuario u = usuarios.findByUsername(Usuario.normalizarUsername(username))
				.orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
		return User.withUsername(u.getUsername())
				.password(u.getPasswordHash())
				.authorities(autoridades(u))
				.disabled(!u.puedeIniciarSesion())
				.build();
	}

	private List<GrantedAuthority> autoridades(Usuario u) {
		RolNombre permanente = u.getRol().getNombre();
		List<GrantedAuthority> autoridades = new ArrayList<>();
		autoridades.add(new SimpleGrantedAuthority(permanente.authority()));
		if (!u.puedeIniciarSesion()) {
			return autoridades;
		}
		String funcion = turnos.funcionActualDe(u);
		if (funcion != null && !funcion.equals(permanente.name())) {
			RolNombre temporal = RolNombre.valueOf(funcion);
			if (temporal != RolNombre.ADMINISTRADOR) {
				autoridades.add(new SimpleGrantedAuthority(temporal.authority()));
			}
		}
		return autoridades;
	}
}
