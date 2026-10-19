package com.laramadita.backend.zonas.service;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.texto.TextoDescriptivo;
import com.laramadita.backend.zonas.api.dto.MesaResponse;
import com.laramadita.backend.zonas.api.dto.ZonaRequest;
import com.laramadita.backend.zonas.api.dto.ZonaResponse;
import com.laramadita.backend.zonas.model.Zona;
import com.laramadita.backend.zonas.repository.MesaRepository;
import com.laramadita.backend.zonas.repository.ZonaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ZonaService {

	private final ZonaRepository zonas;
	private final MesaRepository mesas;

	public ZonaService(ZonaRepository zonas, MesaRepository mesas) {
		this.zonas = zonas;
		this.mesas = mesas;
	}

	@Transactional(readOnly = true)
	public List<ZonaResponse> listar() {
		return zonas.findAllByOrderByNombreAsc().stream().map(ZonaResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public List<MesaResponse> listarMesas() {
		return mesas.findAllByOrderByZona_NombreAscIdAsc().stream().map(MesaResponse::de).toList();
	}

	public ZonaResponse crear(ZonaRequest request) {
		String nombre = exigirNombre(request.nombre());
		exigirNombreDisponible(nombre, null);
		Zona zona = new Zona(nombre, request.cajas(), request.cocinas());
		zona.sincronizarMesas(request.cantidadMesas());
		return ZonaResponse.de(zonas.save(zona));
	}

	public ZonaResponse actualizar(Integer id, ZonaRequest request) {
		Zona zona = buscar(id);
		String nombre = exigirNombre(request.nombre());
		exigirNombreDisponible(nombre, id);
		zona.actualizar(nombre, request.cajas(), request.cocinas());
		zona.sincronizarMesas(request.cantidadMesas());
		return ZonaResponse.de(zona);
	}

	public ZonaResponse cambiarActivo(Integer id) {
		Zona zona = buscar(id);
		if (zona.isActivo()) {
			zona.desactivar();
		} else {
			zona.activar();
		}
		return ZonaResponse.de(zona);
	}

	public void eliminar(Integer id) {
		Zona zona = buscar(id);
		if (!zona.sePuedeEliminar()) {
			throw new ReglaNegocioException("No se puede eliminar: hay mesas ocupadas");
		}
		zonas.delete(zona);
	}

	private Zona buscar(Integer id) {
		return zonas.findWithMesasById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró la zona"));
	}

	private void exigirNombreDisponible(String nombre, Integer idActual) {
		zonas.findByNombreIgnoreCase(nombre).ifPresent(existente -> {
			if (idActual != null && existente.getId().equals(idActual)) {
				return;
			}
			throw new ConflictoException("Ya existe una zona con el nombre " + nombre);
		});
	}

	private static String exigirNombre(String nombre) {
		String normalizado = TextoDescriptivo.normalizar(nombre);
		if (normalizado.isBlank()) {
			throw new ReglaNegocioException("Ingresa el nombre de la zona");
		}
		return normalizado;
	}
}
