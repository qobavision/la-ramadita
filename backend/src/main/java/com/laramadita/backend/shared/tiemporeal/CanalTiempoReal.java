package com.laramadita.backend.shared.tiemporeal;

import com.laramadita.backend.usuarios.model.Usuario;

/**
 * Canal de avisos que una pantalla puede pedir al conectarse
 * ({@code ?canales=cocina}). Cada módulo declara el suyo y decide quién puede
 * escucharlo, de modo que la regla de acceso vive en el módulo y no aquí.
 */
public interface CanalTiempoReal {

	/** Nombre del canal, en minúsculas (p. ej. {@code cocina}). */
	String nombre();

	/** Si el usuario puede recibir los avisos de este canal. */
	boolean permite(Usuario usuario);
}
