package com.laramadita.backend.insumos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.insumos.api.dto.InsumoActualizarRequest;
import com.laramadita.backend.insumos.api.dto.InsumoRequest;
import com.laramadita.backend.insumos.api.dto.InsumoResponse;
import com.laramadita.backend.insumos.model.EstadoStock;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.model.UnidadMedida;
import com.laramadita.backend.insumos.repository.InsumoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InsumoServiceTest {

	@Mock
	InsumoRepository insumos;

	InsumoService service;

	@BeforeEach
	void preparar() {
		service = new InsumoService(insumos);
		when(insumos.save(any(Insumo.class))).thenAnswer(inv -> {
			Insumo insumo = inv.getArgument(0);
			if (insumo.getId() == null) {
				ReflectionTestUtils.setField(insumo, "id", 1);
			}
			return insumo;
		});
		when(insumos.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
	}

	@Test
	void altaNormalizaNombreYCalculaEstadoNormal() {
		InsumoResponse r = service.crear(new InsumoRequest("  POLLO ENTERO  ", "UNIDAD",
				new BigDecimal("10"), new BigDecimal("3")));

		ArgumentCaptor<Insumo> guardado = ArgumentCaptor.forClass(Insumo.class);
		verify(insumos).save(guardado.capture());
		assertThat(guardado.getValue().getNombre()).isEqualTo("Pollo Entero");
		assertThat(guardado.getValue().getUnidadMedida()).isEqualTo(UnidadMedida.UNIDAD);
		assertThat(r.estado()).isEqualTo(EstadoStock.NORMAL);
		assertThat(r.stockActual()).isEqualByComparingTo("10");
	}

	@Test
	void rechazaNombreDuplicadoTrasNormalizar() {
		Insumo existente = new Insumo("Pollo", UnidadMedida.UNIDAD, BigDecimal.ZERO, BigDecimal.ONE);
		ReflectionTestUtils.setField(existente, "id", 9);
		when(insumos.findByNombreIgnoreCase("Pollo")).thenReturn(Optional.of(existente));

		assertThatThrownBy(() -> service.crear(new InsumoRequest("POLLO", "UNIDAD",
				BigDecimal.ZERO, BigDecimal.ONE)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("Pollo");
	}

	@Test
	void rechazaStockNegativo() {
		assertThatThrownBy(() -> service.crear(new InsumoRequest("Papa", "KILOGRAMO",
				new BigDecimal("-1"), BigDecimal.ONE)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("negativo");
	}

	@Test
	void estadoStockBajoCuandoNoSuperaElMinimo() {
		InsumoResponse r = service.crear(new InsumoRequest("Aceite", "LITRO",
				new BigDecimal("2"), new BigDecimal("3")));
		assertThat(r.estado()).isEqualTo(EstadoStock.STOCK_BAJO);
	}

	@Test
	void estadoAgotadoCuandoStockEsCero() {
		InsumoResponse r = service.crear(new InsumoRequest("Sal", "KILOGRAMO",
				BigDecimal.ZERO, new BigDecimal("1")));
		assertThat(r.estado()).isEqualTo(EstadoStock.AGOTADO);
	}

	@Test
	void editarNoCambiaElStock() {
		Insumo existente = new Insumo("Pollo Entero", UnidadMedida.UNIDAD, new BigDecimal("10"),
				new BigDecimal("3"));
		ReflectionTestUtils.setField(existente, "id", 4);
		when(insumos.findById(4)).thenReturn(Optional.of(existente));

		InsumoResponse r = service.actualizar(4,
				new InsumoActualizarRequest("pollo entero", "UNIDAD", new BigDecimal("5")));

		assertThat(r.stockActual()).isEqualByComparingTo("10");
		assertThat(r.stockMinimo()).isEqualByComparingTo("5");
		assertThat(r.nombre()).isEqualTo("Pollo Entero");
	}

	@Test
	void eliminaFisicamenteSiNoTieneReferencias() {
		Insumo insumo = insumoActivo(7, "Papa Huyaro");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));

		assertThat(service.eliminar(7)).isEmpty();
		verify(insumos).delete(insumo);
	}

	@Test
	void desactivaSiTieneMovimientoHistorico() {
		Insumo insumo = insumoActivo(7, "Pollo Entero");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));
		when(insumos.existeEnMovimientos(7)).thenReturn(1);

		InsumoResponse r = service.eliminar(7).orElseThrow();

		assertThat(r.activo()).isFalse();
		assertThat(insumo.isActivo()).isFalse();
		verify(insumos, never()).delete(any());
	}

	@Test
	void desactivaSiEstuvoEnCompraRecibidaSinDependenciaActiva() {
		Insumo insumo = insumoActivo(7, "Cerdo");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));
		when(insumos.existeEnAlgunaCompra(7)).thenReturn(1);

		assertThat(service.eliminar(7).orElseThrow().activo()).isFalse();
		verify(insumos, never()).delete(any());
	}

	@Test
	void desactivaSiEstuvoEnCompraParcialSinPendiente() {
		Insumo insumo = insumoActivo(7, "Aceite");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));
		when(insumos.existeEnAlgunaCompra(7)).thenReturn(1);
		when(insumos.existeEnCompraPendiente(7)).thenReturn(0);

		assertThat(service.eliminar(7).orElseThrow().activo()).isFalse();
		verify(insumos, never()).delete(any());
	}

	@Test
	void bloqueaSiEstaEnCompraPendiente() {
		Insumo insumo = insumoActivo(7, "Pollo Entero");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));
		when(insumos.existeEnCompraPendiente(7)).thenReturn(1);

		assertThatThrownBy(() -> service.eliminar(7))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("compra pendiente");
		verify(insumos, never()).delete(any());
		assertThat(insumo.isActivo()).isTrue();
	}

	@Test
	void bloqueaSiEstaEnRecetaDeProductoActivo() {
		Insumo insumo = insumoActivo(7, "Papa Amarilla");
		when(insumos.findById(7)).thenReturn(Optional.of(insumo));
		when(insumos.existeEnRecetaDeProductoActivo(7)).thenReturn(1);

		assertThatThrownBy(() -> service.eliminar(7))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("receta activa");
		verify(insumos, never()).delete(any());
		assertThat(insumo.isActivo()).isTrue();
	}

	@Test
	void listaSoloActivosPorDefecto() {
		Insumo activo = insumoActivo(1, "Papa Amarilla");
		when(insumos.findByActivoTrueOrderByNombreAsc()).thenReturn(List.of(activo));

		List<InsumoResponse> lista = service.listar(null, null);

		assertThat(lista).extracting(InsumoResponse::nombre).containsExactly("Papa Amarilla");
		assertThat(lista).allMatch(InsumoResponse::activo);
	}

	@Test
	void listaSoloInactivos() {
		Insumo inactivo = insumoActivo(2, "Papa Huayro");
		inactivo.desactivar();
		when(insumos.findByActivoFalseOrderByNombreAsc()).thenReturn(List.of(inactivo));

		List<InsumoResponse> lista = service.listar("", "INACTIVOS");

		assertThat(lista).extracting(InsumoResponse::nombre).containsExactly("Papa Huayro");
		assertThat(lista).allMatch(r -> !r.activo());
	}

	@Test
	void listaTodosIncluyeActivosEInactivos() {
		Insumo activo = insumoActivo(1, "Aceite");
		Insumo inactivo = insumoActivo(2, "Papa Huayro");
		inactivo.desactivar();
		when(insumos.findAllByOrderByNombreAsc()).thenReturn(List.of(activo, inactivo));

		List<InsumoResponse> lista = service.listar(null, "TODOS");

		assertThat(lista).extracting(InsumoResponse::nombre).containsExactly("Aceite", "Papa Huayro");
		assertThat(lista).extracting(InsumoResponse::activo).containsExactly(true, false);
	}

	@Test
	void rechazaEstadoDeListadoInvalido() {
		assertThatThrownBy(() -> service.listar(null, "ARCHIVADOS"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("ACTIVOS");
	}

	@Test
	void buscaCombinandoTextoYFiltroInactivos() {
		Insumo inactivo = insumoActivo(2, "Papa Huayro");
		inactivo.desactivar();
		when(insumos.findByActivoFalseAndNombreContainingIgnoreCaseOrderByNombreAsc("papa"))
				.thenReturn(List.of(inactivo));

		List<InsumoResponse> lista = service.listar("papa", "INACTIVOS");

		assertThat(lista).extracting(InsumoResponse::nombre).containsExactly("Papa Huayro");
		assertThat(lista).allMatch(r -> !r.activo());
	}

	@Test
	void reactivaInsumoInactivoConservandoElMismoId() {
		Insumo inactivo = insumoActivo(8, "Papa Huayro");
		inactivo.desactivar();
		when(insumos.findById(8)).thenReturn(Optional.of(inactivo));

		InsumoResponse r = service.reactivar(8);

		assertThat(r.id()).isEqualTo(8);
		assertThat(r.nombre()).isEqualTo("Papa Huayro");
		assertThat(r.activo()).isTrue();
		assertThat(inactivo.isActivo()).isTrue();
	}

	@Test
	void noReactivaUnInsumoYaActivo() {
		Insumo activo = insumoActivo(8, "Papa Huayro");
		when(insumos.findById(8)).thenReturn(Optional.of(activo));

		assertThatThrownBy(() -> service.reactivar(8))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("ya está activo");
		assertThat(activo.isActivo()).isTrue();
	}

	@Test
	void noReactivaUnInsumoInexistente() {
		when(insumos.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.reactivar(99))
				.isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void noCreaDuplicadoSiElNombrePerteneceAUnInsumoDesactivado() {
		Insumo inactivo = insumoActivo(8, "Papa Huayro");
		inactivo.desactivar();
		when(insumos.findByNombreIgnoreCase("Papa Huayro")).thenReturn(Optional.of(inactivo));

		assertThatThrownBy(() -> service.crear(new InsumoRequest("papa huayro", "KILOGRAMO",
				BigDecimal.ZERO, BigDecimal.ONE)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("desactivado");
		verify(insumos, never()).save(any());
	}

	private static Insumo insumoActivo(Integer id, String nombre) {
		Insumo insumo = new Insumo(nombre, UnidadMedida.UNIDAD, new BigDecimal("10"), new BigDecimal("3"));
		ReflectionTestUtils.setField(insumo, "id", id);
		return insumo;
	}
}
