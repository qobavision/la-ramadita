package com.laramadita.backend.productos.repository;

import com.laramadita.backend.productos.model.CartaDetalle;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartaDetalleRepository extends JpaRepository<CartaDetalle, Integer> {

	@EntityGraph(attributePaths = { "carta", "producto" })
	List<CartaDetalle> findByProducto_Id(Integer productoId);
}
