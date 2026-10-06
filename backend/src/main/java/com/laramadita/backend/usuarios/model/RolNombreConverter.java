package com.laramadita.backend.usuarios.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Traduce {@link RolNombre} al texto almacenado en {@code dbo.roles.nombre}. */
@Converter(autoApply = true)
public class RolNombreConverter implements AttributeConverter<RolNombre, String> {

	@Override
	public String convertToDatabaseColumn(RolNombre atributo) {
		return atributo == null ? null : atributo.valorBaseDatos();
	}

	@Override
	public RolNombre convertToEntityAttribute(String valor) {
		return valor == null ? null : RolNombre.desdeBaseDatos(valor);
	}
}
