package com.laramadita.backend.reportes.service;

import com.laramadita.backend.insumos.model.EstadoStock;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.repository.InsumoRepository;
import com.laramadita.backend.reportes.api.dto.ResumenVentasResponse;
import com.laramadita.backend.reportes.api.dto.ResumenVentasResponse.AlertaStock;
import com.laramadita.backend.reportes.api.dto.ResumenVentasResponse.ProductoRanking;
import com.laramadita.backend.reportes.api.dto.ResumenVentasResponse.Serie;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.ventas.service.FiltroVentas;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M12 Reportes y BI: indicadores agregados sobre las ventas cobradas del
 * periodo (ver {@link FiltroVentas}). Solo lectura.
 */
@Service
@Transactional(readOnly = true)
public class ReporteService {

	static final int TOP_PRODUCTOS = 10;

	/**
	 * Cobro de cada venta del filtro: CTE {@code cobros(atencion_id, unidad,
	 * fecha_lima, dia_operativo, cobrado)}. {@code fecha_lima} es la hora real del
	 * cierre (hora pico); {@code dia_operativo}, el día del turno (evolución por día).
	 */
	private static final String CTE_COBROS = FiltroVentas.CTE_VENTAS.stripTrailing() + """
			, cobros AS (
			    SELECT v.atencion_id, v.unidad, DATEADD(HOUR, :offsetHoras, v.fecha_fin) AS fecha_lima,
			           CAST(DATEADD(HOUR, :offsetHoras, v.fecha_turno) AS DATE) AS dia_operativo,
			           (SELECT SUM(pg.monto) FROM dbo.pagos pg
			             WHERE pg.atencion_id = v.atencion_id AND pg.estado = N'CONFIRMADO') AS cobrado
			    FROM ventas v
			)
			""";

	private final NamedParameterJdbcTemplate jdbc;
	private final InsumoRepository insumos;

	public ReporteService(NamedParameterJdbcTemplate jdbc, InsumoRepository insumos) {
		this.jdbc = jdbc;
		this.insumos = insumos;
	}

	public ResumenVentasResponse resumen(FiltroVentas filtro) {
		MapSqlParameterSource p = filtro.parametros();

		Map<String, Serie> unidades = new LinkedHashMap<>();
		for (NombreUnidad u : NombreUnidad.values()) {
			if (filtro.unidad() == null || filtro.unidad() == u) {
				unidades.put(u.name(), new Serie(u.name(), u.etiqueta(), BigDecimal.ZERO, 0));
			}
		}
		jdbc.query(CTE_COBROS + "SELECT unidad, COUNT(*) AS n, SUM(cobrado) AS total FROM cobros GROUP BY unidad", p,
				rs -> {
					String clave = rs.getString("unidad");
					BigDecimal importe = monto(rs.getBigDecimal("total"));
					int n = rs.getInt("n");
					unidades.computeIfPresent(clave, (k, s) -> new Serie(k, s.etiqueta(), importe, n));
				});
		List<Serie> porUnidad = List.copyOf(unidades.values());
		BigDecimal total = porUnidad.stream().map(Serie::total).reduce(BigDecimal.ZERO, BigDecimal::add);
		int ventas = porUnidad.stream().mapToInt(Serie::cantidad).sum();
		BigDecimal ticket = ventas == 0 ? BigDecimal.ZERO
				: total.divide(BigDecimal.valueOf(ventas), 2, RoundingMode.HALF_UP);

		return new ResumenVentasResponse(
				filtro.desde(),
				filtro.hasta(),
				filtro.etiquetaUnidad(),
				total,
				ventas,
				ticket,
				porUnidad,
				porMetodo(p),
				porDia(filtro, p),
				porHora(p),
				topProductos(p),
				alertasStock());
	}

	/** Todos los métodos activos, aunque no tengan cobros, para comparar. */
	private List<Serie> porMetodo(MapSqlParameterSource p) {
		Map<String, Serie> metodos = new LinkedHashMap<>();
		jdbc.query("SELECT nombre FROM dbo.metodos_pago WHERE activo = 1 ORDER BY metodo_pago_id", Map.of(),
				rs -> {
					String nombre = rs.getString("nombre");
					metodos.put(nombre, new Serie(nombre, etiquetaMetodo(nombre), BigDecimal.ZERO, 0));
				});
		jdbc.query(FiltroVentas.CTE_VENTAS + """
				SELECT mp.nombre, COUNT(DISTINCT pg.atencion_id) AS n, SUM(pg.monto) AS total
				FROM dbo.pagos pg
				JOIN ventas v ON v.atencion_id = pg.atencion_id
				JOIN dbo.metodos_pago mp ON mp.metodo_pago_id = pg.metodo_pago_id
				WHERE pg.estado = N'CONFIRMADO'
				GROUP BY mp.nombre
				""", p, rs -> {
			String nombre = rs.getString("nombre");
			metodos.put(nombre, new Serie(nombre, etiquetaMetodo(nombre), monto(rs.getBigDecimal("total")), rs.getInt("n")));
		});
		return List.copyOf(metodos.values());
	}

	private List<Serie> porDia(FiltroVentas filtro, MapSqlParameterSource p) {
		Map<LocalDate, Serie> dias = new LinkedHashMap<>();
		for (LocalDate d = filtro.desde(); !d.isAfter(filtro.hasta()); d = d.plusDays(1)) {
			dias.put(d, new Serie(d.toString(), d.toString(), BigDecimal.ZERO, 0));
		}
		jdbc.query(CTE_COBROS + """
				SELECT dia_operativo AS dia, COUNT(*) AS n, SUM(cobrado) AS total
				FROM cobros GROUP BY dia_operativo
				""", p, rs -> {
			LocalDate dia = rs.getObject("dia", LocalDate.class);
			BigDecimal importe = monto(rs.getBigDecimal("total"));
			int n = rs.getInt("n");
			dias.computeIfPresent(dia, (k, s) -> new Serie(s.clave(), s.etiqueta(), importe, n));
		});
		return List.copyOf(dias.values());
	}

	/** Franjas horarias entre la primera y la última hora con ventas (las intermedias en 0). */
	private List<Serie> porHora(MapSqlParameterSource p) {
		Map<Integer, Serie> horas = new LinkedHashMap<>();
		jdbc.query(CTE_COBROS + """
				SELECT DATEPART(HOUR, fecha_lima) AS hora, COUNT(*) AS n, SUM(cobrado) AS total
				FROM cobros GROUP BY DATEPART(HOUR, fecha_lima)
				""", p, rs -> {
			int hora = rs.getInt("hora");
			horas.put(hora, new Serie(String.valueOf(hora), etiquetaHora(hora), monto(rs.getBigDecimal("total")), rs.getInt("n")));
		});
		if (horas.isEmpty()) {
			return List.of();
		}
		int primera = horas.keySet().stream().min(Integer::compare).orElse(0);
		int ultima = horas.keySet().stream().max(Integer::compare).orElse(23);
		List<Serie> resultado = new ArrayList<>();
		for (int h = primera; h <= ultima; h++) {
			resultado.add(horas.getOrDefault(h, new Serie(String.valueOf(h), etiquetaHora(h), BigDecimal.ZERO, 0)));
		}
		return resultado;
	}

	private List<ProductoRanking> topProductos(MapSqlParameterSource p) {
		return jdbc.query(FiltroVentas.CTE_VENTAS + """
				SELECT TOP (%d) pr.nombre, SUM(pd.cantidad) AS cantidad, SUM(pd.cantidad * pd.precio_unitario) AS importe
				FROM dbo.pedido_detalle pd
				JOIN dbo.pedidos pe ON pe.pedido_id = pd.pedido_id
				JOIN ventas v ON v.atencion_id = pe.atencion_id
				JOIN dbo.productos pr ON pr.producto_id = pd.producto_id
				WHERE pe.estado IN (N'ENVIADO_COCINA', N'CERRADO') AND pd.estado_item = N'ACTIVO'
				GROUP BY pr.producto_id, pr.nombre
				ORDER BY SUM(pd.cantidad) DESC, SUM(pd.cantidad * pd.precio_unitario) DESC
				""".formatted(TOP_PRODUCTOS), p, (rs, i) -> new ProductoRanking(
				rs.getString("nombre"), rs.getInt("cantidad"), monto(rs.getBigDecimal("importe"))));
	}

	/** Usa la misma regla que Inventario ({@link EstadoStock}): agotado primero, luego stock bajo. */
	private List<AlertaStock> alertasStock() {
		return insumos.findByActivoTrueOrderByNombreAsc().stream()
				.filter(i -> i.estadoStock() != EstadoStock.NORMAL)
				.sorted(Comparator.comparing((Insumo i) -> i.estadoStock() != EstadoStock.AGOTADO))
				.map(i -> new AlertaStock(i.getId(), i.getNombre(), i.getStockActual(), i.getStockMinimo(),
						i.getUnidadMedida().name(), i.estadoStock().name()))
				.toList();
	}

	private static BigDecimal monto(BigDecimal valor) {
		return valor == null ? BigDecimal.ZERO : valor;
	}

	private static String etiquetaHora(int hora) {
		return String.format("%02d:00", hora);
	}

	private static String etiquetaMetodo(String nombre) {
		return nombre.isEmpty() ? nombre : nombre.charAt(0) + nombre.substring(1).toLowerCase();
	}
}
