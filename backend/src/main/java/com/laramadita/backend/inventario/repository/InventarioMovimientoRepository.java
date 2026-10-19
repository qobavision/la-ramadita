package com.laramadita.backend.inventario.repository;

import com.laramadita.backend.inventario.model.InventarioMovimiento;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {

	@Query("""
			select m from InventarioMovimiento m
			join fetch m.insumo
			join fetch m.usuario
			where (:insumoId is null or m.insumo.id = :insumoId)
			order by m.fechaMovimiento desc, m.id desc
			""")
	List<InventarioMovimiento> listarRecientes(@Param("insumoId") Integer insumoId, Pageable pageable);
}
