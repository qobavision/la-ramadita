package com.laramadita.backend.usuarios.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RolNombreTest {

	@Test
	void duenoSeMapeaAlValorOficialConEnie() {
		assertThat(RolNombre.DUENO.valorBaseDatos()).isEqualTo("DUE\u00D1O");
		assertThat(RolNombre.desdeBaseDatos("DUE\u00D1O")).isEqualTo(RolNombre.DUENO);
	}

	@Test
	void losDemasRolesCoincidenLiteralmenteConLaBase() {
		assertThat(RolNombre.SUPERVISOR.valorBaseDatos()).isEqualTo("SUPERVISOR");
		assertThat(RolNombre.MESERO.valorBaseDatos()).isEqualTo("MESERO");
		assertThat(RolNombre.COCINA.valorBaseDatos()).isEqualTo("COCINA");
		assertThat(RolNombre.CAJA.valorBaseDatos()).isEqualTo("CAJA");
	}

	@Test
	void laAutoridadDeSpringSecurityUsaLaConstanteAscii() {
		assertThat(RolNombre.DUENO.authority()).isEqualTo("ROLE_DUENO");
		assertThat(RolNombre.COCINA.authority()).isEqualTo("ROLE_COCINA");
	}

	@Test
	void desdeAceptaConstanteOValorDeBaseSinDistinguirMayusculas() {
		assertThat(RolNombre.desde("dueno")).contains(RolNombre.DUENO);
		assertThat(RolNombre.desde("DUE\u00D1O")).contains(RolNombre.DUENO);
		assertThat(RolNombre.desde("  caja ")).contains(RolNombre.CAJA);
		assertThat(RolNombre.desde("COCINERO")).isEmpty();
		assertThat(RolNombre.desde(null)).isEmpty();
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
