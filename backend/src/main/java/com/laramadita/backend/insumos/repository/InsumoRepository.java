package com.laramadita.backend.insumos.repository;

import com.laramadita.backend.insumos.model.Insumo;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
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

	@Query(value = """
			SELECT MIN(c.cantidad)
			FROM (
				SELECT rd.cantidad_requerida AS cantidad
				FROM dbo.receta_detalle rd
				WHERE rd.insumo_id = :insumoId
				UNION ALL
				SELECT d.cantidad_comprada
				FROM dbo.compra_detalle d
				WHERE d.insumo_id = :insumoId
				UNION ALL
				SELECT rdet.cantidad
				FROM dbo.recepcion_detalle rdet
				INNER JOIN dbo.compra_detalle d ON d.compra_detalle_id = rdet.compra_detalle_id
				WHERE d.insumo_id = :insumoId
				UNION ALL
				SELECT m.cantidad
				FROM dbo.inventario_movimientos m
				WHERE m.insumo_id = :insumoId
			) c
			""", nativeQuery = true)
	BigDecimal minimaCantidadAsociada(@Param("insumoId") Integer insumoId);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE dbo.receta_detalle
			SET cantidad_requerida = CAST(ROUND(cantidad_requerida * :factor, 3) AS DECIMAL(18,3))
			WHERE insumo_id = :insumoId
			""", nativeQuery = true)
	int convertirCantidadesDeReceta(@Param("insumoId") Integer insumoId, @Param("factor") BigDecimal factor);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE dbo.compra_detalle
			SET cantidad_comprada = CAST(ROUND(cantidad_comprada * :factorCantidad, 3) AS DECIMAL(18,3)),
				cantidad_recibida = CAST(ROUND(cantidad_recibida * :factorRecibida, 3) AS DECIMAL(18,3)),
				costo_unitario = CAST(ROUND(costo_unitario / :factorCosto, 4) AS DECIMAL(18,4))
			WHERE insumo_id = :insumoId
			""", nativeQuery = true)
	int convertirCantidadesDeCompra(
			@Param("insumoId") Integer insumoId,
			@Param("factorCantidad") BigDecimal factorCantidad,
			@Param("factorRecibida") BigDecimal factorRecibida,
			@Param("factorCosto") BigDecimal factorCosto);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE rdet
			SET cantidad = CAST(ROUND(rdet.cantidad * :factor, 3) AS DECIMAL(18,3))
			FROM dbo.recepcion_detalle rdet
			INNER JOIN dbo.compra_detalle d ON d.compra_detalle_id = rdet.compra_detalle_id
			WHERE d.insumo_id = :insumoId
			""", nativeQuery = true)
	int convertirCantidadesDeRecepcion(@Param("insumoId") Integer insumoId, @Param("factor") BigDecimal factor);

	@Modifying(flushAutomatically = true)
	@Query(value = """
			UPDATE dbo.inventario_movimientos
			SET cantidad = CAST(ROUND(cantidad * :factorCantidad, 3) AS DECIMAL(18,3)),
				stock_anterior = CAST(ROUND(stock_anterior * :factorAnterior, 3) AS DECIMAL(18,3)),
				stock_nuevo = CAST(ROUND(stock_nuevo * :factorNuevo, 3) AS DECIMAL(18,3))
			WHERE insumo_id = :insumoId
			""", nativeQuery = true)
	int convertirCantidadesDeMovimiento(
			@Param("insumoId") Integer insumoId,
			@Param("factorCantidad") BigDecimal factorCantidad,
			@Param("factorAnterior") BigDecimal factorAnterior,
			@Param("factorNuevo") BigDecimal factorNuevo);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Insumo i where i.id = :id")
	Optional<Insumo> bloquearPorId(@Param("id") Integer id);
}
