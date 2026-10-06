package com.laramadita.backend.seguridad.config;

import com.laramadita.backend.seguridad.service.UsuarioDetailsService;
import com.laramadita.backend.shared.error.ProblemaHttp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida el token Bearer en cada petición.
 *
 * Además de verificar firma y expiración, vuelve a consultar al usuario:
 * si fue desactivado, o si cambió de rol, el efecto es inmediato aunque el
 * token siga vigente. Las autoridades se toman del estado actual, no del token.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIJO_BEARER = "Bearer ";

	private final JwtDecoder decoder;
	private final UsuarioDetailsService usuarios;

	public JwtAuthenticationFilter(JwtDecoder decoder, UsuarioDetailsService usuarios) {
		this.decoder = decoder;
		this.usuarios = usuarios;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (cabecera == null || !cabecera.startsWith(PREFIJO_BEARER)) {
			chain.doFilter(request, response);
			return;
		}

		try {
			Jwt jwt = decoder.decode(cabecera.substring(PREFIJO_BEARER.length()).trim());
			UserDetails detalles = usuarios.loadUserByUsername(jwt.getSubject());
			if (!detalles.isEnabled()) {
				throw new JwtException("Usuario inactivo");
			}
			var autenticacion = new UsernamePasswordAuthenticationToken(detalles, null, detalles.getAuthorities());
			autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(autenticacion);
		} catch (JwtException | UsernameNotFoundException e) {
			SecurityContextHolder.clearContext();
			ProblemaHttp.escribir(response, HttpStatus.UNAUTHORIZED, "No autenticado",
					"El token no es válido, expiró o el usuario está inactivo");
			return;
		}

		chain.doFilter(request, response);
	}
}
