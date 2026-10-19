package com.laramadita.backend.inventario.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.laramadita.backend.compras.api.CompraController;
import com.laramadita.backend.insumos.api.InsumoController;
import com.laramadita.backend.productos.api.CartaController;
import com.laramadita.backend.productos.api.ProductoController;
import com.laramadita.backend.productos.api.RecetaController;
import com.laramadita.backend.proveedores.api.ProveedorController;
import com.laramadita.backend.turnos.api.TurnoController;
import com.laramadita.backend.zonas.api.ZonaController;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class SupervisionInventarioSeguridadTest {

	@Test
	void supervisorYAdministradorPuedenUsarLosCatalogos() {
		assertThat(anotacion(InsumoController.class))
				.contains("SUPERVISOR")
				.contains("ADMINISTRADOR");
		assertThat(anotacion(ProveedorController.class))
				.contains("SUPERVISOR")
				.contains("ADMINISTRADOR");
		assertThat(anotacion(CompraController.class))
				.contains("SUPERVISOR")
				.contains("ADMINISTRADOR");
		assertThat(anotacion(InventarioMovimientoController.class))
				.contains("SUPERVISOR")
				.contains("ADMINISTRADOR");
		assertThat(anotacion(ProductoController.class)).contains("SUPERVISOR").contains("ADMINISTRADOR");
		assertThat(anotacion(RecetaController.class)).contains("SUPERVISOR").contains("ADMINISTRADOR");
		assertThat(anotacion(CartaController.class)).contains("SUPERVISOR").contains("ADMINISTRADOR");
		assertThat(anotacion(ZonaController.class)).contains("SUPERVISOR").contains("ADMINISTRADOR");
		assertThat(anotacion(TurnoController.class)).contains("SUPERVISOR").contains("ADMINISTRADOR");
	}

	@Test
	void reactivarInsumoQuedaReservadoASupervisorYAdministrador() throws NoSuchMethodException {
		Method metodo = InsumoController.class.getMethod("reactivar", Integer.class);
		assertThat(metodo.getAnnotation(PreAuthorize.class)).isNull();
		assertThat(anotacion(InsumoController.class))
				.isEqualTo("hasAnyRole('ADMINISTRADOR','SUPERVISOR')")
				.doesNotContain("MESERO")
				.doesNotContain("COCINA")
				.doesNotContain("CAJA");
	}

	@Test
	void noAutorizaMeseroCocinaNiCaja() {
		String esperada = "hasAnyRole('ADMINISTRADOR','SUPERVISOR')";
		assertThat(anotacion(InsumoController.class)).isEqualTo(esperada);
		assertThat(anotacion(ProveedorController.class)).isEqualTo(esperada);
		assertThat(anotacion(CompraController.class)).isEqualTo(esperada);
		assertThat(anotacion(InventarioMovimientoController.class)).isEqualTo(esperada);
		assertThat(anotacion(ProductoController.class)).isEqualTo(esperada);
		assertThat(anotacion(RecetaController.class)).isEqualTo(esperada);
		assertThat(anotacion(CartaController.class)).isEqualTo(esperada);
		assertThat(anotacion(ZonaController.class)).isEqualTo(esperada);
		assertThat(anotacion(TurnoController.class)).isEqualTo(esperada);
		assertThat(esperada).doesNotContain("MESERO").doesNotContain("COCINA").doesNotContain("CAJA");
	}

	private static String anotacion(Class<?> tipo) {
		return tipo.getAnnotation(PreAuthorize.class).value();
	}
}
