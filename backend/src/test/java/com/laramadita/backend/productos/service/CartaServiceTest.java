package com.laramadita.backend.productos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.productos.api.dto.CartaDetalleRequest;
import com.laramadita.backend.productos.api.dto.CartaRequest;
import com.laramadita.backend.productos.api.dto.CartaResponse;
import com.laramadita.backend.productos.model.AmbitoCarta;
import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.Carta;
import com.laramadita.backend.productos.model.CartaDetalle;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.EstadoProductoCarta;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.productos.model.SeccionCarta;
import com.laramadita.backend.productos.repository.CartaRepository;
import com.laramadita.backend.productos.repository.ProductoRepository;
import com.laramadita.backend.shared.error.ConflictoException;
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
class CartaServiceTest {

	@Mock
	CartaRepository cartas;
	@Mock
	ProductoRepository productos;
	@Mock
	CoherenciaCartaService coherenciaCarta;

	CartaService service;

	@BeforeEach
	void preparar() {
		service = new CartaService(cartas, new ProductoService(productos, coherenciaCarta));
		when(cartas.save(any(Carta.class))).thenAnswer(inv -> {
			Carta c = inv.getArgument(0);
			if (c.getId() == null) {
				ReflectionTestUtils.setField(c, "id", 1);
			}
			int i = 1;
			for (CartaDetalle d : c.getDetalles()) {
				if (d.getId() == null) {
					ReflectionTestUtils.setField(d, "id", i++);
				}
			}
			return c;
		});
		when(cartas.existsByAmbitoAndNombreIgnoreCase(any(), any())).thenReturn(false);
		when(cartas.existsByAmbitoAndVigenteTrue(AmbitoCarta.RESTAURANTE)).thenReturn(false);
		when(cartas.existsByAmbitoAndVigenteTrue(AmbitoCarta.POLLERIA)).thenReturn(false);
	}

	@Test
	void crearCartaConNombreYUnidadYLaPrimeraQuedaVigente() {
		CartaResponse r = service.crear(new CartaRequest("carta LUNES", "RESTAURANTE", List.of()));
		assertThat(r.nombre()).isEqualTo("Carta Lunes");
		assertThat(r.ambito()).isEqualTo("RESTAURANTE");
		assertThat(r.vigente()).isTrue();
	}

	@Test
	void segundaCartaNoPisaLaVigente() {
		when(cartas.existsByAmbitoAndVigenteTrue(AmbitoCarta.RESTAURANTE)).thenReturn(true);
		CartaResponse r = service.crear(new CartaRequest("Carta Martes", "RESTAURANTE", List.of()));
		assertThat(r.vigente()).isFalse();
	}

	@Test
	void rechazaNombreDuplicadoEnLaMismaUnidad() {
		when(cartas.existsByAmbitoAndNombreIgnoreCase(AmbitoCarta.RESTAURANTE, "Carta Lunes")).thenReturn(true);
		assertThatThrownBy(() -> service.crear(new CartaRequest("CARTA LUNES", "RESTAURANTE", List.of())))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void menuCriolloGuardaEntradasSegundosYPrecioConjunto() {
		when(productos.findById(40)).thenReturn(Optional.of(producto(40, "Menú Criollo", CategoriaProducto.MENU, new BigDecimal("16"))));
		when(productos.findById(9)).thenReturn(Optional.of(producto(9, "Sopa De Casa", CategoriaProducto.ENTRADA, BigDecimal.ZERO)));
		when(productos.findById(3)).thenReturn(Optional.of(producto(3, "Arroz Con Pollo", CategoriaProducto.SEGUNDO, BigDecimal.ZERO)));

		CartaResponse r = service.crear(new CartaRequest("Carta Lunes", "RESTAURANTE", List.of(
				new CartaDetalleRequest(40, "MENU_CRIOLLO", new BigDecimal("16"), "DISPONIBLE"),
				new CartaDetalleRequest(9, "MENU_CRIOLLO_ENTRADA", null, "DISPONIBLE"),
				new CartaDetalleRequest(3, "MENU_CRIOLLO_SEGUNDO", null, "DISPONIBLE"))));

		assertThat(r.detalles()).hasSize(3);
		assertThat(r.detalles().stream().filter(d -> d.seccion().equals("MENU_CRIOLLO")).findFirst())
				.get()
				.extracting(d -> d.precioEfectivo())
				.isEqualTo(new BigDecimal("16"));
	}

	@Test
	void menuEjecutivoGuardaPlatosYPrecioConjunto() {
		when(productos.findById(41)).thenReturn(Optional.of(producto(41, "Menú Ejecutivo", CategoriaProducto.MENU, new BigDecimal("20"))));
		when(productos.findById(5)).thenReturn(Optional.of(producto(5, "Lomo Saltado", CategoriaProducto.PLATO_EJECUTIVO, BigDecimal.ZERO)));
		when(productos.findById(23)).thenReturn(Optional.of(producto(23, "Pechuga A La Plancha", CategoriaProducto.PLATO_EJECUTIVO, BigDecimal.ZERO)));

		CartaResponse r = service.crear(new CartaRequest("Carta Lunes", "RESTAURANTE", List.of(
				new CartaDetalleRequest(41, "MENU_EJECUTIVO", new BigDecimal("20"), "DISPONIBLE"),
				new CartaDetalleRequest(23, "MENU_EJECUTIVO", null, "DISPONIBLE"),
				new CartaDetalleRequest(5, "MENU_EJECUTIVO", null, "DISPONIBLE"))));

		assertThat(r.detalles()).hasSize(3);
		assertThat(r.detalles().stream().filter(d -> d.nombre().equals("Menú Ejecutivo")).findFirst())
				.get()
				.extracting(d -> d.precioEfectivo())
				.isEqualTo(new BigDecimal("20"));
		assertThat(r.detalles().stream().filter(d -> d.nombre().equals("Lomo Saltado")).findFirst())
				.get()
				.extracting(d -> d.precioDia())
				.isNull();
	}

	@Test
	void precioDiaNuloUsaPrecioBase() {
		when(productos.findById(5)).thenReturn(Optional.of(producto(5, "Lomo Saltado", CategoriaProducto.PLATO_EJECUTIVO, new BigDecimal("22"))));
		CartaResponse r = service.crear(new CartaRequest("Carta Lunes", "RESTAURANTE", List.of(
				new CartaDetalleRequest(5, "MENU_EJECUTIVO", null, "DISPONIBLE"))));
		assertThat(r.detalles().getFirst().precioDia()).isNull();
		assertThat(r.detalles().getFirst().precioEfectivo()).isEqualByComparingTo("22");
	}

	@Test
	void marcarAgotadoNoInactivaElProducto() {
		Producto lomo = producto(5, "Lomo Saltado", CategoriaProducto.PLATO, new BigDecimal("22"));
		when(productos.findById(5)).thenReturn(Optional.of(lomo));
		Carta carta = cartaConDetalle(lomo);
		when(cartas.findWithDetallesById(1)).thenReturn(Optional.of(carta));

		CartaResponse r = service.cambiarEstadoDetalle(1, 7, "AGOTADO");
		assertThat(r.detalles().getFirst().estado()).isEqualTo("AGOTADO");
		assertThat(lomo.isActivo()).isTrue();
	}

	@Test
	void quitarDeCartaConservaElProducto() {
		Producto arroz = producto(3, "Arroz Con Pollo", CategoriaProducto.SEGUNDO, BigDecimal.ZERO);
		when(productos.findById(3)).thenReturn(Optional.of(arroz));
		Carta carta = cartaConDetalle(arroz);
		when(cartas.findWithDetallesById(1)).thenReturn(Optional.of(carta));

		CartaResponse r = service.quitarDetalle(1, 7);
		assertThat(r.detalles()).isEmpty();
		assertThat(arroz.isActivo()).isTrue();
		assertThat(arroz.getNombre()).isEqualTo("Arroz Con Pollo");
	}

	@Test
	void rechazaSeccionDeOtraUnidad() {
		when(productos.findById(31)).thenReturn(Optional.of(producto(31, "1/4 Pollo Clásico",
				CategoriaProducto.POLLO_A_LA_BRASA, new BigDecimal("14"))));
		assertThatThrownBy(() -> service.crear(new CartaRequest("Carta Lunes", "RESTAURANTE", List.of(
				new CartaDetalleRequest(31, "BRASA", new BigDecimal("14"), "DISPONIBLE")))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("BRASA");
	}

	@Test
	void editarConservaLaLineaYPermiteAgregarOtra() {
		Producto coca = producto(8, "Coca Cola", CategoriaProducto.BEBIDA, new BigDecimal("4.5"));
		Producto agua = producto(9, "Agua Mineral", CategoriaProducto.BEBIDA, new BigDecimal("2.5"));
		when(productos.findById(8)).thenReturn(Optional.of(coca));
		when(productos.findById(9)).thenReturn(Optional.of(agua));
		Carta carta = new Carta("Carta Restaurante", AmbitoCarta.RESTAURANTE);
		ReflectionTestUtils.setField(carta, "id", 1);
		CartaDetalle detalle = new CartaDetalle(coca, SeccionCarta.BEBIDA, new BigDecimal("4.5"),
				EstadoProductoCarta.DISPONIBLE);
		ReflectionTestUtils.setField(detalle, "id", 7);
		carta.agregarDetalle(detalle);
		when(cartas.findWithDetallesById(1)).thenReturn(Optional.of(carta));

		CartaResponse r = service.actualizar(1, new CartaRequest("Carta Restaurante", "RESTAURANTE", List.of(
				new CartaDetalleRequest(8, "BEBIDA", new BigDecimal("5"), "DISPONIBLE"),
				new CartaDetalleRequest(9, "BEBIDA", new BigDecimal("2.5"), "DISPONIBLE"))));

		assertThat(carta.getDetalles()).hasSize(2);
		assertThat(carta.getDetalles().getFirst().getId()).isEqualTo(7);
		assertThat(carta.getDetalles().getFirst().getPrecioDia()).isEqualByComparingTo("5");
		assertThat(r.detalles()).extracting(d -> d.nombre())
				.containsExactlyInAnyOrder("Coca Cola", "Agua Mineral");
		verify(cartas).flush();
	}

	@Test
	void listarQuitaDeCriolloLoQueYaNoEsSegundo() {
		Producto lomo = producto(5, "Lomo Saltado De Carne", CategoriaProducto.PLATO_EJECUTIVO, new BigDecimal("22"));
		Carta carta = new Carta("Carta Restaurante", AmbitoCarta.RESTAURANTE);
		ReflectionTestUtils.setField(carta, "id", 1);
		CartaDetalle detalle = new CartaDetalle(lomo, SeccionCarta.MENU_CRIOLLO_SEGUNDO, null,
				EstadoProductoCarta.DISPONIBLE);
		ReflectionTestUtils.setField(detalle, "id", 7);
		carta.agregarDetalle(detalle);
		when(cartas.findByAmbitoOrderByNombreAsc(AmbitoCarta.RESTAURANTE)).thenReturn(List.of(carta));

		CartaResponse r = service.listar("RESTAURANTE").getFirst();
		assertThat(r.detalles()).isEmpty();
		assertThat(carta.getDetalles()).isEmpty();
	}

	@Test
	void marcarVigenteQuitaLaAnterior() {
		Carta carta = new Carta("Carta Martes", AmbitoCarta.RESTAURANTE);
		ReflectionTestUtils.setField(carta, "id", 2);
		when(cartas.findWithDetallesById(2)).thenReturn(Optional.of(carta));

		CartaResponse r = service.marcarVigente(2);
		verify(cartas).quitarVigenteDe(AmbitoCarta.RESTAURANTE);
		assertThat(r.vigente()).isTrue();
	}

	private static Producto producto(int id, String nombre, CategoriaProducto categoria, BigDecimal precio) {
		Producto p = new Producto(nombre, categoria, AmbitoProducto.RESTAURANTE, precio);
		ReflectionTestUtils.setField(p, "id", id);
		return p;
	}

	private static Carta cartaConDetalle(Producto producto) {
		Carta carta = new Carta("Carta Lunes", AmbitoCarta.RESTAURANTE);
		ReflectionTestUtils.setField(carta, "id", 1);
		CartaDetalle detalle = new CartaDetalle(producto, SeccionCarta.A_LA_CARTA, null, EstadoProductoCarta.DISPONIBLE);
		ReflectionTestUtils.setField(detalle, "id", 7);
		carta.agregarDetalle(detalle);
		return carta;
	}
}
