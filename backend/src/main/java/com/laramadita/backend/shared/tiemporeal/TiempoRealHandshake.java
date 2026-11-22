package com.laramadita.backend.shared.tiemporeal;

import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Autentica la conexión WebSocket. El navegador no puede enviar la cabecera
 * {@code Authorization} al abrir un WebSocket, por eso el JWT llega como
 * parámetro {@code token}. Se valida igual que en la API: firma, expiración
 * y usuario que todavía puede iniciar sesión.
 *
 * Los canales pedidos ({@code canales=cocina}) se autorizan con el
 * {@link CanalTiempoReal} de cada módulo; si alguno no está permitido, la
 * conexión se rechaza con 403.
 */
@Component
class TiempoRealHandshake implements HandshakeInterceptor {

	static final String ATRIBUTO_USUARIO = "lr.usuario";
	static final String ATRIBUTO_CANALES = "lr.canales";
	static final String ATRIBUTO_EXPIRA = "lr.expira";

	private final JwtDecoder decoder;
	private final UsuarioRepository usuarios;
	private final List<CanalTiempoReal> canales;

	TiempoRealHandshake(JwtDecoder decoder, UsuarioRepository usuarios, List<CanalTiempoReal> canales) {
		this.decoder = decoder;
		this.usuarios = usuarios;
		this.canales = canales;
	}

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
			Map<String, Object> atributos) {
		var parametros = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
		Optional<Jwt> jwt = decodificar(parametros.getFirst("token"));
		Optional<Usuario> usuario = jwt.flatMap(t -> usuarios.findByUsername(t.getSubject()))
				.filter(Usuario::puedeIniciarSesion);
		if (usuario.isEmpty()) {
			response.setStatusCode(HttpStatus.UNAUTHORIZED);
			return false;
		}

		Set<String> pedidos = separar(parametros.getFirst("canales"));
		Set<String> permitidos = new HashSet<>();
		for (String nombre : pedidos) {
			boolean autorizado = canales.stream()
					.anyMatch(c -> c.nombre().equals(nombre) && c.permite(usuario.get()));
			if (!autorizado) {
				response.setStatusCode(HttpStatus.FORBIDDEN);
				return false;
			}
			permitidos.add(nombre);
		}

		atributos.put(ATRIBUTO_USUARIO, usuario.get().getUsername());
		atributos.put(ATRIBUTO_CANALES, Set.copyOf(permitidos));
		if (jwt.get().getExpiresAt() != null) {
			atributos.put(ATRIBUTO_EXPIRA, jwt.get().getExpiresAt());
		}
		return true;
	}

	/**
	 * Lo que se autorizó al conectar sigue valiendo: token vigente, usuario que
	 * aún puede iniciar sesión y canales todavía permitidos (p. ej. no fue
	 * expulsado del turno ni perdió la función). Lo usa {@link RevisionSesionesTiempoReal}.
	 */
	boolean sigueVigente(Map<String, Object> atributos, Instant ahora) {
		if (atributos.get(ATRIBUTO_EXPIRA) instanceof Instant expira && !ahora.isBefore(expira)) {
			return false;
		}
		if (!(atributos.get(ATRIBUTO_USUARIO) instanceof String username)) {
			return false;
		}
		Optional<Usuario> usuario = usuarios.findByUsername(username).filter(Usuario::puedeIniciarSesion);
		if (usuario.isEmpty()) {
			return false;
		}
		if (atributos.get(ATRIBUTO_CANALES) instanceof Set<?> suscritos) {
			for (Object nombre : suscritos) {
				boolean permitido = canales.stream()
						.anyMatch(c -> c.nombre().equals(nombre) && c.permite(usuario.get()));
				if (!permitido) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
			Exception exception) {
	}

	private Optional<Jwt> decodificar(String token) {
		if (token == null || token.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(decoder.decode(token.trim()));
		} catch (JwtException e) {
			return Optional.empty();
		}
	}

	private static Set<String> separar(String valor) {
		if (valor == null || valor.isBlank()) {
			return Set.of();
		}
		Set<String> resultado = new HashSet<>();
		Arrays.stream(valor.split(","))
				.map(s -> s.trim().toLowerCase(Locale.ROOT))
				.filter(s -> !s.isEmpty())
				.forEach(resultado::add);
		return resultado;
	}
}
