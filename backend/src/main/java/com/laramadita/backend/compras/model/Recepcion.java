package com.laramadita.backend.compras.model;

import com.laramadita.backend.usuarios.model.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/** Confirmación de recepción. Tabla {@code dbo.recepciones} de V7. */
@Entity
@Table(name = "recepciones")
public class Recepcion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recepcion_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "compra_id", nullable = false)
	private Compra compra;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario recibidoPor;

	@Column(name = "fecha_recepcion", nullable = false, updatable = false)
	private LocalDateTime fechaRecepcion;

	@OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RecepcionDetalle> detalles = new ArrayList<>();

	protected Recepcion() {
	}

	public Recepcion(Compra compra, Usuario recibidoPor) {
		this.compra = compra;
		this.recibidoPor = recibidoPor;
	}

	@PrePersist
	void alCrear() {
		if (fechaRecepcion == null) {
			fechaRecepcion = LocalDateTime.now(ZoneOffset.UTC);
		}
	}

	public void agregarDetalle(RecepcionDetalle detalle) {
		detalle.asignarRecepcion(this);
		detalles.add(detalle);
	}

	public Integer getId() {
		return id;
	}

	public Compra getCompra() {
		return compra;
	}

	public Usuario getRecibidoPor() {
		return recibidoPor;
	}

	public LocalDateTime getFechaRecepcion() {
		return fechaRecepcion;
	}

	public List<RecepcionDetalle> getDetalles() {
		return detalles;
	}
}
