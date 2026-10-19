package com.laramadita.backend.productos.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.annotations.Nationalized;

/** Carta nombrada y reutilizable. Tabla {@code dbo.cartas} de V1, extendida en V8. */
@Entity
@Table(name = "cartas")
public class Carta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "carta_id")
	private Integer id;

	@Column(name = "turno_id")
	private Integer turnoId;

	@Nationalized
	@Column(name = "nombre", nullable = false, length = 120)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "ambito", nullable = false, length = 20)
	private AmbitoCarta ambito;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoCarta estado = EstadoCarta.ACTIVA;

	@Column(name = "vigente", nullable = false)
	private boolean vigente;

	@OneToMany(mappedBy = "carta", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<CartaDetalle> detalles = new ArrayList<>();

	protected Carta() {
	}

	public Carta(String nombre, AmbitoCarta ambito) {
		this.nombre = nombre;
		this.ambito = ambito;
		this.estado = EstadoCarta.ACTIVA;
		this.vigente = false;
	}

	@PrePersist
	void alCrear() {
		if (fecha == null) {
			fecha = LocalDate.now();
		}
	}

	public void renombrar(String nombre) {
		this.nombre = nombre;
	}

	public void marcarVigente() {
		this.vigente = true;
	}

	public void quitarVigente() {
		this.vigente = false;
	}

	public void reemplazarDetalles(List<CartaDetalle> lineas) {
		this.detalles.clear();
		lineas.forEach(this::agregarDetalle);
	}

	public void retirarDetallesAusentes(Set<String> claves) {
		this.detalles.removeIf(detalle -> !claves.contains(claveDe(detalle)));
	}

	public void retirarDetallesIncompatibles() {
		this.detalles.removeIf(detalle -> !detalle.getSeccion().acepta(detalle.getProducto().getCategoria()));
	}

	public void actualizarOAgregarDetalles(List<CartaDetalle> nuevas) {
		Map<String, CartaDetalle> actuales = new LinkedHashMap<>();
		for (CartaDetalle detalle : detalles) {
			actuales.put(claveDe(detalle), detalle);
		}
		for (CartaDetalle nueva : nuevas) {
			CartaDetalle existente = actuales.get(claveDe(nueva));
			if (existente != null) {
				existente.actualizar(nueva.getPrecioDia(), nueva.getEstado());
			} else {
				agregarDetalle(nueva);
			}
		}
	}

	public void agregarDetalle(CartaDetalle detalle) {
		detalle.asignarCarta(this);
		this.detalles.add(detalle);
	}

	public static String claveDe(CartaDetalle detalle) {
		return detalle.getProducto().getId() + ":" + detalle.getSeccion().name();
	}

	public void quitarDetalle(CartaDetalle detalle) {
		this.detalles.remove(detalle);
	}

	public Integer getId() {
		return id;
	}

	public Integer getTurnoId() {
		return turnoId;
	}

	public String getNombre() {
		return nombre;
	}

	public AmbitoCarta getAmbito() {
		return ambito;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public EstadoCarta getEstado() {
		return estado;
	}

	public boolean isVigente() {
		return vigente;
	}

	public List<CartaDetalle> getDetalles() {
		return detalles;
	}
}
