package com.laramadita.backend.ventas.service;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import java.sql.Types;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Periodo y unidad de negocio de una consulta de ventas (M11/M12).
 *
 * Las fechas se eligen en días de Lima y se convierten a UTC, que es como se
 * guardan en la base. Una venta es una atención CERRADA con al menos un pago
 * CONFIRMADO (las liberadas sin consumo no cuentan); su unidad es la del
 * turno en que se abrió.
 *
 * Las ventas se cuentan por <b>día operativo</b>: la fecha de apertura del
 * turno. Así una venta de Pollería cobrada a las 00:30 queda en el día de su
 * turno y cuadra con la caja de ese turno (la hora pico usa la hora real del cierre).
 *
 * @param unidad {@code null} = consolidado (Restaurante + Pollería)
 */
public record FiltroVentas(LocalDate desde, LocalDate hasta, NombreUnidad unidad) {

	public static final ZoneId LIMA = ZoneId.of("America/Lima");
	static final long DIAS_MAXIMOS = 366;

	/**
	 * Ventas del filtro como CTE {@code ventas(atencion_id, fecha_fin,
	 * fecha_turno, tipo_atencion, mesa_id, mesero_id, unidad)}. Usa los
	 * parámetros de {@link #parametros()}.
	 */
	public static final String CTE_VENTAS = """
			WITH ventas AS (
			    SELECT a.atencion_id, a.fecha_fin, t.fecha_apertura AS fecha_turno,
			           a.tipo_atencion, a.mesa_id, a.mesero_id, un.nombre AS unidad
			    FROM dbo.atenciones a
			    JOIN dbo.turnos t ON t.turno_id = a.turno_id
			    JOIN dbo.unidades_negocio un ON un.unidad_negocio_id = t.unidad_negocio_id
			    WHERE a.estado = N'CERRADA'
			      AND t.fecha_apertura >= :desde AND t.fecha_apertura < :hasta
			      AND (:unidad IS NULL OR un.nombre = :unidad)
			      AND EXISTS (SELECT 1 FROM dbo.pagos pg
			                  WHERE pg.atencion_id = a.atencion_id AND pg.estado = N'CONFIRMADO')
			)
			""";

	public FiltroVentas {
		if (desde.isAfter(hasta)) {
			throw new ReglaNegocioException("La fecha inicial no puede ser posterior a la final");
		}
		if (ChronoUnit.DAYS.between(desde, hasta) >= DIAS_MAXIMOS) {
			throw new ReglaNegocioException("El rango de fechas no puede superar un año");
		}
	}

	/** Sin fechas se consulta el día de hoy en Lima; sin unidad, el consolidado. */
	public static FiltroVentas de(String desde, String hasta, String unidad, Clock clock) {
		LocalDate hoy = LocalDate.now(clock.withZone(LIMA));
		LocalDate inicio = fecha(desde, hoy);
		LocalDate fin = fecha(hasta, inicio.isAfter(hoy) ? inicio : hoy);
		NombreUnidad nombre = null;
		if (unidad != null && !unidad.isBlank() && !"CONSOLIDADO".equalsIgnoreCase(unidad.trim())) {
			nombre = NombreUnidad.desde(unidad)
					.orElseThrow(() -> new ReglaNegocioException("La unidad debe ser RESTAURANTE, POLLERIA o CONSOLIDADO"));
		}
		return new FiltroVentas(inicio, fin, nombre);
	}

	public MapSqlParameterSource parametros() {
		return new MapSqlParameterSource()
				.addValue("desde", utc(desde.atStartOfDay()))
				.addValue("hasta", utc(hasta.plusDays(1).atStartOfDay()))
				.addValue("unidad", unidad == null ? null : unidad.name(), Types.NVARCHAR)
				.addValue("offsetHoras", offsetHoras());
	}

	/** Diferencia de Lima con UTC en horas (Perú no usa horario de verano: −5). */
	public int offsetHoras() {
		return LIMA.getRules().getOffset(desde.atStartOfDay(LIMA).toInstant()).getTotalSeconds() / 3600;
	}

	public String etiquetaUnidad() {
		return unidad == null ? "Consolidado" : unidad.etiqueta();
	}

	private static LocalDateTime utc(LocalDateTime limaLocal) {
		return limaLocal.atZone(LIMA).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	private static LocalDate fecha(String texto, LocalDate porDefecto) {
		if (texto == null || texto.isBlank()) {
			return porDefecto;
		}
		try {
			return LocalDate.parse(texto.trim());
		} catch (DateTimeParseException e) {
			throw new ReglaNegocioException("Fecha no válida: " + texto + " (formato AAAA-MM-DD)");
		}
	}
}
