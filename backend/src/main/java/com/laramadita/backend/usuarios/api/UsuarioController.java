package com.laramadita.backend.usuarios.api;

import com.laramadita.backend.usuarios.api.dto.PasswordRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioActualizarRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioRequest;
import com.laramadita.backend.usuarios.api.dto.UsuarioResponse;
import com.laramadita.backend.usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administración de cuentas de acceso. Solo ADMINISTRADOR. */
@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

	private final UsuarioService service;

	public UsuarioController(UsuarioService service) {
		this.service = service;
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return service.listar();
	}

	@GetMapping("/{id}")
	public UsuarioResponse obtener(@PathVariable Integer id) {
		return service.obtener(id);
	}

	@PostMapping
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request, Authentication auth) {
		UsuarioResponse creado = service.crear(request, auth.getName());
		return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioActualizarRequest request,
			Authentication auth) {
		return service.actualizar(id, request, auth.getName());
	}

	@PatchMapping("/{id}/password")
	public UsuarioResponse restablecerPassword(@PathVariable Integer id, @Valid @RequestBody PasswordRequest request,
			Authentication auth) {
		return service.restablecerPassword(id, request, auth.getName());
	}

	@PatchMapping("/{id}/desactivar")
	public UsuarioResponse desactivar(@PathVariable Integer id, Authentication auth) {
		return service.desactivar(id, auth.getName());
	}

	@PatchMapping("/{id}/archivar")
	public UsuarioResponse archivar(@PathVariable Integer id, Authentication auth) {
		return service.archivar(id, auth.getName());
	}

	@PatchMapping("/{id}/activar")
	public UsuarioResponse activar(@PathVariable Integer id, Authentication auth) {
		return service.activar(id, auth.getName());
	}
}
