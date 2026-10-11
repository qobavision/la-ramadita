package com.laramadita.backend.shared.texto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import org.junit.jupiter.api.Test;

class DocumentoPeruTest {

	@Test
	void aceptaRucDeOnceDigitos() {
		assertThat(DocumentoPeru.exigirDocumento("RUC", "20123456789")).isEqualTo("20123456789");
	}

	@Test
	void rechazaRucInvalido() {
		assertThatThrownBy(() -> DocumentoPeru.exigirDocumento("RUC", "2012345678"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("11");
	}

	@Test
	void aceptaDniDeOchoDigitos() {
		assertThat(DocumentoPeru.exigirDocumento("DNI", "45678901")).isEqualTo("45678901");
	}

	@Test
	void rechazaDniInvalido() {
		assertThatThrownBy(() -> DocumentoPeru.exigirDocumento("dni", "4567890"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("8");
	}

	@Test
	void celularOpcionalVacioQuedaNulo() {
		assertThat(DocumentoPeru.celularOpcional("")).isNull();
		assertThat(DocumentoPeru.celularOpcional("987654321")).isEqualTo("987654321");
	}

	@Test
	void rechazaCelularIncompleto() {
		assertThatThrownBy(() -> DocumentoPeru.celularOpcional("98765432"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("9");
	}
}
