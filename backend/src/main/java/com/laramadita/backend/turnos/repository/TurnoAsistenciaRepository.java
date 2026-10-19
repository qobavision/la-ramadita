package com.laramadita.backend.turnos.repository;

import com.laramadita.backend.turnos.model.Turno;
import com.laramadita.backend.turnos.model.TurnoAsistencia;
import com.laramadita.backend.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnoAsistenciaRepository extends JpaRepository<TurnoAsistencia, Integer> {

	boolean existsByTurnoAndUsuario(Turno turno, Usuario usuario);

	@EntityGraph(attributePaths = { "usuario", "usuario.rol", "usuario.empleado" })
	List<TurnoAsistencia> findByTurnoOrderByHoraIngresoAsc(Turno turno);

	@EntityGraph(attributePaths = { "usuario", "usuario.rol", "usuario.empleado" })
	Optional<TurnoAsistencia> findByTurnoAndUsuario(Turno turno, Usuario usuario);
}
