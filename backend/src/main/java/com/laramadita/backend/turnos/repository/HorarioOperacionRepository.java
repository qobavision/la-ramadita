package com.laramadita.backend.turnos.repository;

import com.laramadita.backend.turnos.model.HorarioOperacion;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioOperacionRepository extends JpaRepository<HorarioOperacion, Integer> {

	@EntityGraph(attributePaths = "unidad")
	List<HorarioOperacion> findAllByOrderByUnidad_IdAsc();

	@EntityGraph(attributePaths = "unidad")
	Optional<HorarioOperacion> findByUnidad_Nombre(NombreUnidad nombre);
}
