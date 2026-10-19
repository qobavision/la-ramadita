package com.laramadita.backend.zonas.repository;

import com.laramadita.backend.zonas.model.Mesa;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Integer> {

	@EntityGraph(attributePaths = "zona")
	List<Mesa> findAllByOrderByZona_NombreAscIdAsc();
}
