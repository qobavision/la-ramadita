package com.laramadita.backend.reportes.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resumen de M12 para el Dashboard y Reportes / BI. Todas las cifras salen
 * de ventas cobradas en el sistema (sin datos inventados).
 *
 * @param ticketPromedio total / ventas, o 0 si no hubo ventas
 * @param porDia         un punto por cada día del periodo (también los días sin ventas)
 * @param porHora        franjas de una hora (hora de Lima) con ventas
 */
public record ResumenVentasResponse(
		LocalDate desde,
		LocalDate hasta,
		String unidad,
		BigDecimal total,
		int ventas,
		BigDecimal ticketPromedio,
		List<Serie> porUnidad,
		List<Serie> porMetodo,
		List<Serie> porDia,
		List<Serie> porHora,
		List<ProductoRanking> topProductos,
		List<AlertaStock> alertasStock) {

	/** Punto de una serie: etiqueta (unidad, método, día AAAA-MM-DD u hora "13:00"), importe y número de ventas. */
	public record Serie(String clave, String etiqueta, BigDecimal total, int cantidad) {
	}

	public record ProductoRanking(String producto, int cantidad, BigDecimal importe) {
	}

	/** Insumo en stock bajo o agotado (M04). */
	public record AlertaStock(Integer insumoId, String insumo, BigDecimal stockActual, BigDecimal stockMinimo,
			String unidadMedida, String estado) {
	}
}
