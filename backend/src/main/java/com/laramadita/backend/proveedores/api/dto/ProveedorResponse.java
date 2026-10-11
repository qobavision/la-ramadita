package com.laramadita.backend.proveedores.api.dto;

import com.laramadita.backend.proveedores.model.Proveedor;

public record ProveedorResponse(
		Integer id,
		String nombreRazonSocial,
		String tipoDocumento,
		String documento,
		String celular,
		String personaContacto,
		boolean activo) {

	public static ProveedorResponse de(Proveedor proveedor) {
		return new ProveedorResponse(
				proveedor.getId(),
				proveedor.getNombreRazonSocial(),
				proveedor.getTipoDocumento() == null ? null : proveedor.getTipoDocumento().name(),
				proveedor.getDocumento(),
				proveedor.getTelefono(),
				proveedor.getPersonaContacto(),
				proveedor.isActivo());
	}
}
