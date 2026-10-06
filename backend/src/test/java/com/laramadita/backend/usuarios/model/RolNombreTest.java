package com.laramadita.backend.usuarios.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RolNombreTest {

	@Test
	void losRolesCoincidenLiteralmenteConLaBase() {
		assertThat(RolNombre.ADMINISTRADOR.valorBaseDatos()).isEqualTo("ADMINISTRADOR");
		assertThat(RolNombre.desdeBaseDatos("ADMINISTRADOR")).isEqualTo(RolNombre.ADMINISTRADOR);
		assertThat(RolNombre.SUPERVISOR.valorBaseDatos()).isEqualTo("SUPERVISOR");
		assertThat(RolNombre.MESERO.valorBaseDatos()).isEqualTo("MESERO");
		assertThat(RolNombre.COCINA.valorBaseDatos()).isEqualTo("COCINA");
		assertThat(RolNombre.CAJA.valorBaseDatos()).isEqualTo("CAJA");
	}

	@Test
	void laAutoridadDeSpringSecurityUsaLaConstante() {
		assertThat(RolNombre.ADMINISTRADOR.authority()).isEqualTo("ROLE_ADMINISTRADOR");
		assertThat(RolNombre.COCINA.authority()).isEqualTo("ROLE_COCINA");
	}

	@Test
	void desdeAceptaLaConstanteSinDistinguirMayusculasNiEspacios() {
		assertThat(RolNombre.desde("administrador")).contains(RolNombre.ADMINISTRADOR);
		assertThat(RolNombre.desde("  caja ")).contains(RolNombre.CAJA);
		assertThat(RolNombre.desde("COCINERO")).isEmpty();
		assertThat(RolNombre.desde(null)).isEmpty();
	}

	@Test
	void elAntiguoNombreDuenoYaNoEsValido() {
		assertThat(RolNombre.desde("DUENO")).isEmpty();
		assertThat(RolNombre.desde("DUE\u00D1O")).isEmpty();
	}

	@Test
	void unValorDesconocidoEnBaseDeDatosFallaExplicitamente() {
		assertThatThrownBy(() -> RolNombre.desdeBaseDatos("ADMIN"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void elConvertidorJpaEsSimetrico() {
		RolNombreConverter conv = new RolNombreConverter();
		for (RolNombre rol : RolNombre.values()) {
			assertThat(conv.convertToEntityAttribute(conv.convertToDatabaseColumn(rol))).isEqualTo(rol);
		}
		assertThat(conv.convertToDatabaseColumn(null)).isNull();
		assertThat(conv.convertToEntityAttribute(null)).isNull();
	}
}
