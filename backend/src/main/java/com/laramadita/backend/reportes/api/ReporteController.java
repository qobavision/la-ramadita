package com.laramadita.backend.reportes.api;

import com.laramadita.backend.reportes.api.dto.ResumenVentasResponse;
import com.laramadita.backend.reportes.service.ReporteService;
import com.laramadita.backend.ventas.service.FiltroVentas;
import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * M12 Reportes y BI. Solo ADMINISTRADOR (Dashboard y Reportes / BI, AGENTS.md §7).
 * Mismos filtros que {@code /ventas}: desde, hasta (AAAA-MM-DD) y unidad.
 */
@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteController {

	private final ReporteService reportes;
	private final Clock clock;

	public ReporteController(ReporteService reportes, Clock clock) {
		this.reportes = reportes;
		this.clock = clock;
	}

	@GetMapping("/resumen")
	public ResumenVentasResponse resumen(
			@RequestParam(required = false) String desde,
			@RequestParam(required = false) String hasta,
			@RequestParam(required = false) String unidad) {
		return reportes.resumen(FiltroVentas.de(desde, hasta, unidad, clock));
	}
}
