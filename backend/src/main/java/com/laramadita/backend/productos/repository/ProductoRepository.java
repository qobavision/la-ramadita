package com.laramadita.backend.productos.repository;

import com.laramadita.backend.productos.model.AmbitoProducto;
import com.laramadita.backend.productos.model.CategoriaProducto;
import com.laramadita.backend.productos.model.Producto;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

	Optional<Producto> findByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

	@EntityGraph(attributePaths = { "receta", "receta.insumo", "componentes", "componentes.componente" })
	Optional<Producto> findWithRelacionesById(Integer id);

	@EntityGraph(attributePaths = { "receta", "receta.insumo", "componentes", "componentes.componente" })
	@Query("select distinct p from Producto p order by p.nombre")
	List<Producto> findAllConRelaciones();

	@EntityGraph(attributePaths = { "receta", "receta.insumo", "componentes", "componentes.componente" })
	@Query("select distinct p from Producto p where p.activo = true order by p.nombre")
	List<Producto> findActivosConRelaciones();

	@EntityGraph(attributePaths = { "receta", "receta.insumo" })
	@Query("select distinct p from Producto p join p.receta r order by p.nombre")
	List<Producto> findConReceta();

	@EntityGraph(attributePaths = { "receta", "receta.insumo" })
	@Query("select p from Producto p where p.id = :id")
	Optional<Producto> findConRecetaById(@Param("id") Integer id);

	List<Producto> findByAmbitoInAndActivoTrueOrderByNombreAsc(Collection<AmbitoProducto> ambitos);

	List<Producto> findByCategoriaAndActivoTrueOrderByNombreAsc(CategoriaProducto categoria);

	@Query("""
			select case when count(c) > 0 then true else false end
			from CartaDetalle c where c.producto.id = :productoId
			""")
	boolean existeEnAlgunaCarta(@Param("productoId") Integer productoId);

	@Query("""
			select case when count(pc) > 0 then true else false end
			from ProductoComponente pc where pc.componente.id = :productoId
			""")
	boolean existeComoComponente(@Param("productoId") Integer productoId);
}
