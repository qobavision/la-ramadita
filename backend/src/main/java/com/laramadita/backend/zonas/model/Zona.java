package com.laramadita.backend.zonas.model;

import com.laramadita.backend.shared.error.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.Nationalized;

/** Zona o piso. Tabla {@code dbo.zonas} de V1, capacidad en V9. */
@Entity
@Table(name = "zonas")
public class Zona {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "zona_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 60, unique = true)
	private String nombre;

	@Nationalized
	@Column(name = "descripcion", length = 200)
	private String descripcion;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "cajas", nullable = false)
	private int cajas;

	@Column(name = "cocinas", nullable = false)
	private int cocinas;

	@OneToMany(mappedBy = "zona", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<Mesa> mesas = new ArrayList<>();

	protected Zona() {
	}

	public Zona(String nombre, int cajas, int cocinas) {
		this.nombre = nombre;
		this.cajas = exigirNoNegativo(cajas, "cajas");
		this.cocinas = exigirNoNegativo(cocinas, "cocinas");
		this.activo = true;
	}

	public void actualizar(String nombre, int cajas, int cocinas) {
		this.nombre = nombre;
		this.cajas = exigirNoNegativo(cajas, "cajas");
		this.cocinas = exigirNoNegativo(cocinas, "cocinas");
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		if (uso() == UsoZona.OPERACION_ACTIVA) {
			throw new ReglaNegocioException("No se puede inactivar: hay mesas ocupadas");
		}
		this.activo = false;
	}

	public void sincronizarMesas(int cantidad) {
		int objetivo = exigirNoNegativo(cantidad, "mesas");
		while (mesas.size() > objetivo) {
			Mesa ultima = mesas.getLast();
			if (!ultima.disponible()) {
				throw new ReglaNegocioException("No se pueden quitar mesas que están ocupadas");
			}
			mesas.removeLast();
		}
		while (mesas.size() < objetivo) {
			agregarMesa(new Mesa(String.valueOf(mesas.size() + 1)));
		}
	}

	public void agregarMesa(Mesa mesa) {
		mesa.asignarZona(this);
		this.mesas.add(mesa);
	}

	public UsoZona uso() {
		if (mesas.stream().anyMatch(m -> m.isActivo() && m.getEstado().enOperacion())) {
			return UsoZona.OPERACION_ACTIVA;
		}
		return UsoZona.SIN_USO;
	}

	public boolean sePuedeEliminar() {
		return uso() == UsoZona.SIN_USO;
	}

	private static int exigirNoNegativo(int valor, String campo) {
		if (valor < 0) {
			throw new ReglaNegocioException("La cantidad de " + campo + " no puede ser negativa");
		}
		return valor;
	}

	public Integer getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public boolean isActivo() {
		return activo;
	}

	public int getCajas() {
		return cajas;
	}

	public int getCocinas() {
		return cocinas;
	}

	public List<Mesa> getMesas() {
		return mesas;
	}
}
