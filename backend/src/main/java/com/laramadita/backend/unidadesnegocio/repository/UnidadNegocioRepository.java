package com.laramadita.backend.unidadesnegocio.repository;

import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.unidadesnegocio.model.UnidadNegocio;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnidadNegocioRepository extends JpaRepository<UnidadNegocio, Integer> {

	Optional<UnidadNegocio> findByNombre(NombreUnidad nombre);
}
