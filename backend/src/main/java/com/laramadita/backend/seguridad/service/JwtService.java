package com.laramadita.backend.seguridad.service;

import com.laramadita.backend.seguridad.config.JwtProperties;
import com.laramadita.backend.usuarios.model.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite tokens JWT firmados con HS256. */
@Service
public class JwtService {

	public static final String EMISOR = "la-ramadita";
	public static final String CLAIM_ROL = "rol";

	private final JwtEncoder encoder;
	private final JwtProperties props;

	public JwtService(JwtEncoder encoder, JwtProperties props) {
		this.encoder = encoder;
		this.props = props;
	}

	public Token emitir(Usuario usuario) {
		Instant ahora = Instant.now();
		Instant expira = ahora.plus(Duration.ofMinutes(props.expirationMinutes()));
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(EMISOR)
				.issuedAt(ahora)
				.expiresAt(expira)
				.subject(usuario.getUsername())
				.claim(CLAIM_ROL, usuario.getRol().getNombre().name())
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String valor = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new Token(valor, expira);
	}

	public record Token(String valor, Instant expiraEn) {
	}
}
