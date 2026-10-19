package com.laramadita.backend.proveedores.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.laramadita.backend.proveedores.api.dto.ProveedorRequest;
import com.laramadita.backend.proveedores.api.dto.ProveedorResponse;
import com.laramadita.backend.proveedores.model.Proveedor;
import com.laramadita.backend.proveedores.model.TipoDocumento;
import com.laramadita.backend.proveedores.repository.ProveedorRepository;
import com.laramadita.backend.shared.error.ConflictoException;
import com.laramadita.backend.shared.error.ReglaNegocioException;
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
class ProveedorServiceTest {

	@Mock
	ProveedorRepository proveedores;

	ProveedorService service;

	@BeforeEach
	void preparar() {
		service = new ProveedorService(proveedores);
		when(proveedores.save(any(Proveedor.class))).thenAnswer(inv -> {
			Proveedor proveedor = inv.getArgument(0);
			if (proveedor.getId() == null) {
				ReflectionTestUtils.setField(proveedor, "id", 1);
			}
			return proveedor;
		});
	}

	@Test
	void aceptaRucYNormalizaNombre() {
		ProveedorResponse r = service.crear(new ProveedorRequest("avícola SAN PEDRO", "RUC", "20123456789",
				"987654321", "juan PEREZ", true));

		ArgumentCaptor<Proveedor> guardado = ArgumentCaptor.forClass(Proveedor.class);
		verify(proveedores).save(guardado.capture());
		assertThat(guardado.getValue().getNombreRazonSocial()).isEqualTo("Avícola San Pedro");
		assertThat(guardado.getValue().getPersonaContacto()).isEqualTo("Juan Perez");
		assertThat(guardado.getValue().getTipoDocumento()).isEqualTo(TipoDocumento.RUC);
		assertThat(guardado.getValue().getDocumento()).isEqualTo("20123456789");
		assertThat(r.celular()).isEqualTo("987654321");
	}

	@Test
	void rechazaRucInvalido() {
		assertThatThrownBy(() -> service.crear(new ProveedorRequest("Avicola", "RUC", "2012345678", null, null, true)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("11");
	}

	@Test
	void aceptaDniDeOchoDigitos() {
		ProveedorResponse r = service.crear(new ProveedorRequest("Maria Lopez", "DNI", "45678901", null, null, true));
		assertThat(r.tipoDocumento()).isEqualTo("DNI");
		assertThat(r.documento()).isEqualTo("45678901");
	}

	@Test
	void rechazaDniInvalido() {
		assertThatThrownBy(() -> service.crear(new ProveedorRequest("Maria", "DNI", "4567890", null, null, true)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("8");
	}

	@Test
	void rechazaCelularIncompleto() {
		assertThatThrownBy(() -> service.crear(
				new ProveedorRequest("Avicola", "RUC", "20123456789", "98765432", null, true)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("9");
	}

	@Test
	void rechazaDocumentoDuplicado() {
		when(proveedores.existsByDocumento("20123456789")).thenReturn(true);
		assertThatThrownBy(() -> service.crear(
				new ProveedorRequest("Otro", "RUC", "20123456789", null, null, true)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("documento");
	}

	@Test
	void proveedorInactivoNoDisponibleParaCompra() {
		Proveedor inactivo = new Proveedor("Avicola San Pedro", TipoDocumento.RUC, "20123456789", null, null);
		inactivo.desactivar();
		ReflectionTestUtils.setField(inactivo, "id", 8);
		when(proveedores.findById(8)).thenReturn(java.util.Optional.of(inactivo));

		assertThatThrownBy(() -> service.exigirActivo(8))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("no está activo");
	}

	@Test
	void archivarEsBajaLogicaYNoBorra() {
		Proveedor proveedor = new Proveedor("Avicola San Pedro", TipoDocumento.RUC, "20123456789", null, null);
		ReflectionTestUtils.setField(proveedor, "id", 3);
		when(proveedores.findById(3)).thenReturn(java.util.Optional.of(proveedor));

		ProveedorResponse r = service.archivar(3);

		assertThat(r.archivado()).isTrue();
		assertThat(r.activo()).isFalse();
		assertThat(proveedor.isArchivado()).isTrue();
	}

	@Test
	void proveedorArchivadoNoSeUsaEnComprasNiSeEdita() {
		Proveedor archivado = new Proveedor("Avicola San Pedro", TipoDocumento.RUC, "20123456789", null, null);
		archivado.archivar();
		ReflectionTestUtils.setField(archivado, "id", 4);
		when(proveedores.findById(4)).thenReturn(java.util.Optional.of(archivado));

		assertThatThrownBy(() -> service.exigirActivo(4))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("no está activo");
		assertThatThrownBy(() -> service.actualizar(4, new ProveedorRequest("Avicola San Pedro", "RUC",
				"20123456789", null, null, true)))
				.isInstanceOf(ReglaNegocioException.class)
				.hasMessageContaining("eliminado");
	}
}
