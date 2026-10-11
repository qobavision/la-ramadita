package com.laramadita.backend.compras.repository;

import com.laramadita.backend.compras.model.Compra;
import com.laramadita.backend.compras.model.EstadoCompra;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraRepository extends JpaRepository<Compra, Integer> {

	@EntityGraph(attributePaths = { "proveedor", "detalles", "detalles.insumo", "registradoPor" })
	List<Compra> findByEstadoNotOrderByFechaCompraDesc(EstadoCompra estado);

	@EntityGraph(attributePaths = { "proveedor", "detalles", "detalles.insumo", "registradoPor" })
	@Query("select c from Compra c where c.id = :id")
	Optional<Compra> buscarCompleta(@Param("id") Integer id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Compra c where c.id = :id")
	Optional<Compra> bloquearPorId(@Param("id") Integer id);

	@Query(value = "SELECT CAST(NEXT VALUE FOR dbo.seq_compra_codigo AS INT)", nativeQuery = true)
	Integer siguienteNumeroCodigo();

	long countByEstadoIn(List<EstadoCompra> estados);
}
