package com.laramadita.backend.shared.texto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TextoDescriptivoTest {

	@Test
	void capitalizaYQuitaEspacios() {
		assertThat(TextoDescriptivo.normalizar("  pollo  ")).isEqualTo("Pollo");
		assertThat(TextoDescriptivo.normalizar("POLLO ENTERO")).isEqualTo("Pollo Entero");
		assertThat(TextoDescriptivo.normalizar("papa amarilla")).isEqualTo("Papa Amarilla");
		assertThat(TextoDescriptivo.normalizar("avícola SAN PEDRO")).isEqualTo("Avícola San Pedro");
	}

	@Test
	void trataVariantesComoElMismoNombre() {
		assertThat(TextoDescriptivo.normalizar("Pollo"))
				.isEqualTo(TextoDescriptivo.normalizar("POLLO"))
				.isEqualTo(TextoDescriptivo.normalizar("  pollo  "));
	}
}
