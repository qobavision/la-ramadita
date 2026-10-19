package com.laramadita.backend.insumos.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class UnidadMedidaTest {

	@Test
	void convierteKilogramoAGramoYLitroAMililitro() {
		assertThat(UnidadMedida.KILOGRAMO.factorHacia(UnidadMedida.GRAMO))
				.contains(new BigDecimal("1000"));
		assertThat(UnidadMedida.GRAMO.factorHacia(UnidadMedida.KILOGRAMO))
				.contains(new BigDecimal("0.001"));
		assertThat(UnidadMedida.LITRO.factorHacia(UnidadMedida.MILILITRO))
				.contains(new BigDecimal("1000"));
		assertThat(UnidadMedida.MILILITRO.factorHacia(UnidadMedida.LITRO))
				.contains(new BigDecimal("0.001"));
	}

	@Test
	void noConvierteUnidadNiPesoAVolumen() {
		assertThat(UnidadMedida.UNIDAD.factorHacia(UnidadMedida.KILOGRAMO)).isEmpty();
		assertThat(UnidadMedida.UNIDAD.factorHacia(UnidadMedida.GRAMO)).isEmpty();
		assertThat(UnidadMedida.GRAMO.factorHacia(UnidadMedida.LITRO)).isEmpty();
		assertThat(UnidadMedida.KILOGRAMO.factorHacia(UnidadMedida.UNIDAD)).isEmpty();
	}
}
