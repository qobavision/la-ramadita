package com.laramadita.backend.empleados.repository;

import com.laramadita.backend.empleados.model.Cargo;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoRepository extends JpaRepository<Cargo, Integer> {

	Optional<Cargo> findByNombre(String nombre);

	List<Cargo> findAllByActivoTrueOrderByNombreAsc();
}
