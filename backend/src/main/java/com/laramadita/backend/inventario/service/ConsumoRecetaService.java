package com.laramadita.backend.inventario.service;

import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.service.InsumoService;
import com.laramadita.backend.inventario.model.InventarioMovimiento;
import com.laramadita.backend.inventario.repository.InventarioMovimientoRepository;
import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.PedidoDetalle;
import com.laramadita.backend.pedidos.model.ProductosPreparados;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.ProductoComponente;
import com.laramadita.backend.productos.model.RecetaDetalle;
import com.laramadita.backend.usuarios.model.Usuario;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M04: al marcar un producto como Preparado en Cocina se descuentan los
 * insumos de su receta (SALIDA_RECETA, un movimiento por insumo). Los combos
 * descuentan la receta de cada componente.
 *
 * Corre dentro de la transacción de Cocina: nunca lanza por falta de stock
 * (Cocina ya preparó el plato). Si falta stock, descuenta hasta cero y deja
 * anotado el faltante en el motivo.
 */
@Service
public class ConsumoRecetaService {

	private static final int ESCALA = 3;
	private static final int MAX_NIVEL_COMBO = 5;

	private final EntityManager em;
	private final InsumoService insumos;
	private final InventarioMovimientoRepository movimientos;

	public ConsumoRecetaService(EntityManager em, InsumoService insumos, InventarioMovimientoRepository movimientos) {
		this.em = em;
		this.insumos = insumos;
		this.movimientos = movimientos;
	}

	@EventListener
	@Transactional
	public void alPreparar(ProductosPreparados evento) {
		Map<PedidoDetalle, Map<Integer, BigDecimal>> requeridos = new LinkedHashMap<>();
		TreeSet<Integer> insumoIds = new TreeSet<>();
		for (Integer detalleId : evento.detalleIds()) {
			PedidoDetalle linea = em.find(PedidoDetalle.class, detalleId);
			if (linea == null) {
				continue;
			}
			Map<Integer, BigDecimal> porInsumo = new TreeMap<>();
			acumular(linea.getProducto(), BigDecimal.valueOf(linea.getCantidad()), porInsumo, 0);
			if (!porInsumo.isEmpty()) {
				requeridos.put(linea, porInsumo);
				insumoIds.addAll(porInsumo.keySet());
			}
		}
		if (requeridos.isEmpty()) {
			return;
		}
		// Bloqueo en orden de id: dos cocineros a la vez no se cruzan.
		Map<Integer, Insumo> bloqueados = new LinkedHashMap<>();
		insumoIds.forEach(id -> bloqueados.put(id, insumos.bloquear(id)));
		Usuario cocinero = em.getReference(Usuario.class, evento.usuarioId());

		requeridos.forEach((linea, porInsumo) -> {
			Atencion atencion = linea.getPedido().getAtencion();
			String referencia = "Receta: " + linea.getCantidad() + " × " + linea.nombre() + " · "
					+ atencion.identificacion();
			porInsumo.forEach((insumoId, requerido) -> {
				Insumo insumo = bloqueados.get(insumoId);
				BigDecimal anterior = insumo.getStockActual();
				BigDecimal descontado = insumo.consumir(requerido);
				if (descontado.signum() <= 0) {
					return;
				}
				BigDecimal faltante = requerido.subtract(descontado);
				String motivo = faltante.signum() > 0
						? referencia + " · faltó " + faltante.stripTrailingZeros().toPlainString() + " sin stock"
						: referencia;
				movimientos.save(InventarioMovimiento.salidaReceta(
						insumo,
						cocinero,
						linea.getId(),
						atencion.getTurno().getUnidad().getId(),
						atencion.getTurno().getId(),
						descontado,
						anterior,
						insumo.getStockActual(),
						motivo.length() > 300 ? motivo.substring(0, 300) : motivo));
			});
		});
	}

	/** Receta propia más la de cada componente del combo, multiplicada por la cantidad. */
	private static void acumular(Producto producto, BigDecimal factor, Map<Integer, BigDecimal> destino, int nivel) {
		if (nivel > MAX_NIVEL_COMBO) {
			return;
		}
		for (RecetaDetalle linea : producto.getReceta()) {
			BigDecimal cantidad = linea.getCantidadRequerida().multiply(factor).setScale(ESCALA, RoundingMode.HALF_UP);
			if (cantidad.signum() > 0) {
				destino.merge(linea.getInsumo().getId(), cantidad, BigDecimal::add);
			}
		}
		for (ProductoComponente pieza : producto.getComponentes()) {
			acumular(pieza.getComponente(), factor.multiply(pieza.getCantidad()), destino, nivel + 1);
		}
	}
}
