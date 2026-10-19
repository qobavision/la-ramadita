package com.laramadita.backend.productos.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoriaProductoTest {

	@Test
	void platoGenericoSeGuardaComoEjecutivo() {
		assertThat(CategoriaProducto.desde("PLATO")).contains(CategoriaProducto.PLATO_EJECUTIVO);
		assertThat(CategoriaProducto.desde("plato ejecutivo")).contains(CategoriaProducto.PLATO_EJECUTIVO);
		assertThat(CategoriaProducto.desde("Plato marino")).contains(CategoriaProducto.PLATO_MARINO);
		assertThat(CategoriaProducto.desde("plato chifa")).contains(CategoriaProducto.CHIFA);
		assertThat(CategoriaProducto.desde("plato a la brasa")).contains(CategoriaProducto.POLLO_A_LA_BRASA);
	}

	@Test
	void recetaAceptaLasCategoriasDeCarta() {
		assertThat(CategoriaProducto.ENTRADA.esDeReceta()).isTrue();
		assertThat(CategoriaProducto.PLATO_EJECUTIVO.esDeReceta()).isTrue();
		assertThat(CategoriaProducto.COMBO.esDeReceta()).isFalse();
		assertThat(CategoriaProducto.EXTRA.esDeReceta()).isTrue();
		assertThat(CategoriaProducto.PLATO.paraPersistir()).isEqualTo(CategoriaProducto.PLATO_EJECUTIVO);
	}
}
