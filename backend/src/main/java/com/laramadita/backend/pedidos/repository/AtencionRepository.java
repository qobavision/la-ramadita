package com.laramadita.backend.pedidos.repository;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.EstadoAtencion;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AtencionRepository extends JpaRepository<Atencion, Integer> {

	@EntityGraph(attributePaths = { "mesa", "mesa.zona", "mesero" })
	List<Atencion> findByEstadoInOrderByFechaInicioAsc(Collection<EstadoAtencion> estados);

	@EntityGraph(attributePaths = { "mesa", "mesa.zona", "mesero", "turno", "turno.unidad" })
	Optional<Atencion> findWithRelacionesById(Integer id);

	/** Bloquea la atención mientras se cobra: evita doble cobro por doble toque o dos cajeros a la vez. */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Atencion a where a.id = :id")
	Optional<Atencion> findParaActualizar(@Param("id") Integer id);

	boolean existsByMesa_IdAndEstadoIn(Integer mesaId, Collection<EstadoAtencion> estados);
}
