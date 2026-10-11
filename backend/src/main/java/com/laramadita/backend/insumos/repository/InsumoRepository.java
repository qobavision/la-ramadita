package com.laramadita.backend.insumos.repository;

import com.laramadita.backend.insumos.model.Insumo;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InsumoRepository extends JpaRepository<Insumo, Integer> {

	List<Insumo> findByActivoTrueOrderByNombreAsc();

	List<Insumo> findByActivoTrueAndNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

	List<Insumo> findByActivoFalseOrderByNombreAsc();

	List<Insumo> findByActivoFalseAndNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

	List<Insumo> findAllByOrderByNombreAsc();

	List<Insumo> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

	Optional<Insumo> findByNombreIgnoreCase(String nombre);

	@Query(value = """
			SELECT CASE WHEN EXISTS (
				SELECT 1
				FROM dbo.compra_detalle d
				INNER JOIN dbo.compras c ON c.compra_id = d.compra_id
				WHERE d.insumo_id = :insumoId AND c.estado = N'PENDIENTE'
			) THEN 1 ELSE 0 END
			""", nativeQuery = true)
	int existeEnCompraPendiente(@Param("insumoId") Integer insumoId);

	@Query(value = """
			SELECT CASE WHEN EXISTS (
				SELECT 1 FROM dbo.compra_detalle d WHERE d.insumo_id = :insumoId
			) THEN 1 ELSE 0 END
			""", nativeQuery = true)
	int existeEnAlgunaCompra(@Param("insumoId") Integer insumoId);

	@Query(value = """
			SELECT CASE WHEN EXISTS (
				SELECT 1 FROM dbo.inventario_movimientos m WHERE m.insumo_id = :insumoId
			) THEN 1 ELSE 0 END
			""", nativeQuery = true)
	int existeEnMovimientos(@Param("insumoId") Integer insumoId);

	@Query(value = """
			SELECT CASE WHEN EXISTS (
				SELECT 1
				FROM dbo.receta_detalle rd
				INNER JOIN dbo.productos p ON p.producto_id = rd.producto_id
				WHERE rd.insumo_id = :insumoId AND p.activo = 1
			) THEN 1 ELSE 0 END
			""", nativeQuery = true)
	int existeEnRecetaDeProductoActivo(@Param("insumoId") Integer insumoId);

	@Query(value = """
			SELECT CASE WHEN EXISTS (
				SELECT 1 FROM dbo.receta_detalle rd WHERE rd.insumo_id = :insumoId
			) THEN 1 ELSE 0 END
			""", nativeQuery = true)
	int existeEnAlgunaReceta(@Param("insumoId") Integer insumoId);

	long countByActivoTrueAndStockActualLessThanEqualAndStockActualGreaterThan(java.math.BigDecimal minimo,
			java.math.BigDecimal cero);

	long countByActivoTrueAndStockActual(java.math.BigDecimal stock);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Insumo i where i.id = :id")
	Optional<Insumo> bloquearPorId(@Param("id") Integer id);
}
