package com.laramadita.backend.zonas.repository;

import com.laramadita.backend.zonas.model.Mesa;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MesaRepository extends JpaRepository<Mesa, Integer> {

	@EntityGraph(attributePaths = "zona")
	List<Mesa> findAllByOrderByZona_NombreAscIdAsc();

	/** Zonas con al menos una mesa que ya tuvo atenciones (M06): no se eliminan, solo se inactivan. */
	@Query(value = """
			SELECT DISTINCT m.zona_id FROM dbo.mesas m
			WHERE EXISTS (SELECT 1 FROM dbo.atenciones a WHERE a.mesa_id = m.mesa_id)
			""", nativeQuery = true)
	List<Integer> zonasConHistorial();

	/** Mesas de la zona que ya tuvieron atenciones; no pueden borrarse físicamente. */
	@Query(value = """
			SELECT m.mesa_id FROM dbo.mesas m
			WHERE m.zona_id = :zonaId
			  AND EXISTS (SELECT 1 FROM dbo.atenciones a WHERE a.mesa_id = m.mesa_id)
			""", nativeQuery = true)
	List<Integer> mesasConHistorial(@Param("zonaId") Integer zonaId);
}
