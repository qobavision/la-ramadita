package com.laramadita.backend.caja.repository;

import com.laramadita.backend.caja.model.MetodoPago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {

	List<MetodoPago> findByActivoTrueOrderByIdAsc();

	Optional<MetodoPago> findByNombreIgnoreCase(String nombre);
}
