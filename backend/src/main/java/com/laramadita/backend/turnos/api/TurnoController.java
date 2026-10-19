package com.laramadita.backend.turnos.api;

import com.laramadita.backend.turnos.api.dto.FuncionTurnoRequest;
import com.laramadita.backend.turnos.api.dto.HorarioRequest;
import com.laramadita.backend.turnos.api.dto.HorarioResponse;
import com.laramadita.backend.turnos.api.dto.PersonalTurnoResponse;
import com.laramadita.backend.turnos.api.dto.TurnoRequest;
import com.laramadita.backend.turnos.api.dto.TurnoResponse;
import com.laramadita.backend.turnos.service.TurnoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
public class TurnoController {

	private final TurnoService service;

	public TurnoController(TurnoService service) {
		this.service = service;
	}

	@GetMapping("/horarios")
	public List<HorarioResponse> horarios() {
		return service.listarHorarios();
	}

	@PutMapping("/horarios/{ambito}")
	public HorarioResponse actualizarHorario(@PathVariable String ambito, @Valid @RequestBody HorarioRequest request) {
		return service.actualizarHorario(ambito, request);
	}

	@GetMapping("/turnos/actual")
	public TurnoResponse actual() {
		return service.actual();
	}

	@GetMapping("/turnos/actual/personal")
	public List<PersonalTurnoResponse> personalActual() {
		return service.personalActual();
	}

	@PatchMapping("/turnos/actual/personal/{usuarioId}")
	public PersonalTurnoResponse asignarFuncion(
			@PathVariable Integer usuarioId,
			@RequestBody FuncionTurnoRequest request,
			Authentication auth) {
		return service.asignarFuncion(usuarioId, request, auth.getName());
	}

	@DeleteMapping("/turnos/actual/personal/{usuarioId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void expulsar(@PathVariable Integer usuarioId, Authentication auth) {
		service.expulsar(usuarioId, auth.getName());
	}

	@PostMapping("/turnos")
	public TurnoResponse iniciar(@Valid @RequestBody TurnoRequest request, Authentication auth) {
		return service.iniciar(request.ambito(), auth.getName());
	}

	@PatchMapping("/turnos/{id}/cierre")
	public TurnoResponse prepararCierre(@PathVariable Integer id) {
		return service.prepararCierre(id);
	}

	@PatchMapping("/turnos/{id}/cerrar")
	public TurnoResponse cerrar(@PathVariable Integer id, Authentication auth) {
		return service.cerrar(id, auth.getName());
	}
}
