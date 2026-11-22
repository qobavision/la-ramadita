package com.laramadita.backend.caja.repository;

import com.laramadita.backend.caja.model.EstadoPago;
import com.laramadita.backend.caja.model.Pago;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

	@EntityGraph(attributePaths = { "metodo", "usuario" })
	List<Pago> findByAtencion_IdAndEstadoOrderByFechaAsc(Integer atencionId, EstadoPago estado);

	@EntityGraph(attributePaths = { "metodo" })
	List<Pago> findByAtencion_IdInAndEstado(Collection<Integer> atencionIds, EstadoPago estado);

	@EntityGraph(attributePaths = { "metodo", "usuario", "atencion" })
	List<Pago> findBySesion_IdAndEstadoOrderByFechaAsc(Integer sesionId, EstadoPago estado);
}
