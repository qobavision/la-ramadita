package com.laramadita.backend.compras.repository;

import com.laramadita.backend.compras.model.Recepcion;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecepcionRepository extends JpaRepository<Recepcion, Integer> {

	@EntityGraph(attributePaths = { "detalles", "detalles.compraDetalle", "recibidoPor" })
	List<Recepcion> findByCompra_IdOrderByFechaRecepcionAsc(Integer compraId);
}
