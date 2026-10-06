package com.laramadita.backend.seguridad.config;

import com.laramadita.backend.seguridad.service.JwtService;
import com.laramadita.backend.seguridad.service.UsuarioDetailsService;
import com.laramadita.backend.shared.error.ProblemaHttp;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security 7 para una API REST sin estado protegida
 * con JWT (HS256) y contraseñas con BCrypt.
 *
 * Solo {@code POST /api/v1/auth/login} es público. Todo lo demás requiere un
 * token válido de un usuario activo; la autorización fina se declara con
 * {@code @PreAuthorize} en los controladores.
 */
@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtEncoder jwtEncoder(JwtProperties props) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(clave(props)));
	}

	@Bean
	JwtDecoder jwtDecoder(JwtProperties props) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave(props))
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtService.EMISOR));
		return decoder;
	}

	@Bean
	AuthenticationManager authenticationManager(UsuarioDetailsService usuarios, PasswordEncoder encoder) {
		// Spring Security 7: el UserDetailsService se pasa por constructor.
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarios);
		provider.setPasswordEncoder(encoder);
		return new ProviderManager(provider);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
						.requestMatchers("/error").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(e -> e
						.authenticationEntryPoint((req, res, ex) -> ProblemaHttp.escribir(res,
								HttpStatus.UNAUTHORIZED, "No autenticado", "Se requiere iniciar sesión"))
						.accessDeniedHandler((req, res, ex) -> ProblemaHttp.escribir(res,
								HttpStatus.FORBIDDEN, "Acceso denegado",
								"No tiene permiso para realizar esta operación")))
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private static SecretKey clave(JwtProperties props) {
		return new SecretKeySpec(props.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
