package com.laramadita.backend.proveedores.repository;

import com.laramadita.backend.proveedores.model.Proveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

	List<Proveedor> findByArchivadoFalseOrderByNombreRazonSocialAsc();

	List<Proveedor> findByActivoTrueAndArchivadoFalseOrderByNombreRazonSocialAsc();

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdNot(String documento, Integer id);
}
