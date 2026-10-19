package com.laramadita.backend.productos.model;

import com.laramadita.backend.insumos.model.Insumo;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Nationalized;

/** Producto vendible. Tabla {@code dbo.productos} de V1. */
@Entity
@Table(name = "productos")
public class Producto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "producto_id")
	private Integer id;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 120, unique = true)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "categoria", nullable = false, length = 60)
	private CategoriaProducto categoria;

	@Column(name = "precio", nullable = false, precision = 12, scale = 2)
	private BigDecimal precio;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "ambito", nullable = false, length = 20)
	private AmbitoProducto ambito;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<RecetaDetalle> receta = new LinkedHashSet<>();

	@BatchSize(size = 32)
	@OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProductoComponente> componentes = new LinkedHashSet<>();

	protected Producto() {
	}

	public Producto(String nombre, CategoriaProducto categoria, AmbitoProducto ambito, BigDecimal precio) {
		this.nombre = nombre;
		this.categoria = categoria;
		this.ambito = ambito;
		this.precio = exigirPrecio(precio);
		this.activo = true;
	}

	public void actualizar(String nombre, CategoriaProducto categoria, AmbitoProducto ambito, BigDecimal precio) {
		this.nombre = nombre;
		this.categoria = categoria;
		this.ambito = ambito;
		this.precio = exigirPrecio(precio);
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public void reemplazarReceta(List<RecetaDetalle> lineas) {
		this.receta.clear();
		lineas.forEach(this::agregarLineaReceta);
	}

	public void retirarInsumosAusentes(Set<Integer> insumoIds) {
		receta.removeIf(linea -> !insumoIds.contains(linea.getInsumo().getId()));
	}

	public void actualizarOAgregarReceta(List<RecetaDetalle> nuevas) {
		Map<Integer, RecetaDetalle> actuales = new LinkedHashMap<>();
		for (RecetaDetalle linea : receta) {
			actuales.put(linea.getInsumo().getId(), linea);
		}
		for (RecetaDetalle nueva : nuevas) {
			RecetaDetalle existente = actuales.get(nueva.getInsumo().getId());
			if (existente != null) {
				existente.cambiarCantidad(nueva.getCantidadRequerida());
			} else {
				agregarLineaReceta(nueva);
			}
		}
	}

	public void agregarLineaReceta(RecetaDetalle linea) {
		linea.asignarProducto(this);
		this.receta.add(linea);
	}

	public void reemplazarComponentes(List<ProductoComponente> piezas) {
		this.componentes.clear();
		piezas.forEach(this::agregarComponente);
	}

	public void retirarComponentesAusentes(Set<Integer> componenteIds) {
		componentes.removeIf(pieza -> !componenteIds.contains(pieza.getComponente().getId()));
	}

	public void actualizarOAgregarComponentes(List<ProductoComponente> nuevas) {
		Map<Integer, ProductoComponente> actuales = new LinkedHashMap<>();
		for (ProductoComponente pieza : componentes) {
			actuales.put(pieza.getComponente().getId(), pieza);
		}
		for (ProductoComponente nueva : nuevas) {
			ProductoComponente existente = actuales.get(nueva.getComponente().getId());
			if (existente != null) {
				existente.cambiarCantidad(nueva.getCantidad());
			} else {
				agregarComponente(nueva);
			}
		}
	}

	public void agregarComponente(ProductoComponente pieza) {
		pieza.asignarProducto(this);
		this.componentes.add(pieza);
	}

	public boolean tieneReceta() {
		return !receta.isEmpty();
	}

	public boolean esCompuesto() {
		return !componentes.isEmpty();
	}

	public static RecetaDetalle lineaReceta(Insumo insumo, BigDecimal cantidad) {
		return new RecetaDetalle(insumo, cantidad);
	}

	private static BigDecimal exigirPrecio(BigDecimal valor) {
		BigDecimal limpio = valor == null ? BigDecimal.ZERO : valor;
		if (limpio.compareTo(BigDecimal.ZERO) < 0) {
			throw new ReglaNegocioException("El precio no puede ser negativo");
		}
		return limpio;
	}

	public Integer getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public CategoriaProducto getCategoria() {
		return categoria;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public AmbitoProducto getAmbito() {
		return ambito;
	}

	public boolean isActivo() {
		return activo;
	}

	public Set<RecetaDetalle> getReceta() {
		return receta;
	}

	public Set<ProductoComponente> getComponentes() {
		return componentes;
	}
}
