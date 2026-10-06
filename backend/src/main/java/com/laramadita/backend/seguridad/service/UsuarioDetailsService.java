package com.laramadita.backend.seguridad.service;

import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.util.List;
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
 * La única autoridad es el rol oficial, expresado como {@code ROLE_ADMINISTRADOR},
 * {@code ROLE_SUPERVISOR}, {@code ROLE_MESERO}, {@code ROLE_COCINA} o
 * {@code ROLE_CAJA}. Un usuario inactivo se marca como deshabilitado, lo que
 * impide tanto el login como el uso de tokens ya emitidos.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarios;

	public UsuarioDetailsService(UsuarioRepository usuarios) {
		this.usuarios = usuarios;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		Usuario u = usuarios.findByUsername(Usuario.normalizarUsername(username))
				.orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas"));
		return User.withUsername(u.getUsername())
				.password(u.getPasswordHash())
				.authorities(List.of(new SimpleGrantedAuthority(u.getRol().getNombre().authority())))
				.disabled(!u.isActivo())
				.build();
	}
}
