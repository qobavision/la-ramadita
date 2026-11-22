package com.laramadita.backend.cocina.api;

import com.laramadita.backend.cocina.api.dto.ComandaResponse;
import com.laramadita.backend.cocina.api.dto.HistorialCocinaResponse;
import com.laramadita.backend.cocina.api.dto.TableroCocinaResponse;
import com.laramadita.backend.cocina.service.CocinaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * M08 Cocina. El acceso por rol permanente o función temporal del turno se
 * valida en {@code AccesoCocina}.
 */
@RestController
@RequestMapping("/api/v1/cocina")
@PreAuthorize("isAuthenticated()")
public class CocinaController {

	private final CocinaService service;

	public CocinaController(CocinaService service) {
		this.service = service;
	}

	@GetMapping("/tablero")
	public TableroCocinaResponse tablero(Authentication auth) {
		return service.tablero(auth.getName());
	}

	@GetMapping("/historial")
	public HistorialCocinaResponse historial(Authentication auth) {
		return service.historial(auth.getName());
	}

	@PostMapping("/detalles/{detalleId}/preparar")
	public ComandaResponse prepararLinea(@PathVariable Integer detalleId, Authentication auth) {
		return service.prepararLinea(detalleId, auth.getName());
	}

	@PostMapping("/comandas/{pedidoId}/preparar")
	public ComandaResponse prepararComanda(@PathVariable Integer pedidoId, Authentication auth) {
		return service.prepararComanda(pedidoId, auth.getName());
	}
}
