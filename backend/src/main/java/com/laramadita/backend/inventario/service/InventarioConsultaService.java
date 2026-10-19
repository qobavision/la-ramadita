package com.laramadita.backend.inventario.service;

import com.laramadita.backend.inventario.api.dto.MovimientoResponse;
import com.laramadita.backend.inventario.repository.InventarioMovimientoRepository;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventarioConsultaService {

	private final InventarioMovimientoRepository movimientos;

	public InventarioConsultaService(InventarioMovimientoRepository movimientos) {
		this.movimientos = movimientos;
	}

	public List<MovimientoResponse> listar(Integer insumoId) {
		return movimientos.listarRecientes(insumoId, PageRequest.of(0, 200)).stream()
				.map(MovimientoResponse::de)
				.toList();
	}
}
