package com.laramadita.backend.compras.service;

import static com.laramadita.backend.usuarios.model.UsuariosFixture.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.compras.api.dto.CompraDetalleRequest;
import com.laramadita.backend.compras.api.dto.CompraRequest;
import com.laramadita.backend.compras.api.dto.CompraResponse;
import com.laramadita.backend.compras.api.dto.RecepcionDetalleRequest;
import com.laramadita.backend.compras.api.dto.RecepcionRequest;
import com.laramadita.backend.compras.model.Compra;
import com.laramadita.backend.compras.model.CompraDetalle;
import com.laramadita.backend.compras.model.EstadoCompra;
import com.laramadita.backend.compras.repository.CompraRepository;
import com.laramadita.backend.compras.repository.RecepcionRepository;
import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.insumos.model.UnidadMedida;
import com.laramadita.backend.insumos.service.InsumoService;
import com.laramadita.backend.inventario.model.InventarioMovimiento;
import com.laramadita.backend.inventario.repository.InventarioMovimientoRepository;
import com.laramadita.backend.proveedores.model.Proveedor;
import com.laramadita.backend.proveedores.model.TipoDocumento;
import com.laramadita.backend.proveedores.service.ProveedorService;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.repository.UsuarioRepository;
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
class CompraServiceTest {

	@Mock
	CompraRepository compras;
	@Mock
	RecepcionRepository recepciones;
	@Mock
	InventarioMovimientoRepository movimientos;
	@Mock
	ProveedorService proveedores;
	@Mock
	InsumoService insumos;
	@Mock
	UsuarioRepository usuarios;

	CompraService service;
	Proveedor proveedor;
	Insumo pollo;
	Insumo cerdo;
	Usuario supervisor;

	@BeforeEach
	void preparar() {
		service = new CompraService(compras, recepciones, movimientos, proveedores, insumos, usuarios);
		proveedor = new Proveedor("Avicola San Pedro", TipoDocumento.RUC, "20123456789", "987654321", "Juan Perez");
		ReflectionTestUtils.setField(proveedor, "id", 2);
		pollo = new Insumo("Pollo Entero", UnidadMedida.UNIDAD, new BigDecimal("10"), new BigDecimal("3"));
		ReflectionTestUtils.setField(pollo, "id", 1);
		cerdo = new Insumo("Cerdo", UnidadMedida.KILOGRAMO, new BigDecimal("5"), new BigDecimal("2"));
		ReflectionTestUtils.setField(cerdo, "id", 3);
		supervisor = usuario(9, "supervisor", RolNombre.SUPERVISOR, true);
		when(proveedores.exigirActivo(2)).thenReturn(proveedor);
		when(proveedores.buscar(2)).thenReturn(proveedor);
		when(insumos.buscarActivo(1)).thenReturn(pollo);
		when(insumos.buscarActivo(3)).thenReturn(cerdo);
		when(insumos.bloquear(1)).thenReturn(pollo);
		when(usuarios.findByUsername("supervisor")).thenReturn(Optional.of(supervisor));
		when(compras.siguienteNumeroCodigo()).thenReturn(1);
		when(compras.save(any(Compra.class))).thenAnswer(inv -> {
			Compra compra = inv.getArgument(0);
			if (compra.getId() == null) {
				ReflectionTestUtils.setField(compra, "id", 10);
			}
			int linea = 11;
			for (CompraDetalle detalle : compra.getDetalles()) {
				if (detalle.getId() == null) {
					ReflectionTestUtils.setField(detalle, "id", linea++);
				}
			}
			return compra;
		});
		when(recepciones.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(movimientos.save(any())).thenAnswer(inv -> inv.getArgument(0));
	}

	@Test
	void creaCompraConVariosDetallesYTotalCalculado() {
		CompraResponse r = service.crear(new CompraRequest(2, List.of(
				new CompraDetalleRequest(1, new BigDecimal("50"), new BigDecimal("10.00")),
				new CompraDetalleRequest(3, new BigDecimal("20"), new BigDecimal("12.50")))), "supervisor");

		assertThat(r.codigo()).isEqualTo("COMP-001");
		assertThat(r.estado()).isEqualTo("PENDIENTE");
		assertThat(r.detalles()).hasSize(2);
		assertThat(r.total()).isEqualByComparingTo("750.00");
		assertThat(r.registradoPor()).isEqualTo("supervisor");
	}

	@Test
	void editaSoloSiEstaPendienteSinRecepcion() {
		Compra compra = compraPendiente();
		when(compras.buscarCompleta(10)).thenReturn(Optional.of(compra));

		CompraResponse r = service.actualizar(10, new CompraRequest(2, List.of(
				new CompraDetalleRequest(1, new BigDecimal("40"), new BigDecimal("11.00")))));

		assertThat(r.detalles()).hasSize(1);
		assertThat(r.total()).isEqualByComparingTo("440.00");
	}

	@Test
	void impideEditarParcial() {
		Compra compra = compraPendiente();
		compra.getDetalles().getFirst().recibir(new BigDecimal("10"));
		compra.recalcularEstado();
		when(compras.buscarCompleta(10)).thenReturn(Optional.of(compra));

		assertThatThrownBy(() -> service.actualizar(10, new CompraRequest(2, List.of(
				new CompraDetalleRequest(1, new BigDecimal("40"), new BigDecimal("11.00"))))))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("pendiente");
		assertThat(compra.getEstado()).isEqualTo(EstadoCompra.PARCIAL);
	}

	@Test
	void impideEditarRecibida() {
		Compra compra = compraPendiente();
		compra.getDetalles().getFirst().recibir(new BigDecimal("50"));
		compra.recalcularEstado();
		when(compras.buscarCompleta(10)).thenReturn(Optional.of(compra));

		assertThatThrownBy(() -> service.actualizar(10, new CompraRequest(2, List.of(
				new CompraDetalleRequest(1, new BigDecimal("40"), new BigDecimal("11.00"))))))
				.isInstanceOf(ReglaNegocioException.class);
		assertThat(compra.getEstado()).isEqualTo(EstadoCompra.RECIBIDA);
	}

	@Test
	void cancelaPendienteSinRecepcion() {
		Compra compra = compraPendiente();
		when(compras.buscarCompleta(10)).thenReturn(Optional.of(compra));

		assertThat(service.cancelar(10).estado()).isEqualTo("CANCELADA");
	}

	@Test
	void impideCancelarConRecepcion() {
		Compra compra = compraPendiente();
		compra.getDetalles().getFirst().recibir(new BigDecimal("10"));
		compra.recalcularEstado();
		when(compras.buscarCompleta(10)).thenReturn(Optional.of(compra));

		assertThatThrownBy(() -> service.cancelar(10))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("pendiente");
	}

	@Test
	void recepcionParcialActualizaStockYEstado() {
		Compra compra = compraPendiente();
		when(compras.bloquearPorId(10)).thenReturn(Optional.of(compra));

		CompraResponse r = service.recibir(10, new RecepcionRequest(List.of(
				new RecepcionDetalleRequest(11, new BigDecimal("40")))), "supervisor");

		assertThat(r.estado()).isEqualTo("PARCIAL");
		assertThat(r.detalles().getFirst().cantidadRecibida()).isEqualByComparingTo("40");
		assertThat(pollo.getStockActual()).isEqualByComparingTo("50");
		ArgumentCaptor<InventarioMovimiento> mov = ArgumentCaptor.forClass(InventarioMovimiento.class);
		verify(movimientos).save(mov.capture());
		assertThat(mov.getValue().getStockAnterior()).isEqualByComparingTo("10");
		assertThat(mov.getValue().getStockNuevo()).isEqualByComparingTo("50");
		assertThat(mov.getValue().getCantidad()).isEqualByComparingTo("40");
		assertThat(mov.getValue().getMotivo()).isEqualTo("Recepción COMP-001");
		assertThat(mov.getValue().getUsuario().getUsername()).isEqualTo("supervisor");
	}

	@Test
	void segundaRecepcionCompletaLaCompra() {
		Compra compra = compraPendiente();
		compra.getDetalles().getFirst().recibir(new BigDecimal("40"));
		pollo.incrementarStock(new BigDecimal("40"));
		compra.recalcularEstado();
		when(compras.bloquearPorId(10)).thenReturn(Optional.of(compra));

		CompraResponse r = service.recibir(10, new RecepcionRequest(List.of(
				new RecepcionDetalleRequest(11, new BigDecimal("10")))), "supervisor");

		assertThat(r.estado()).isEqualTo("RECIBIDA");
		assertThat(pollo.getStockActual()).isEqualByComparingTo("60");
	}

	@Test
	void recepcionCompletaDeUnaSolaVez() {
		Compra compra = compraPendiente();
		when(compras.bloquearPorId(10)).thenReturn(Optional.of(compra));

		CompraResponse r = service.recibir(10, new RecepcionRequest(List.of(
				new RecepcionDetalleRequest(11, new BigDecimal("50")))), "supervisor");

		assertThat(r.estado()).isEqualTo("RECIBIDA");
		assertThat(pollo.getStockActual()).isEqualByComparingTo("60");
	}

	@Test
	void impideSobreRecepcion() {
		Compra compra = compraPendiente();
		compra.getDetalles().getFirst().recibir(new BigDecimal("40"));
		compra.recalcularEstado();
		when(compras.bloquearPorId(10)).thenReturn(Optional.of(compra));

		assertThatThrownBy(() -> service.recibir(10, new RecepcionRequest(List.of(
				new RecepcionDetalleRequest(11, new BigDecimal("11")))), "supervisor"))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("pendientes");
	}

	@Test
	void rollbackSiFallaElMovimiento() {
		Compra compra = compraPendiente();
		when(compras.bloquearPorId(10)).thenReturn(Optional.of(compra));
		when(movimientos.save(any())).thenThrow(new IllegalStateException("fallo forzado"));

		assertThatThrownBy(() -> service.recibir(10, new RecepcionRequest(List.of(
				new RecepcionDetalleRequest(11, new BigDecimal("40")))), "supervisor"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("fallo forzado");
		verify(recepciones, never()).save(any());
	}

	private Compra compraPendiente() {
		Compra compra = new Compra("COMP-001", proveedor, supervisor);
		ReflectionTestUtils.setField(compra, "id", 10);
		CompraDetalle detalle = new CompraDetalle(pollo, new BigDecimal("50"), new BigDecimal("10.00"));
		ReflectionTestUtils.setField(detalle, "id", 11);
		compra.agregarDetalle(detalle);
		return compra;
	}
}
