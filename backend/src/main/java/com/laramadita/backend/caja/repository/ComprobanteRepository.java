package com.laramadita.backend.caja.repository;

import com.laramadita.backend.caja.model.Comprobante;
import com.laramadita.backend.caja.model.EstadoComprobante;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Integer> {

	@EntityGraph(attributePaths = { "emitidoPor" })
	Optional<Comprobante> findFirstByAtencion_IdAndEstado(Integer atencionId, EstadoComprobante estado);

	/** Último correlativo de la serie, bloqueando el rango para que dos cobros no tomen el mismo número. */
	@Query(value = """
			SELECT COALESCE(MAX(numero), 0)
			FROM dbo.comprobantes WITH (UPDLOCK, HOLDLOCK)
			WHERE tipo = :tipo AND serie = :serie
			""", nativeQuery = true)
	int ultimoNumero(@Param("tipo") String tipo, @Param("serie") String serie);
}
