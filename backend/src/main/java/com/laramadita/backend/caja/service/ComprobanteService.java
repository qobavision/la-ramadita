package com.laramadita.backend.caja.service;

import com.laramadita.backend.caja.api.dto.ComprobanteResponse;
import com.laramadita.backend.caja.model.Comprobante;
import com.laramadita.backend.caja.model.EstadoComprobante;
import com.laramadita.backend.caja.model.EstadoPago;
import com.laramadita.backend.caja.model.TipoComprobante;
import com.laramadita.backend.caja.repository.ComprobanteRepository;
import com.laramadita.backend.caja.repository.PagoRepository;
import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.usuarios.model.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M10 Comprobantes simulados (sin SUNAT): boleta simple emitida al quedar la
 * cuenta pagada. Serie por unidad de negocio: B001 Restaurante, B002 Pollería.
 */
@Service
@Transactional
public class ComprobanteService {

	private final ComprobanteRepository comprobantes;
	private final PagoRepository pagos;
	private final AccesoCaja acceso;

	public ComprobanteService(ComprobanteRepository comprobantes, PagoRepository pagos, AccesoCaja acceso) {
		this.comprobantes = comprobantes;
		this.pagos = pagos;
		this.acceso = acceso;
	}

	/** Una sola boleta emitida por atención (índice UX_comprobante_emitido_atencion de V15). */
	public Comprobante emitirBoleta(Atencion atencion, Usuario cajero) {
		return comprobantes.findFirstByAtencion_IdAndEstado(atencion.getId(), EstadoComprobante.EMITIDO)
				.orElseGet(() -> {
					String serie = serieDe(atencion.getTurno().getUnidad().getNombre());
					int numero = comprobantes.ultimoNumero(TipoComprobante.BOLETA.name(), serie) + 1;
					return comprobantes.save(Comprobante.boleta(atencion, cajero, serie, numero));
				});
	}

	/** Al anular un pago de una cuenta pagada, su boleta queda ANULADA (se conserva). */
	public void anularDe(Integer atencionId) {
		comprobantes.findFirstByAtencion_IdAndEstado(atencionId, EstadoComprobante.EMITIDO)
				.ifPresent(Comprobante::anular);
	}

	@Transactional(readOnly = true)
	public ComprobanteResponse boleta(Integer atencionId, String username) {
		acceso.exigirConsultar(username);
		Comprobante comprobante = comprobantes.findFirstByAtencion_IdAndEstado(atencionId, EstadoComprobante.EMITIDO)
				.orElseThrow(() -> new RecursoNoEncontradoException("La cuenta todavía no tiene boleta emitida"));
		return ComprobanteResponse.de(comprobante,
				pagos.findByAtencion_IdAndEstadoOrderByFechaAsc(atencionId, EstadoPago.CONFIRMADO));
	}

	private static String serieDe(NombreUnidad unidad) {
		return unidad == NombreUnidad.POLLERIA ? "B002" : "B001";
	}
}
