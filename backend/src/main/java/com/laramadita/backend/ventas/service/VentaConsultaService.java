package com.laramadita.backend.ventas.service;

import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.ventas.api.dto.MontoMetodoResponse;
import com.laramadita.backend.ventas.api.dto.VentaDetalleResponse;
import com.laramadita.backend.ventas.api.dto.VentaResumenResponse;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * M11 Ventas e historial. Consultas de solo lectura sobre las tablas de V1
 * (atenciones, pedidos, pagos, comprobantes): no modifican nada y no dependen
 * de las entidades de Caja. Los precios salen de {@code pedido_detalle}
 * (precio histórico), no del producto actual.
 */
@Service
@Transactional(readOnly = true)
public class VentaConsultaService {

	/** Columnas de una venta para {@link #venta(ResultSet)}; requiere la CTE o un FROM equivalente con alias v. */
	private static final String COLUMNAS_VENTA = """
			SELECT v.atencion_id, v.fecha_fin, v.tipo_atencion, v.unidad, m.numero AS mesa, z.nombre AS zona,
			       u.nombre AS mesero_nombre, u.apellido AS mesero_apellido,
			       (SELECT COALESCE(SUM(pd.cantidad * pd.precio_unitario), 0)
			          FROM dbo.pedido_detalle pd JOIN dbo.pedidos p ON p.pedido_id = pd.pedido_id
			         WHERE p.atencion_id = v.atencion_id AND p.estado IN (N'ENVIADO_COCINA', N'CERRADO')
			           AND pd.estado_item = N'ACTIVO') AS consumo,
			       (SELECT COALESCE(SUM(pg.monto), 0) FROM dbo.pagos pg
			         WHERE pg.atencion_id = v.atencion_id AND pg.estado = N'CONFIRMADO') AS cobrado
			FROM ventas v
			JOIN dbo.usuarios u ON u.usuario_id = v.mesero_id
			LEFT JOIN dbo.mesas m ON m.mesa_id = v.mesa_id
			LEFT JOIN dbo.zonas z ON z.zona_id = m.zona_id
			""";

	private final NamedParameterJdbcTemplate jdbc;

	public VentaConsultaService(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/** Ventas del periodo, la más reciente primero, con el desglose por método de pago. */
	public List<VentaResumenResponse> listar(FiltroVentas filtro) {
		MapSqlParameterSource parametros = filtro.parametros();
		Map<Integer, List<MontoMetodoResponse>> metodos = new HashMap<>();
		jdbc.query(FiltroVentas.CTE_VENTAS + """
				SELECT pg.atencion_id, mp.nombre AS metodo, SUM(pg.monto) AS monto
				FROM dbo.pagos pg
				JOIN ventas v ON v.atencion_id = pg.atencion_id
				JOIN dbo.metodos_pago mp ON mp.metodo_pago_id = pg.metodo_pago_id
				WHERE pg.estado = N'CONFIRMADO'
				GROUP BY pg.atencion_id, mp.nombre
				ORDER BY pg.atencion_id, SUM(pg.monto) DESC
				""", parametros, rs -> {
			metodos.computeIfAbsent(rs.getInt("atencion_id"), k -> new ArrayList<>())
					.add(new MontoMetodoResponse(rs.getString("metodo"), rs.getBigDecimal("monto")));
		});
		return jdbc.query(FiltroVentas.CTE_VENTAS + COLUMNAS_VENTA + " ORDER BY v.fecha_fin DESC", parametros,
				(rs, i) -> venta(rs, metodos.getOrDefault(rs.getInt("atencion_id"), List.of())));
	}

	/** Detalle completo de una venta para reconstruirla (productos, pagos, comprobantes). */
	public VentaDetalleResponse detalle(Integer atencionId) {
		MapSqlParameterSource parametros = new MapSqlParameterSource("id", atencionId);
		String cteUna = """
				WITH ventas AS (
				    SELECT a.atencion_id, a.fecha_fin, a.tipo_atencion, a.mesa_id, a.mesero_id, un.nombre AS unidad
				    FROM dbo.atenciones a
				    JOIN dbo.turnos t ON t.turno_id = a.turno_id
				    JOIN dbo.unidades_negocio un ON un.unidad_negocio_id = t.unidad_negocio_id
				    WHERE a.atencion_id = :id AND a.estado = N'CERRADA'
				)
				""";

		List<VentaDetalleResponse.Pago> pagos = jdbc.query("""
				SELECT mp.nombre AS metodo, pg.monto, pg.fecha_pago, pg.estado, u.nombre, u.apellido
				FROM dbo.pagos pg
				JOIN dbo.metodos_pago mp ON mp.metodo_pago_id = pg.metodo_pago_id
				JOIN dbo.usuarios u ON u.usuario_id = pg.usuario_id
				WHERE pg.atencion_id = :id
				ORDER BY pg.fecha_pago, pg.pago_id
				""", parametros, (rs, i) -> new VentaDetalleResponse.Pago(
				rs.getString("metodo"),
				rs.getBigDecimal("monto"),
				instante(rs, "fecha_pago"),
				nombre(rs.getString("nombre"), rs.getString("apellido")),
				rs.getString("estado")));

		List<MontoMetodoResponse> metodos = new ArrayList<>();
		pagos.stream().filter(p -> "CONFIRMADO".equals(p.estado())).forEach(p -> {
			for (int i = 0; i < metodos.size(); i++) {
				if (metodos.get(i).metodo().equals(p.metodo())) {
					metodos.set(i, new MontoMetodoResponse(p.metodo(), metodos.get(i).monto().add(p.monto())));
					return;
				}
			}
			metodos.add(new MontoMetodoResponse(p.metodo(), p.monto()));
		});

		VentaResumenResponse venta = jdbc.query(cteUna + COLUMNAS_VENTA, parametros, (rs, i) -> venta(rs, metodos))
				.stream()
				.findFirst()
				.orElseThrow(() -> new RecursoNoEncontradoException("No se encontró una venta cerrada con ese número"));

		List<VentaDetalleResponse.Producto> productos = jdbc.query("""
				SELECT COALESCE(pd.nombre_producto, pr.nombre) AS nombre, pd.cantidad, pd.precio_unitario, pd.observacion
				FROM dbo.pedido_detalle pd
				JOIN dbo.pedidos p ON p.pedido_id = pd.pedido_id
				JOIN dbo.productos pr ON pr.producto_id = pd.producto_id
				WHERE p.atencion_id = :id AND p.estado IN (N'ENVIADO_COCINA', N'CERRADO') AND pd.estado_item = N'ACTIVO'
				ORDER BY pd.pedido_detalle_id
				""", parametros, (rs, i) -> {
			BigDecimal precio = rs.getBigDecimal("precio_unitario");
			int cantidad = rs.getInt("cantidad");
			return new VentaDetalleResponse.Producto(rs.getString("nombre"), cantidad, precio,
					precio.multiply(BigDecimal.valueOf(cantidad)), rs.getString("observacion"));
		});

		List<VentaDetalleResponse.Comprobante> comprobantes = jdbc.query("""
				SELECT c.tipo, c.serie, c.numero, c.total, c.fecha_emision, c.nombre_cliente, c.estado
				FROM dbo.comprobantes c
				WHERE c.atencion_id = :id
				ORDER BY c.fecha_emision, c.comprobante_id
				""", parametros, (rs, i) -> new VentaDetalleResponse.Comprobante(
				rs.getString("tipo"),
				rs.getString("serie"),
				rs.getInt("numero"),
				rs.getBigDecimal("total"),
				instante(rs, "fecha_emision"),
				rs.getString("nombre_cliente"),
				rs.getString("estado")));

		return new VentaDetalleResponse(venta, productos, pagos, comprobantes);
	}

	private static VentaResumenResponse venta(ResultSet rs, List<MontoMetodoResponse> metodos) throws SQLException {
		int id = rs.getInt("atencion_id");
		boolean mesa = "MESA".equals(rs.getString("tipo_atencion"));
		return new VentaResumenResponse(
				id,
				instante(rs, "fecha_fin"),
				etiquetaUnidad(rs.getString("unidad")),
				rs.getString("tipo_atencion"),
				mesa ? "Mesa " + rs.getString("mesa") : "Para llevar #" + id,
				rs.getString("zona"),
				nombre(rs.getString("mesero_nombre"), rs.getString("mesero_apellido")),
				rs.getBigDecimal("consumo"),
				rs.getBigDecimal("cobrado"),
				metodos);
	}

	static String etiquetaUnidad(String unidad) {
		return "POLLERIA".equals(unidad) ? "Pollería" : "Restaurante";
	}

	static Instant instante(ResultSet rs, String columna) throws SQLException {
		LocalDateTime fecha = rs.getObject(columna, LocalDateTime.class);
		return fecha == null ? null : fecha.toInstant(ZoneOffset.UTC);
	}

	private static String nombre(String nombre, String apellido) {
		return apellido == null || apellido.isBlank() ? nombre : nombre + " " + apellido;
	}
}
