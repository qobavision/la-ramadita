package com.laramadita.backend.reportes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.unidadesnegocio.model.NombreUnidad;
import com.laramadita.backend.ventas.service.FiltroVentas;
import com.laramadita.backend.ventas.service.VentaConsultaService;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Ejecuta las consultas SQL de M11/M12 contra la base local (como
 * {@code BackendApplicationTests}) para validar sintaxis y columnas de V1.
 * Solo lectura: no inserta ni modifica datos.
 */
@SpringBootTest
class ConsultasVentasTest {

	@Autowired
	private ReporteService reportes;

	@Autowired
	private VentaConsultaService ventas;

	@Test
	void resumenYListadoSeEjecutanParaConsolidadoYPorUnidad() {
		LocalDate hoy = LocalDate.now(FiltroVentas.LIMA);
		FiltroVentas mes = new FiltroVentas(hoy.minusDays(30), hoy, null);

		var resumen = reportes.resumen(mes);
		assertThat(resumen.porDia()).hasSize(31);
		assertThat(resumen.porUnidad()).hasSize(2);
		assertThat(resumen.porMetodo()).isNotEmpty();
		assertThat(resumen.ventas()).isGreaterThanOrEqualTo(0);

		assertThat(ventas.listar(mes)).isNotNull();
		assertThat(reportes.resumen(new FiltroVentas(hoy, hoy, NombreUnidad.POLLERIA)).porUnidad()).hasSize(1);
		assertThatThrownBy(() -> ventas.detalle(-1)).isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void filtroRechazaRangosInvalidos() {
		LocalDate hoy = LocalDate.now(FiltroVentas.LIMA);
		assertThatThrownBy(() -> new FiltroVentas(hoy, hoy.minusDays(1), null)).isInstanceOf(ReglaNegocioException.class);
		assertThatThrownBy(() -> new FiltroVentas(hoy.minusYears(2), hoy, null)).isInstanceOf(ReglaNegocioException.class);
	}
}
