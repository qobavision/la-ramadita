package com.laramadita.backend.usuarios.repository;

import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

	Optional<Usuario> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByUsernameAndIdNot(String username, Integer id);

	List<Usuario> findAllByOrderByUsernameAsc();

	boolean existsByEmpleado_Id(Integer empleadoId);

	Optional<Usuario> findByEmpleado_Id(Integer empleadoId);

	/** Cuenta los usuarios activos con el rol indicado, excluyendo un usuario concreto. */
	@Query("""
			select count(u) from Usuario u
			where u.rol.nombre = :rol and u.activo = true and u.archivado = false and u.id <> :excluirId
			""")
	long contarActivosPorRolExcluyendo(@Param("rol") RolNombre rol, @Param("excluirId") Integer excluirId);
}
