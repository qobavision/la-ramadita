package com.laramadita.backend.turnos.repository;

import com.laramadita.backend.turnos.model.EstadoTurno;
import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.unidadesnegocio.model.UnidadNegocio;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TurnoRepository extends JpaRepository<Turno, Integer> {

	@EntityGraph(attributePaths = { "unidad", "usuarioApertura", "usuarioCierre" })
	Optional<Turno> findFirstByEstadoInOrderByFechaAperturaDesc(Collection<EstadoTurno> estados);

	boolean existsByUnidadAndEstadoIn(UnidadNegocio unidad, Collection<EstadoTurno> estados);

	boolean existsByEstadoIn(Collection<EstadoTurno> estados);

	boolean existsByUnidadAndFechaAperturaGreaterThanEqualAndFechaAperturaLessThan(
			UnidadNegocio unidad, java.time.LocalDateTime desde, java.time.LocalDateTime hasta);

	@EntityGraph(attributePaths = { "unidad", "usuarioApertura", "usuarioCierre" })
	@Query("select t from Turno t where t.id = :id")
	Optional<Turno> findWithRelacionesById(@Param("id") Integer id);

	@EntityGraph(attributePaths = { "unidad", "usuarioApertura", "usuarioCierre" })
	Optional<Turno> findFirstByEstadoOrderByFechaCierreDesc(EstadoTurno estado);
}
