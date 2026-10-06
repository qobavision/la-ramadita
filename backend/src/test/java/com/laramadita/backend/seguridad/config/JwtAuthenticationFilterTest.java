package com.laramadita.backend.seguridad.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.seguridad.service.UsuarioDetailsService;
import jakarta.servlet.FilterChain;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

class JwtAuthenticationFilterTest {

	JwtDecoder decoder = mock(JwtDecoder.class);
	UsuarioDetailsService usuarios = mock(UsuarioDetailsService.class);
	FilterChain chain = mock(FilterChain.class);
	JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(decoder, usuarios);

	@AfterEach
	void limpiar() {
		SecurityContextHolder.clearContext();
	}

	private static Jwt jwtPara(String username) {
		return new Jwt("token", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "HS256"),
				Map.of("sub", username, "iss", "la-ramadita"));
	}

	private static UserDetails detalles(String username, String rolAuthority, boolean activo) {
		return User.withUsername(username).password("hash").authorities(new SimpleGrantedAuthority(rolAuthority))
				.disabled(!activo).build();
	}

	private static MockHttpServletRequest peticionCon(String authorization) {
		MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/usuarios");
		if (authorization != null) {
			req.addHeader(HttpHeaders.AUTHORIZATION, authorization);
		}
		return req;
	}

	@Test
	void sinCabeceraContinuaSinAutenticar() throws Exception {
		MockHttpServletResponse res = new MockHttpServletResponse();

		filtro.doFilter(peticionCon(null), res, chain);

		verify(chain).doFilter(any(), any());
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		assertThat(res.getStatus()).isEqualTo(200);
	}

	@Test
	void conTokenValidoYUsuarioActivoAutenticaConElRolActual() throws Exception {
		when(decoder.decode("abc")).thenReturn(jwtPara("supervisora"));
		when(usuarios.loadUserByUsername("supervisora"))
				.thenReturn(detalles("supervisora", "ROLE_SUPERVISOR", true));
		MockHttpServletResponse res = new MockHttpServletResponse();

		filtro.doFilter(peticionCon("Bearer abc"), res, chain);

		verify(chain).doFilter(any(), any());
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getName()).isEqualTo("supervisora");
		assertThat(auth.getAuthorities()).extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_SUPERVISOR");
	}

	@Test
	void unUsuarioDesactivadoRecibe401AunqueElTokenSigaVigente() throws Exception {
		when(decoder.decode("abc")).thenReturn(jwtPara("mesero1"));
		when(usuarios.loadUserByUsername("mesero1")).thenReturn(detalles("mesero1", "ROLE_MESERO", false));
		MockHttpServletResponse res = new MockHttpServletResponse();

		filtro.doFilter(peticionCon("Bearer abc"), res, chain);

		verify(chain, never()).doFilter(any(), any());
		assertThat(res.getStatus()).isEqualTo(401);
		assertThat(res.getContentType()).startsWith("application/problem+json");
		assertThat(res.getContentAsString()).contains("\"status\":401").contains("inactivo");
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	void unTokenInvalidoRecibe401() throws Exception {
		when(decoder.decode("malo")).thenThrow(new JwtException("firma inválida"));
		MockHttpServletResponse res = new MockHttpServletResponse();

		filtro.doFilter(peticionCon("Bearer malo"), res, chain);

		verify(chain, never()).doFilter(any(), any());
		assertThat(res.getStatus()).isEqualTo(401);
	}

	@Test
	void unTokenDeUsuarioEliminadoRecibe401() throws Exception {
		when(decoder.decode("abc")).thenReturn(jwtPara("fantasma"));
		when(usuarios.loadUserByUsername("fantasma")).thenThrow(new UsernameNotFoundException("no existe"));
		MockHttpServletResponse res = new MockHttpServletResponse();

		filtro.doFilter(peticionCon("Bearer abc"), res, chain);

		verify(chain, never()).doFilter(any(), any());
		assertThat(res.getStatus()).isEqualTo(401);
	}
}
