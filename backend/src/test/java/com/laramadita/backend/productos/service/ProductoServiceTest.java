package com.laramadita.backend.productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.model.UnidadMedida;
import com.laramadita.backend.productos.api.dto.ComponenteRequest;
import com.laramadita.backend.productos.api.dto.ProductoRequest;
import com.laramadita.backend.productos.api.dto.ProductoResponse;
import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.RecetaDetalle;
import com.laramadita.backend.productos.repository.ProductoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
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
class ProductoServiceTest {

	@Mock
	ProductoRepository productos;
	@Mock
	CoherenciaCartaService coherenciaCarta;

	ProductoService service;

	@BeforeEach
	void preparar() {
		service = new ProductoService(productos, coherenciaCarta);
		when(productos.save(any(Producto.class))).thenAnswer(inv -> {
			Producto p = inv.getArgument(0);
			if (p.getId() == null) {
				ReflectionTestUtils.setField(p, "id", 10);
			}
			return p;
		});
		when(productos.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
	}

	@Test
	void crearNormalizaNombreYGuardaAmbito() {
		ProductoResponse r = service.crear(new ProductoRequest("  lomo SALTADO  ", "PLATO", "RESTAURANTE",
				new BigDecimal("22"), List.of()));

		ArgumentCaptor<Producto> guardado = ArgumentCaptor.forClass(Producto.class);
		verify(productos).save(guardado.capture());
		assertThat(guardado.getValue().getNombre()).isEqualTo("Lomo Saltado");
		assertThat(guardado.getValue().getAmbito()).isEqualTo(AmbitoProducto.RESTAURANTE);
		assertThat(guardado.getValue().getCategoria()).isEqualTo(CategoriaProducto.PLATO_EJECUTIVO);
		assertThat(r.precio()).isEqualByComparingTo("22");
		assertThat(r.activo()).isTrue();
	}

	@Test
	void rechazaNombreDuplicado() {
		Producto existente = new Producto("Lomo Saltado", CategoriaProducto.PLATO, AmbitoProducto.RESTAURANTE,
				new BigDecimal("22"));
		ReflectionTestUtils.setField(existente, "id", 3);
		when(productos.findByNombreIgnoreCase("Lomo Saltado")).thenReturn(Optional.of(existente));

		assertThatThrownBy(() -> service.crear(new ProductoRequest("LOMO SALTADO", "PLATO", "RESTAURANTE",
				BigDecimal.ZERO, List.of())))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("Lomo Saltado");
	}

	@Test
	void rechazaPrecioNegativo() {
		assertThatThrownBy(() -> service.crear(new ProductoRequest("Ceviche", "PLATO", "RESTAURANTE",
				new BigDecimal("-1"), List.of())))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("negativo");
	}

	@Test
	void inactivarNoBorraElProducto() {
		Producto existente = new Producto("Ceviche", CategoriaProducto.ENTRADA, AmbitoProducto.RESTAURANTE,
				BigDecimal.ZERO);
		ReflectionTestUtils.setField(existente, "id", 8);
		when(productos.findById(8)).thenReturn(Optional.of(existente));

		ProductoResponse r = service.cambiarActivo(8, false);
		assertThat(r.activo()).isFalse();
		assertThat(existente.isActivo()).isFalse();
	}

	@Test
	void comboRechazaAutoreferencia() {
		Producto padre = new Producto("Combo Familiar", CategoriaProducto.COMBO, AmbitoProducto.POLLERIA,
				new BigDecimal("62"));
		ReflectionTestUtils.setField(padre, "id", 5);
		when(productos.findWithRelacionesById(5)).thenReturn(Optional.of(padre));
		when(productos.findById(5)).thenReturn(Optional.of(padre));

		assertThatThrownBy(() -> service.actualizar(5, new ProductoRequest("Combo Familiar", "COMBO", "POLLERIA",
				new BigDecimal("62"), List.of(new ComponenteRequest(5, BigDecimal.ONE)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("sí mismo");
	}

	@Test
	void comboRechazaComponenteDuplicado() {
		Producto pollo = pieza(11, "Pollo Entero");
		when(productos.findById(11)).thenReturn(Optional.of(pollo));

		assertThatThrownBy(() -> service.crear(new ProductoRequest("Combo Familiar", "COMBO", "POLLERIA",
				new BigDecimal("62"),
				List.of(new ComponenteRequest(11, BigDecimal.ONE), new ComponenteRequest(11, BigDecimal.ONE)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("repitas");
	}

	@Test
	void comboRechazaAnidarCombos() {
		Producto hijo = pieza(12, "Mostrito");
		hijo.agregarComponente(new com.laramadita.backend.productos.model.ProductoComponente(pieza(11, "1/4 Pollo"),
				BigDecimal.ONE));
		when(productos.findById(12)).thenReturn(Optional.of(hijo));

		assertThatThrownBy(() -> service.crear(new ProductoRequest("Combo Doble", "COMBO", "POLLERIA",
				new BigDecimal("30"), List.of(new ComponenteRequest(12, BigDecimal.ONE)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("anidar");
	}

	@Test
	void editarComboConservaElComponente() {
		Producto combo = new Producto("Combo Familiar", CategoriaProducto.COMBO, AmbitoProducto.POLLERIA,
				new BigDecimal("62"));
		ReflectionTestUtils.setField(combo, "id", 5);
		Producto pollo = pieza(11, "Pollo Entero");
		var pieza = new com.laramadita.backend.productos.model.ProductoComponente(pollo, BigDecimal.ONE);
		ReflectionTestUtils.setField(pieza, "id", 3);
		combo.agregarComponente(pieza);
		when(productos.findWithRelacionesById(5)).thenReturn(Optional.of(combo));
		when(productos.findById(11)).thenReturn(Optional.of(pollo));
		when(productos.findById(15)).thenReturn(Optional.of(pieza(15, "Chaufa")));

		ProductoResponse r = service.actualizar(5, new ProductoRequest("Combo Familiar", "COMBO", "POLLERIA",
				new BigDecimal("65"), List.of(
						new ComponenteRequest(11, new BigDecimal("2")),
						new ComponenteRequest(15, BigDecimal.ONE))));

		assertThat(combo.getComponentes()).hasSize(2);
		assertThat(combo.getComponentes().iterator().next().getId()).isEqualTo(3);
		assertThat(combo.getComponentes().iterator().next().getCantidad()).isEqualByComparingTo("2");
		assertThat(r.precio()).isEqualByComparingTo("65");
		verify(productos).flush();
	}

	@Test
	void comboGuardaVariosComponentes() {
		when(productos.findById(11)).thenReturn(Optional.of(pieza(11, "Pollo Entero")));
		when(productos.findById(15)).thenReturn(Optional.of(pieza(15, "Chaufa")));
		when(productos.findById(20)).thenReturn(Optional.of(pieza(20, "Gaseosa 1 L")));

		ProductoResponse r = service.crear(new ProductoRequest("Combo Familiar", "COMBO", "POLLERIA",
				new BigDecimal("62"),
				List.of(
						new ComponenteRequest(11, BigDecimal.ONE),
						new ComponenteRequest(15, BigDecimal.ONE),
						new ComponenteRequest(20, BigDecimal.ONE))));

		assertThat(r.compuesto()).isTrue();
		assertThat(r.componentes()).hasSize(3);
	}

	@Test
	void comboRechazaCantidadInvalida() {
		assertThatThrownBy(() -> new com.laramadita.backend.productos.model.ProductoComponente(
				pieza(11, "Pollo Entero"), BigDecimal.ZERO))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("mayor a 0");
	}

	@Test
	void comboExigeAlMenosDosRecetas() {
		when(productos.findById(11)).thenReturn(Optional.of(pieza(11, "Pollo Entero")));
		assertThatThrownBy(() -> service.crear(new ProductoRequest("Combo Familiar", "COMBO", "POLLERIA",
				new BigDecimal("62"), List.of(new ComponenteRequest(11, BigDecimal.ONE)))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("dos recetas");
	}

	private static Producto pieza(int id, String nombre) {
		Producto p = new Producto(nombre, CategoriaProducto.POLLO_A_LA_BRASA, AmbitoProducto.POLLERIA, BigDecimal.TEN);
		ReflectionTestUtils.setField(p, "id", id);
		Insumo insumo = new Insumo("Pollo", UnidadMedida.KILOGRAMO, BigDecimal.TEN, BigDecimal.ONE);
		ReflectionTestUtils.setField(insumo, "id", id + 100);
		p.agregarLineaReceta(new RecetaDetalle(insumo, BigDecimal.ONE));
		return p;
	}
}
