package com.laramadita.backend.productos.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SeccionCartaTest {

	@Test
	void criolloSoloAceptaEntradaYSegundo() {
		assertThat(SeccionCarta.MENU_CRIOLLO_ENTRADA.acepta(CategoriaProducto.ENTRADA)).isTrue();
		assertThat(SeccionCarta.MENU_CRIOLLO_ENTRADA.acepta(CategoriaProducto.SEGUNDO)).isFalse();
		assertThat(SeccionCarta.MENU_CRIOLLO_SEGUNDO.acepta(CategoriaProducto.SEGUNDO)).isTrue();
		assertThat(SeccionCarta.MENU_CRIOLLO_SEGUNDO.acepta(CategoriaProducto.PLATO_EJECUTIVO)).isFalse();
		assertThat(SeccionCarta.MENU_EJECUTIVO.acepta(CategoriaProducto.PLATO)).isTrue();
		assertThat(SeccionCarta.MENU_EJECUTIVO.acepta(CategoriaProducto.MENU)).isTrue();
		assertThat(SeccionCarta.A_LA_CARTA.acepta(CategoriaProducto.PLATO_MARINO)).isTrue();
		assertThat(SeccionCarta.A_LA_CARTA.acepta(CategoriaProducto.PLATO_EJECUTIVO)).isFalse();
	}
}
