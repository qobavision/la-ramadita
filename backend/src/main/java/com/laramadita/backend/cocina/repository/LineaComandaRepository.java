package com.laramadita.backend.cocina.repository;

import com.laramadita.backend.pedidos.model.PedidoDetalle;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Líneas de pedido que Cocina marca como preparadas. */
public interface LineaComandaRepository extends JpaRepository<PedidoDetalle, Integer> {

	@EntityGraph(attributePaths = { "pedido", "producto" })
	Optional<PedidoDetalle> findLineaById(Integer id);

	/** Solo el id (no carga la línea), para bloquear la atención antes de leerla. */
	@Query("select d.pedido.atencion.id from PedidoDetalle d where d.id = :id")
	Optional<Integer> atencionDeLinea(@Param("id") Integer detalleId);
}
