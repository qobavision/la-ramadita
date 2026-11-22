package com.laramadita.backend.caja.model;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.shared.error.ReglaNegocioException;
import com.laramadita.backend.usuarios.model.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.hibernate.annotations.Nationalized;

/**
 * Comprobante simulado (M10, sin SUNAT). Tabla {@code dbo.comprobantes} de V1,
 * estado en V15. Se emite una boleta simple cuando la cuenta queda pagada.
 */
@Entity
@Table(name = "comprobantes")
public class Comprobante {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comprobante_id")
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "atencion_id", nullable = false)
	private Atencion atencion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "emitido_por_usuario_id", nullable = false)
	private Usuario emitidoPor;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "tipo", nullable = false, length = 20)
	private TipoComprobante tipo;

	@Nationalized
	@Column(name = "serie", nullable = false, length = 10)
	private String serie;

	@Column(name = "numero", nullable = false)
	private int numero;

	@Column(name = "fecha_emision", nullable = false)
	private LocalDateTime fechaEmision;

	@Column(name = "total", nullable = false, precision = 12, scale = 2)
	private BigDecimal total;

	@Enumerated(EnumType.STRING)
	@Nationalized
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoComprobante estado = EstadoComprobante.EMITIDO;

	@Column(name = "fecha_anulacion")
	private LocalDateTime fechaAnulacion;

	protected Comprobante() {
	}

	public static Comprobante boleta(Atencion atencion, Usuario emitidoPor, String serie, int numero) {
		Comprobante c = new Comprobante();
		c.atencion = atencion;
		c.emitidoPor = emitidoPor;
		c.tipo = TipoComprobante.BOLETA;
		c.serie = serie;
		c.numero = numero;
		c.total = atencion.totalEnviado();
		c.fechaEmision = LocalDateTime.now(ZoneOffset.UTC);
		c.estado = EstadoComprobante.EMITIDO;
		return c;
	}

	/** Se anuló un pago de la cuenta: la boleta deja de valer y se emitirá otra al volver a pagarse. */
	public void anular() {
		if (estado == EstadoComprobante.ANULADO) {
			throw new ReglaNegocioException("El comprobante ya está anulado");
		}
		this.estado = EstadoComprobante.ANULADO;
		this.fechaAnulacion = LocalDateTime.now(ZoneOffset.UTC);
	}

	/** Serie y correlativo, por ejemplo B001-00000012. */
	public String codigo() {
		return serie + "-" + String.format("%08d", numero);
	}

	public Integer getId() {
		return id;
	}

	public Atencion getAtencion() {
		return atencion;
	}

	public Usuario getEmitidoPor() {
		return emitidoPor;
	}

	public TipoComprobante getTipo() {
		return tipo;
	}

	public String getSerie() {
		return serie;
	}

	public int getNumero() {
		return numero;
	}

	public LocalDateTime getFechaEmision() {
		return fechaEmision;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public EstadoComprobante getEstado() {
		return estado;
	}

	public LocalDateTime getFechaAnulacion() {
		return fechaAnulacion;
	}
}
