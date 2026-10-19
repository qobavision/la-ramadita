package com.laramadita.backend.inventario.api;

import com.laramadita.backend.inventario.api.dto.MovimientoResponse;
import com.laramadita.backend.inventario.service.InventarioConsultaService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventario/movimientos")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class InventarioMovimientoController {

	private final InventarioConsultaService service;

	public InventarioMovimientoController(InventarioConsultaService service) {
		this.service = service;
	}

	@GetMapping
	public List<MovimientoResponse> listar(@RequestParam(required = false) Integer insumoId) {
		return service.listar(insumoId);
	}
}
