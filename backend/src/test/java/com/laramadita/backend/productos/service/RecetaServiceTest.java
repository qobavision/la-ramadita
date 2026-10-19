package com.laramadita.backend.productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.model.UnidadMedida;
import com.laramadita.backend.insumos.service.InsumoService;
import com.laramadita.backend.productos.api.dto.RecetaInsumoRequest;
import com.laramadita.backend.productos.api.dto.RecetaRequest;
import com.laramadita.backend.productos.api.dto.RecetaResponse;
import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.repository.ProductoRepository;
import com.laramadita.backend.shared.error.RecursoNoEncontradoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecetaServiceTest {

	@Mock
	ProductoRepository productos;
	@Mock
	InsumoService insumos;
	@Mock
	CoherenciaCartaService coherenciaCarta;

	ProductoService productoService;
	RecetaService service;

	@BeforeEach
	void preparar() {
		productoService = new ProductoService(productos, coherenciaCarta);
		service = new RecetaService(productos, productoService, insumos, coherenciaCarta);
		when(productos.save(any(Producto.class))).thenAnswer(inv -> {
			Producto p = inv.getArgument(0);
			if (p.getId() == null) {
				ReflectionTestUtils.setField(p, "id", 21);
			}
			return p;
		});
		when(productos.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
		when(insumos.buscarActivo(1)).thenReturn(insumo(1, "Arroz", UnidadMedida.KILOGRAMO));
		when(insumos.buscarActivo(2)).thenReturn(insumo(2, "Pollo", UnidadMedida.KILOGRAMO));
	}

	@Test
	void crearRecetaConVariosInsumosYCantidadDecimal() {
		RecetaResponse r = service.crear(new RecetaRequest("arroz con POLLO", "SEGUNDO", "RESTAURANTE",
				BigDecimal.ZERO,
				List.of(
						new RecetaInsumoRequest(1, new BigDecimal("0.180")),
						new RecetaInsumoRequest(2, new BigDecimal("0.125")))));

		assertThat(r.nombre()).isEqualTo("Arroz Con Pollo");
		assertThat(r.productoId()).isEqualTo(21);
		assertThat(r.insumos()).hasSize(2);
		assertThat(r.insumos().get(1).cantidad()).isEqualByComparingTo("0.125");
	}

	@Test
	void creaRecetaDeEnsalada() {
		RecetaResponse r = service.crear(new RecetaRequest("ensalada criolla", "ENSALADA", "AMBOS",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(1, new BigDecimal("0.080")))));
		assertThat(r.categoria()).isEqualTo("ENSALADA");
		assertThat(r.ambito()).isEqualTo("AMBOS");
	}

	@Test
	void creaRecetaDePlatoEjecutivoCuandoVienePlato() {
		RecetaResponse r = service.crear(new RecetaRequest("lomo saltado", "PLATO", "RESTAURANTE",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(2, new BigDecimal("0.150")))));
		assertThat(r.categoria()).isEqualTo("PLATO_EJECUTIVO");
	}

	@Test
	void rechazaComboComoReceta() {
		assertThatThrownBy(() -> service.crear(new RecetaRequest("Combo Familiar", "COMBO", "POLLERIA",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(1, BigDecimal.ONE)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("entrada");
	}

	@Test
	void rechazaInsumoDuplicado() {
		assertThatThrownBy(() -> service.crear(new RecetaRequest("Arroz Con Pollo", "SEGUNDO", "RESTAURANTE",
				BigDecimal.ZERO,
				List.of(
						new RecetaInsumoRequest(1, new BigDecimal("0.2")),
						new RecetaInsumoRequest(1, new BigDecimal("0.1"))))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("repitas");
	}

	@Test
	void rechazaInsumoInactivo() {
		when(insumos.buscarActivo(9)).thenThrow(new RecursoNoEncontradoException("No se encontró el insumo"));
		assertThatThrownBy(() -> service.crear(new RecetaRequest("Sopa De Casa", "ENTRADA", "RESTAURANTE",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(9, BigDecimal.ONE)))))
				.isInstanceOf(RecursoNoEncontradoException.class);
	}

	@Test
	void rechazaCantidadInvalida() {
		assertThatThrownBy(() -> service.crear(new RecetaRequest("Sopa De Casa", "ENTRADA", "RESTAURANTE",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(1, BigDecimal.ZERO)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("mayor a 0");
	}

	@Test
	void editarReemplazaInsumos() {
		Producto existente = new Producto("Arroz Con Pollo", CategoriaProducto.SEGUNDO, AmbitoProducto.RESTAURANTE,
				BigDecimal.ZERO);
		ReflectionTestUtils.setField(existente, "id", 21);
		existente.agregarLineaReceta(new com.laramadita.backend.productos.model.RecetaDetalle(
				insumo(1, "Arroz", UnidadMedida.KILOGRAMO), new BigDecimal("0.5")));
		when(productos.findConRecetaById(21)).thenReturn(Optional.of(existente));

		RecetaResponse r = service.actualizar(21, new RecetaRequest("Arroz Con Pollo", "SEGUNDO", "RESTAURANTE",
				BigDecimal.ZERO,
				List.of(
						new RecetaInsumoRequest(1, new BigDecimal("0.18")),
						new RecetaInsumoRequest(2, new BigDecimal("0.15")))));

		assertThat(r.insumos()).hasSize(2);
		assertThat(existente.getReceta()).hasSize(2);
		assertThat(existente.getReceta().iterator().next().getCantidadRequerida()).isEqualByComparingTo("0.18");
		verify(coherenciaCarta).retirarDeSeccionesIncompatibles(21, CategoriaProducto.SEGUNDO);
	}

	@Test
	void cambiarAPlatoEjecutivoLoSacaDelCriollo() {
		Producto existente = new Producto("Lomo Saltado De Carne", CategoriaProducto.SEGUNDO, AmbitoProducto.RESTAURANTE,
				BigDecimal.ZERO);
		ReflectionTestUtils.setField(existente, "id", 21);
		existente.agregarLineaReceta(new com.laramadita.backend.productos.model.RecetaDetalle(
				insumo(2, "Pollo", UnidadMedida.KILOGRAMO), new BigDecimal("0.15")));
		when(productos.findConRecetaById(21)).thenReturn(Optional.of(existente));

		service.actualizar(21, new RecetaRequest("Lomo Saltado De Carne", "PLATO_EJECUTIVO", "RESTAURANTE",
				BigDecimal.ZERO, List.of(new RecetaInsumoRequest(2, new BigDecimal("0.15")))));

		verify(coherenciaCarta).retirarDeSeccionesIncompatibles(21, CategoriaProducto.PLATO_EJECUTIVO);
	}

	private static Insumo insumo(int id, String nombre, UnidadMedida unidad) {
		Insumo i = new Insumo(nombre, unidad, BigDecimal.TEN, BigDecimal.ONE);
		ReflectionTestUtils.setField(i, "id", id);
		return i;
	}
}
