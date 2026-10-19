package com.laramadita.backend.zonas.repository;

import com.laramadita.backend.zonas.model.Zona;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ZonaRepository extends JpaRepository<Zona, Integer> {

	@EntityGraph(attributePaths = "mesas")
	List<Zona> findAllByOrderByNombreAsc();

	@EntityGraph(attributePaths = "mesas")
	@Query("select z from Zona z where z.id = :id")
	Optional<Zona> findWithMesasById(@Param("id") Integer id);

	Optional<Zona> findByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
