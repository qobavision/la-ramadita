package com.laramadita.backend.usuarios.repository;

import com.laramadita.backend.usuarios.model.Rol;
import com.laramadita.backend.usuarios.model.RolNombre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Integer> {

	Optional<Rol> findByNombre(RolNombre nombre);
}
