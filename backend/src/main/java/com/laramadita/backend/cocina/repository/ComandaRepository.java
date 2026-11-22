package com.laramadita.backend.cocina.repository;

import com.laramadita.backend.pedidos.model.Atencion;
import com.laramadita.backend.pedidos.model.EstadoAtencion;
import com.laramadita.backend.pedidos.model.EstadoCocina;
import com.laramadita.backend.pedidos.model.EstadoItem;
import com.laramadita.backend.pedidos.model.EstadoPedido;
import com.laramadita.backend.pedidos.model.Pedido;
import java.util.Collection;
import java.util.List;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Pedidos vistos desde Cocina (comandas). Solo lectura y marcado de preparado. */
public interface ComandaRepository extends JpaRepository<Pedido, Integer> {

	/**
	 * Comandas del monitor: las de atenciones en curso y, además, cualquier
	 * comanda con productos aún por preparar aunque la atención ya se haya
	 * cobrado (p. ej. un pedido para llevar pagado antes de prepararse).
	 */
	@EntityGraph(attributePaths = { "atencion", "atencion.mesa", "atencion.mesa.zona", "atencion.mesero" })
	@Query("""
			select p from Pedido p
			where p.estado in :estados
			  and (p.atencion.estado in :activas
			       or exists (select d.id from PedidoDetalle d
			                  where d.pedido = p and d.estadoItem = :activo and d.estadoCocina = :pendiente))
			order by p.fechaEnvio asc
			""")
	List<Pedido> enMonitor(
			@Param("estados") Collection<EstadoPedido> estados,
			@Param("activas") Collection<EstadoAtencion> activas,
			@Param("activo") EstadoItem activo,
			@Param("pendiente") EstadoCocina pendiente);

	/** Todo lo enviado a Cocina durante un turno (historial). */
	@EntityGraph(attributePaths = { "atencion", "atencion.mesa", "atencion.mesa.zona", "atencion.mesero" })
	List<Pedido> findByAtencion_Turno_IdAndFechaEnvioIsNotNullOrderByFechaEnvioDesc(Integer turnoId);

	@EntityGraph(attributePaths = { "atencion", "atencion.mesa", "atencion.mesa.zona", "atencion.mesero" })
	Optional<Pedido> findComandaById(Integer id);

	@Query("select p.atencion.id from Pedido p where p.id = :id")
	Optional<Integer> atencionDeComanda(@Param("id") Integer pedidoId);

	/**
	 * Bloquea la atención (UPDLOCK en SQL Server) hasta el fin de la transacción.
	 * Cocina y mesero bloquean primero la atención antes de tocar sus líneas,
	 * así un "Listo" y un cambio simultáneos no se pisan (sin @Version ni migración).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from Atencion a where a.id = :id")
	Optional<Atencion> bloquearAtencion(@Param("id") Integer atencionId);
}
