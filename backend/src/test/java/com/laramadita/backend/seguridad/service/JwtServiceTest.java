package com.laramadita.backend.seguridad.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.laramadita.backend.seguridad.config.JwtProperties;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Prueba de ida y vuelta con el codificador y decodificador reales de Nimbus.
 * La clave se genera al azar en cada ejecución: no hay secretos en el código.
 */
class JwtServiceTest {

	private static String claveAleatoria() {
		byte[] bytes = new byte[48];
		new SecureRandom().nextBytes(bytes);
		return Base64.getEncoder().encodeToString(bytes);
	}

	private static SecretKey clave(String secreto) {
		return new SecretKeySpec(secreto.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
	}

	@Test
	void emiteUnTokenVerificableConSujetoEmisorRolYExpiracion() {
		String secreto = claveAleatoria();
		JwtProperties props = new JwtProperties(secreto, 30);
		JwtService service = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(clave(secreto))), props);

		JwtService.Token token = service.emitir(usuario(7, "cocina1", RolNombre.COCINA, true));

		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(clave(secreto)).macAlgorithm(MacAlgorithm.HS256)
				.build();
		Jwt jwt = decoder.decode(token.valor());

		assertThat(jwt.getSubject()).isEqualTo("cocina1");
		// El emisor no es una URL, por eso se lee como texto y no con getIssuer().
		assertThat(jwt.getClaimAsString(JwtClaimNames.ISS)).isEqualTo(JwtService.EMISOR);
		assertThat(jwt.getClaimAsString(JwtService.CLAIM_ROL)).isEqualTo("COCINA");
		// El claim exp se serializa en segundos.
		assertThat(jwt.getExpiresAt()).isEqualTo(token.expiraEn().truncatedTo(ChronoUnit.SECONDS));
		assertThat(token.expiraEn()).isAfter(Instant.now().plusSeconds(29 * 60));
		assertThat(token.expiraEn()).isBefore(Instant.now().plusSeconds(31 * 60));
	}

	@Test
	void unTokenFirmadoConOtraClaveEsRechazado() {
		String secretoA = claveAleatoria();
		String secretoB = claveAleatoria();
		JwtService service = new JwtService(new NimbusJwtEncoder(new ImmutableSecret<>(clave(secretoA))),
				new JwtProperties(secretoA, 30));
		String token = service.emitir(usuario(1, "dueno", RolNombre.DUENO, true)).valor();

		NimbusJwtDecoder decoderB = NimbusJwtDecoder.withSecretKey(clave(secretoB)).macAlgorithm(MacAlgorithm.HS256)
				.build();

		assertThatThrownBy(() -> decoderB.decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void lasPropiedadesRechazanSecretoAusenteOCorto() {
		assertThatThrownBy(() -> new JwtProperties("", 30))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("JWT_SECRET");
		assertThatThrownBy(() -> new JwtProperties("corta", 30))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("demasiado corta");
		assertThatThrownBy(() -> new JwtProperties(claveAleatoria(), 0))
				.isInstanceOf(IllegalStateException.class);
	}
}
