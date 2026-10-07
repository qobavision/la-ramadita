package com.laramadita.backend.empleados.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Edición de empleado. El DNI puede omitirse solo si el registro heredado
 * todavía no tiene uno; si se envía, debe ser de 8 dígitos.
 */
public record EmpleadoActualizarRequest(
		@NotBlank @Size(min = 2, max = 80) String nombres,
		@NotBlank @Size(min = 2, max = 80) String apellidos,
		@Pattern(regexp = "\\d{8}", message = "El DNI debe tener exactamente 8 dígitos") String dni,
		@Size(max = 20) @Pattern(regexp = "|\\d{6,15}", message = "El teléfono debe tener entre 6 y 15 dígitos") String telefono,
		@Email @Size(max = 120) String correoPersonal,
		@NotNull Integer cargoId,
		@NotNull Boolean activo) {
}
