package com.laramadita.backend.proveedores.repository;

import com.laramadita.backend.proveedores.model.Proveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

	List<Proveedor> findAllByOrderByNombreRazonSocialAsc();

	List<Proveedor> findByActivoTrueOrderByNombreRazonSocialAsc();

	boolean existsByDocumento(String documento);

	boolean existsByDocumentoAndIdNot(String documento, Integer id);
}
