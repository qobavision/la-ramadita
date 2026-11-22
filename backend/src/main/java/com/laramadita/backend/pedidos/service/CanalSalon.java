package com.laramadita.backend.pedidos.service;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.shared.tiemporeal.CanalTiempoReal;
import com.laramadita.backend.shared.tiemporeal.EventoTiempoReal;
import com.laramadita.backend.turnos.service.TurnoService;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Canal WebSocket {@code salon} (AGENTS.md §10): avisa cambios de estado de
 * mesas y atenciones (abrir, pedir cuenta, reabrir, liberar, pagar) a meseros,
 * supervisión y administración, sin recargar la página.
 */
@Component
public class CanalSalon implements CanalTiempoReal {

	public static final String CANAL = "salon";

	private final TurnoService turnos;

	public CanalSalon(TurnoService turnos) {
		this.turnos = turnos;
	}

	@Override
	public String nombre() {
		return CANAL;
	}

	@Override
	public boolean permite(Usuario usuario) {
		RolNombre rol = usuario.getRol().getNombre();
		String funcion = turnos.funcionActualDe(usuario);
		return rol == RolNombre.ADMINISTRADOR || rol == RolNombre.MESERO || rol == RolNombre.SUPERVISOR
				|| RolNombre.MESERO.name().equals(funcion) || RolNombre.SUPERVISOR.name().equals(funcion);
	}

	/** Aviso de cambio en el salón; los datos se vuelven a leer por la API REST. */
	public static EventoTiempoReal aviso(String motivo, Atencion atencion) {
		Map<String, Object> datos = new HashMap<>();
		datos.put("motivo", motivo);
		datos.put("atencionId", atencion.getId());
		datos.put("identificacion", atencion.identificacion());
		datos.put("estado", atencion.getEstado().name());
		if (atencion.getMesa() != null) {
			datos.put("mesaId", atencion.getMesa().getId());
		}
		return EventoTiempoReal.paraCanal(CANAL, "SALON_ACTUALIZADO", datos);
	}
}
