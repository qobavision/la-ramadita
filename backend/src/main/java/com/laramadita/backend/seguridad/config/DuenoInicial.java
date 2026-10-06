package com.laramadita.backend.seguridad.config;

import com.laramadita.backend.usuarios.model.Rol;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.RolRepository;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el primer usuario DUEÑO cuando la tabla {@code usuarios} está vacía.
 *
 * Las credenciales provienen exclusivamente de las variables de entorno
 * APP_DUENO_USERNAME y APP_DUENO_PASSWORD. No existen valores por defecto:
 * si faltan, solo se registra una advertencia. Una vez que hay usuarios, las
 * variables se ignoran, por lo que pueden retirarse del entorno.
 */
@Component
public class DuenoInicial implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DuenoInicial.class);
	private static final int LONGITUD_MINIMA_PASSWORD = 8;

	private final UsuarioRepository usuarios;
	private final RolRepository roles;
	private final PasswordEncoder encoder;
	private final String username;
	private final String password;

	public DuenoInicial(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder,
			@Value("${app.security.dueno-inicial.username}") String username,
			@Value("${app.security.dueno-inicial.password}") String password) {
		this.usuarios = usuarios;
		this.roles = roles;
		this.encoder = encoder;
		this.username = username == null ? "" : username.trim();
		this.password = password == null ? "" : password;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (usuarios.count() > 0) {
			if (!username.isBlank() || !password.isBlank()) {
				log.info("Ya existen usuarios registrados; se ignoran APP_DUENO_USERNAME y APP_DUENO_PASSWORD.");
			}
			return;
		}

		if (username.isBlank() || password.isBlank()) {
			log.warn("No existe ningún usuario en el sistema. Defina las variables de entorno "
					+ "APP_DUENO_USERNAME y APP_DUENO_PASSWORD y reinicie el backend para crear el DUE\u00D1O inicial.");
			return;
		}
		if (password.length() < LONGITUD_MINIMA_PASSWORD) {
			log.error("APP_DUENO_PASSWORD debe tener al menos {} caracteres. No se creó el DUE\u00D1O inicial.",
					LONGITUD_MINIMA_PASSWORD);
			return;
		}

		Rol rol = roles.findByNombre(RolNombre.DUENO)
				.orElseThrow(() -> new IllegalStateException(
						"No existe el rol DUE\u00D1O en la tabla roles. Verifique la migración V1."));
		Usuario dueno = new Usuario(Usuario.normalizarUsername(username), encoder.encode(password),
				"Due\u00F1o", null, rol);
		usuarios.save(dueno);
		log.info("Usuario DUE\u00D1O inicial '{}' creado a partir de variables de entorno.", dueno.getUsername());
	}
}
