package com.laramadita.backend.ventas.api;

import com.laramadita.backend.ventas.api.dto.VentaDetalleResponse;
import com.laramadita.backend.ventas.api.dto.VentaResumenResponse;
import com.laramadita.backend.ventas.service.FiltroVentas;
import com.laramadita.backend.ventas.service.VentaConsultaService;
import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * M11 Ventas e historial. Solo ADMINISTRADOR consulta las ventas históricas
 * (AGENTS.md §7). Fechas en formato AAAA-MM-DD (días de Lima); unidad
 * RESTAURANTE, POLLERIA o vacía para el consolidado.
 */
@RestController
@RequestMapping("/api/v1/ventas")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class VentaController {

	private final VentaConsultaService ventas;
	private final Clock clock;

	public VentaController(VentaConsultaService ventas, Clock clock) {
		this.ventas = ventas;
		this.clock = clock;
	}

	@GetMapping
	public List<VentaResumenResponse> listar(
			@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta,
			@RequestParam(required = false) String unidad) {
		return ventas.listar(FiltroVentas.de(desde, hasta, unidad, clock));
	}

	@GetMapping("/{atencionId}")
	public VentaDetalleResponse detalle(@PathVariable Integer atencionId) {
		return ventas.detalle(atencionId);
	}
}
