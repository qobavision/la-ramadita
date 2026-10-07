package com.laramadita.backend.empleados.repository;

import com.laramadita.backend.empleados.model.Empleado;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

	List<Empleado> findAllByOrderByApellidosAscNombresAsc();

	@Query("""
			select e from Empleado e
			left join fetch e.usuario
			where e.id = :id
			""")
	Optional<Empleado> findByIdConUsuario(@Param("id") Integer id);

	@Query("""
			select distinct e from Empleado e
			left join fetch e.usuario
			order by e.apellidos, e.nombres
			""")
	List<Empleado> findAllConUsuario();

	boolean existsByDni(String dni);

	boolean existsByDniAndIdNot(String dni, Integer id);
}
