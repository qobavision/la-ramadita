package com.laramadita.backend.productos.repository;

import com.laramadita.backend.productos.model.AmbitoCarta;
import com.laramadita.backend.productos.model.Carta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartaRepository extends JpaRepository<Carta, Integer> {

	@EntityGraph(attributePaths = { "detalles", "detalles.producto" })
	Optional<Carta> findWithDetallesById(Integer id);

	@EntityGraph(attributePaths = { "detalles", "detalles.producto" })
	List<Carta> findByAmbitoOrderByNombreAsc(AmbitoCarta ambito);

	@EntityGraph(attributePaths = { "detalles", "detalles.producto" })
	Optional<Carta> findByAmbitoAndVigenteTrue(AmbitoCarta ambito);

	boolean existsByAmbitoAndNombreIgnoreCase(AmbitoCarta ambito, String nombre);

	boolean existsByAmbitoAndNombreIgnoreCaseAndIdNot(AmbitoCarta ambito, String nombre, Integer id);

	boolean existsByAmbitoAndVigenteTrue(AmbitoCarta ambito);

	@Modifying(flushAutomatically = true)
	@Query("update Carta c set c.vigente = false where c.ambito = :ambito and c.vigente = true")
	void quitarVigenteDe(@Param("ambito") AmbitoCarta ambito);
}
