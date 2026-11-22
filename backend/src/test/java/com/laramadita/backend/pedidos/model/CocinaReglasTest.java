package com.laramadita.backend.pedidos.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.Producto;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.RolNombre;
import com.laramadita.backend.usuarios.model.Usuario;
import com.laramadita.backend.usuarios.model.UsuariosFixture;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Reglas de M08 en el modelo: preparado (RN-12/13) y cambios después del envío (RN-06). */
class CocinaReglasTest {

	private final Usuario mesero = UsuariosFixture.usuario(1, "mesero", RolNombre.MESERO, true);
	private final Usuario cocinero = UsuariosFixture.usuario(2, "cocina", RolNombre.COCINA, true);

	private Atencion atencion;
	private Pedido pedido;
	private PedidoDetalle lomo;
	private PedidoDetalle gaseosa;

	@BeforeEach
	void enviarPedido() {
		atencion = Atencion.paraLlevar(null, mesero);
		atencion.agregar(producto(10, "Lomo saltado"), 2, new BigDecimal("25.00"), null, mesero);
		atencion.agregar(producto(11, "Inca Kola"), 1, new BigDecimal("5.00"), null, mesero);
		pedido = atencion.enviarBorrador();
		ReflectionTestUtils.setField(pedido, "id", 100);
		lomo = pedido.getDetalles().get(0);
		gaseosa = pedido.getDetalles().get(1);
		ReflectionTestUtils.setField(lomo, "id", 1);
		ReflectionTestUtils.setField(gaseosa, "id", 2);
	}

	@Test
	void cocinaMarcaPreparadoUnaSolaVezYQuedaTrazabilidad() {
		lomo.marcarPreparado(cocinero);

		assertThat(lomo.getEstadoCocina()).isEqualTo(EstadoCocina.PREPARADO);
		assertThat(lomo.getPreparadoPor()).isSameAs(cocinero);
		assertThat(lomo.getFechaPreparado()).isNotNull();
		assertThat(lomo.pendienteEnCocina()).isFalse();
		assertThatThrownBy(() -> lomo.marcarPreparado(cocinero)).isInstanceOf(ConflictoException.class);
	}

	@Test
	void cambioDespuesDelEnvioQuedaEnHistorialYSeAvisaACocina() {
		var cambio = atencion.actualizarDetalle(1, 3, "sin cebolla", mesero);

		assertThat(cambio).hasValueSatisfying(c -> {
			assertThat(c.tipo()).isEqualTo(CambioEnCocina.Tipo.MODIFICADO);
			assertThat(c.pedidoId()).isEqualTo(100);
			assertThat(c.detalle()).isEqualTo("Lomo saltado: 2 → 3, \"sin cebolla\"");
		});
		assertThat(lomo.getCantidad()).isEqualTo(3);
		assertThat(lomo.ultimaModificacion()).hasValueSatisfying(h -> {
			assertThat(h.getCantidadAnterior()).isEqualTo(2);
			assertThat(h.getCantidadNueva()).isEqualTo(3);
		});
		assertThat(atencion.totalEnviado()).isEqualByComparingTo("80.00");
	}

	@Test
	void productoPreparadoYaNoSePuedeModificarNiCancelar() {
		lomo.marcarPreparado(cocinero);

		assertThatThrownBy(() -> atencion.actualizarDetalle(1, 1, null, mesero))
				.isInstanceOf(ReglaNegocioException.class);
		assertThatThrownBy(() -> atencion.quitarDetalle(1, mesero)).isInstanceOf(ReglaNegocioException.class);
	}

	@Test
	void cancelarTodoDejaElPedidoCanceladoYConservaElHistorialAlLiberar() {
		assertThat(atencion.quitarDetalle(1, mesero)).hasValueSatisfying(
				c -> assertThat(c.tipo()).isEqualTo(CambioEnCocina.Tipo.MODIFICADO));
		assertThatThrownBy(() -> lomo.marcarPreparado(cocinero)).isInstanceOf(ReglaNegocioException.class);

		assertThat(atencion.quitarDetalle(2, mesero)).hasValueSatisfying(
				c -> assertThat(c.tipo()).isEqualTo(CambioEnCocina.Tipo.CANCELADO));
		assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
		assertThat(atencion.enviados()).isEmpty();

		atencion.liberarSinConsumo();
		assertThat(atencion.getEstado()).isEqualTo(EstadoAtencion.CERRADA);
		assertThat(atencion.getPedidos()).containsExactly(pedido);
	}

	private static Producto producto(int id, String nombre) {
		Producto p = new Producto(nombre, CategoriaProducto.PLATO, AmbitoProducto.AMBOS, new BigDecimal("10.00"));
		ReflectionTestUtils.setField(p, "id", id);
		return p;
	}
}
