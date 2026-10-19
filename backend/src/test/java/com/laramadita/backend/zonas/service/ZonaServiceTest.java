package com.laramadita.backend.zonas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.zonas.api.dto.ZonaRequest;
import com.laramadita.backend.zonas.api.dto.ZonaResponse;
import com.laramadita.backend.zonas.model.Zona;
import com.laramadita.backend.zonas.repository.MesaRepository;
import com.laramadita.backend.zonas.repository.ZonaRepository;
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
class ZonaServiceTest {

	@Mock
	ZonaRepository zonas;
	@Mock
	MesaRepository mesas;

	ZonaService service;

	@BeforeEach
	void preparar() {
		service = new ZonaService(zonas, mesas);
		when(zonas.save(any(Zona.class))).thenAnswer(inv -> {
			Zona z = inv.getArgument(0);
			if (z.getId() == null) {
				ReflectionTestUtils.setField(z, "id", 4);
			}
			return z;
		});
		when(zonas.findByNombreIgnoreCase(any())).thenReturn(Optional.empty());
	}

	@Test
	void crearZonaConMesasNormalizaNombre() {
		ZonaResponse r = service.crear(new ZonaRequest("  salón PRINCIPAL  ", 8, 1, 1));
		assertThat(r.nombre()).isEqualTo("Salón Principal");
		assertThat(r.cantidadMesas()).isEqualTo(8);
		assertThat(r.cajas()).isEqualTo(1);
		assertThat(r.uso()).isEqualTo("SIN_USO");
	}

	@Test
	void rechazaNombreDuplicado() {
		Zona existente = new Zona("Terraza", 1, 1);
		ReflectionTestUtils.setField(existente, "id", 2);
		when(zonas.findByNombreIgnoreCase("Terraza")).thenReturn(Optional.of(existente));
		assertThatThrownBy(() -> service.crear(new ZonaRequest("TERRAZA", 3, 0, 0)))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void eliminarZonaSinOperacion() {
		Zona zona = new Zona("Terraza", 0, 0);
		zona.sincronizarMesas(2);
		ReflectionTestUtils.setField(zona, "id", 2);
		when(zonas.findWithMesasById(2)).thenReturn(Optional.of(zona));
		service.eliminar(2);
		verify(zonas).delete(zona);
	}
}
