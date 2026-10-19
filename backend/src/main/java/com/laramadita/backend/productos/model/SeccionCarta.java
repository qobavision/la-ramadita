package com.laramadita.backend.productos.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum SeccionCarta {

	MENU_CRIOLLO,
	MENU_CRIOLLO_ENTRADA,
	MENU_CRIOLLO_SEGUNDO,
	MENU_EJECUTIVO,
	A_LA_CARTA,
	BEBIDA,
	POSTRE,
	OTRO,
	BRASA,
	CHIFA,
	EXTRA,
	COMBO;

	private static final Set<SeccionCarta> RESTAURANTE = Set.of(
			MENU_CRIOLLO, MENU_CRIOLLO_ENTRADA, MENU_CRIOLLO_SEGUNDO, MENU_EJECUTIVO,
			A_LA_CARTA, BEBIDA, POSTRE, OTRO);

	private static final Set<SeccionCarta> POLLERIA = Set.of(
			BRASA, CHIFA, BEBIDA, EXTRA, COMBO, OTRO);

	public static Optional<SeccionCarta> desde(String texto) {
		if (texto == null || texto.isBlank()) {
			return Optional.empty();
		}
		String limpio = texto.trim().toUpperCase(Locale.ROOT);
		return Arrays.stream(values()).filter(v -> v.name().equals(limpio)).findFirst();
	}

	public boolean perteneceA(AmbitoCarta ambito) {
		return ambito == AmbitoCarta.RESTAURANTE ? RESTAURANTE.contains(this) : POLLERIA.contains(this);
	}

	public boolean acepta(CategoriaProducto categoria) {
		CategoriaProducto tipo = categoria == null ? null : categoria.paraPersistir();
		return switch (this) {
			case MENU_CRIOLLO -> tipo == CategoriaProducto.MENU;
			case MENU_CRIOLLO_ENTRADA -> tipo == CategoriaProducto.ENTRADA;
			case MENU_CRIOLLO_SEGUNDO -> tipo == CategoriaProducto.SEGUNDO;
			case MENU_EJECUTIVO -> tipo == CategoriaProducto.MENU || tipo == CategoriaProducto.PLATO_EJECUTIVO;
			case A_LA_CARTA -> tipo == CategoriaProducto.PLATO_MARINO;
			case BEBIDA -> tipo == CategoriaProducto.BEBIDA;
			case POSTRE -> tipo == CategoriaProducto.POSTRE;
			case BRASA -> tipo == CategoriaProducto.POLLO_A_LA_BRASA;
			case CHIFA -> tipo == CategoriaProducto.CHIFA;
			case COMBO -> tipo == CategoriaProducto.COMBO;
			case EXTRA -> tipo == CategoriaProducto.EXTRA || tipo == CategoriaProducto.GUARNICION;
			case OTRO -> tipo == CategoriaProducto.OTRO || tipo == CategoriaProducto.ENSALADA;
		};
	}
}
