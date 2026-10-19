package com.laramadita.backend.proveedores.service;

import com.laramadita.backend.proveedores.api.dto.ProveedorRequest;
import com.laramadita.backend.proveedores.api.dto.ProveedorResponse;
import com.laramadita.backend.proveedores.model.Proveedor;
import com.laramadita.backend.proveedores.model.TipoDocumento;
import com.laramadita.backend.proveedores.repository.ProveedorRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.shared.texto.DocumentoPeru;
import com.laramadita.backend.shared.texto.TextoDescriptivo;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProveedorService {

	private final ProveedorRepository proveedores;

	public ProveedorService(ProveedorRepository proveedores) {
		this.proveedores = proveedores;
	}

	@Transactional(readOnly = true)
	public List<ProveedorResponse> listar(boolean soloActivos) {
		List<Proveedor> lista = soloActivos
				? proveedores.findByActivoTrueAndArchivadoFalseOrderByNombreRazonSocialAsc()
				: proveedores.findByArchivadoFalseOrderByNombreRazonSocialAsc();
		return lista.stream().map(ProveedorResponse::de).toList();
	}

	@Transactional(readOnly = true)
	public ProveedorResponse obtener(Integer id) {
		return ProveedorResponse.de(buscar(id));
	}

	public ProveedorResponse crear(ProveedorRequest request) {
		DatosProveedor datos = validar(request, null);
		Proveedor creado = proveedores.save(new Proveedor(datos.nombre(), datos.tipo(), datos.documento(),
				datos.celular(), datos.contacto()));
		if (Boolean.FALSE.equals(request.activo())) {
			creado.desactivar();
		}
		return ProveedorResponse.de(creado);
	}

	public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
		Proveedor proveedor = buscar(id);
		if (proveedor.isArchivado()) {
			throw new ReglaNegocioException("Un proveedor eliminado no puede reactivarse desde Editar");
		}
		DatosProveedor datos = validar(request, id);
		proveedor.actualizar(datos.nombre(), datos.tipo(), datos.documento(), datos.celular(), datos.contacto(),
				request.activo() == null || request.activo());
		return ProveedorResponse.de(proveedor);
	}

	public ProveedorResponse alternarActivo(Integer id) {
		Proveedor proveedor = buscar(id);
		if (proveedor.isArchivado()) {
			throw new ReglaNegocioException("Un proveedor eliminado no puede reactivarse desde esta operación");
		}
		if (proveedor.isActivo()) {
			proveedor.desactivar();
		} else {
			proveedor.activar();
		}
		return ProveedorResponse.de(proveedor);
	}

	public ProveedorResponse archivar(Integer id) {
		Proveedor proveedor = buscar(id);
		if (proveedor.isArchivado()) {
			return ProveedorResponse.de(proveedor);
		}
		proveedor.archivar();
		return ProveedorResponse.de(proveedor);
	}

	public Proveedor exigirActivo(Integer id) {
		Proveedor proveedor = buscar(id);
		if (proveedor.isArchivado() || !proveedor.isActivo()) {
			throw new ReglaNegocioException("El proveedor no está activo para nuevas compras");
		}
		return proveedor;
	}

	public Proveedor buscar(Integer id) {
		return proveedores.findById(id)
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el proveedor"));
	}

	private DatosProveedor validar(ProveedorRequest request, Integer idActual) {
		String nombre = TextoDescriptivo.normalizar(request.nombreRazonSocial());
		if (nombre.isBlank()) {
			throw new ReglaNegocioException("Ingresa el nombre o razón social");
		}
		TipoDocumento tipo;
		try {
			tipo = TipoDocumento.valueOf(request.tipoDocumento().trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ReglaNegocioException("El tipo de documento debe ser RUC o DNI");
		}
		String documento = DocumentoPeru.exigirDocumento(tipo.name(), request.documento());
		boolean duplicado = idActual == null
				? proveedores.existsByDocumento(documento)
				: proveedores.existsByDocumentoAndIdNot(documento, idActual);
		if (duplicado) {
			throw new ConflictoException("Ya existe un proveedor con ese documento");
		}
		return new DatosProveedor(nombre, tipo, documento, DocumentoPeru.celularOpcional(request.celular()),
				nuloSiVacio(TextoDescriptivo.normalizar(request.personaContacto())));
	}

	private static String nuloSiVacio(String valor) {
		return valor == null || valor.isBlank() ? null : valor;
	}

	private record DatosProveedor(String nombre, TipoDocumento tipo, String documento, String celular,
			String contacto) {
	}
}
