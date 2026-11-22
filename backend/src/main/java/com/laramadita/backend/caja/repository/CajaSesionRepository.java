package com.laramadita.backend.caja.repository;

import com.laramadita.backend.caja.model.CajaSesion;
import com.laramadita.backend.caja.model.EstadoCaja;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Sesiones de caja. V1 define {@code UX_caja_abierta_turno}: una sola caja
 * abierta por turno, compartida por quienes cobran (cada pago guarda su usuario).
 */
public interface CajaSesionRepository extends JpaRepository<CajaSesion, Integer> {

	@EntityGraph(attributePaths = { "turno", "turno.unidad", "usuarioApertura" })
	Optional<CajaSesion> findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja estado);

	@EntityGraph(attributePaths = { "turno", "turno.unidad", "usuarioApertura" })
	Optional<CajaSesion> findFirstByTurno_IdAndEstado(Integer turnoId, EstadoCaja estado);

	@EntityGraph(attributePaths = { "turno", "turno.unidad", "usuarioApertura", "usuarioCierre" })
	List<CajaSesion> findTop100ByOrderByFechaAperturaDesc();
}
