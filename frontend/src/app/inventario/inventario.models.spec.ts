import { estadoStockDe } from '../paneles/supervision/datos/supervision.models';
import {
  coincideFiltroInsumo,
  convertirCantidadUnidad,
  etiquetaEstadoOperativo,
  factorConversionUnidad,
  mapearCompra,
  mapearInsumo,
  mapearMovimiento,
  mapearProveedor,
  mensajeUnidadIncompatible,
  muestraEstadoStock,
  unidadDesdeApi,
  unidadHaciaApi,
} from './inventario.models';

describe('inventario.models', () => {
  it('mapea unidades de ida y vuelta', () => {
    expect(unidadDesdeApi('UNIDAD')).toBe('Unidad');
    expect(unidadHaciaApi('Kilogramo (kg)')).toBe('KILOGRAMO');
  });

  it('convierte kilogramo a gramo y no convierte unidad', () => {
    expect(factorConversionUnidad('Kilogramo (kg)', 'Gramo (g)')).toBe(1000);
    expect(convertirCantidadUnidad(5, 1000)).toBe(5000);
    expect(convertirCantidadUnidad(50, 1000)).toBe(50000);
    expect(factorConversionUnidad('Unidad', 'Kilogramo (kg)')).toBeNull();
    expect(factorConversionUnidad('Gramo (g)', 'Litro (L)')).toBeNull();
    expect(mensajeUnidadIncompatible('Unidad', 'Kilogramo (kg)')).toContain('elimínalo');
  });

  it('mapea insumo con estado calculado del backend', () => {
    const insumo = mapearInsumo({
      id: 1,
      nombre: 'Pollo Entero',
      unidadMedida: 'UNIDAD',
      stockActual: 10,
      stockMinimo: 3,
      estado: 'NORMAL',
      activo: true,
    });
    expect(insumo.nombre).toBe('Pollo Entero');
    expect(insumo.activo).toBe(true);
    expect(estadoStockDe(insumo)).toBe('NORMAL');
    expect(etiquetaEstadoOperativo(insumo)).toBe('NORMAL');
    expect(muestraEstadoStock(insumo)).toBe(true);
  });

  it('mapea insumo inactivo y muestra estado operativo, no de stock', () => {
    const insumo = mapearInsumo({
      id: 8,
      nombre: 'Papa Huayro',
      unidadMedida: 'KILOGRAMO',
      stockActual: 0,
      stockMinimo: 3,
      estado: 'AGOTADO',
      activo: false,
    });
    expect(insumo.activo).toBe(false);
    expect(etiquetaEstadoOperativo(insumo)).toBe('Inactivo');
    expect(muestraEstadoStock(insumo)).toBe(false);
  });

  it('filtra activos, inactivos y todos combinando la búsqueda', () => {
    const activo = {
      nombre: 'Papa Amarilla',
      unidad: 'Kilogramo (kg)' as const,
      activo: true,
    };
    const inactivo = {
      nombre: 'Papa Huayro',
      unidad: 'Kilogramo (kg)' as const,
      activo: false,
    };

    expect(coincideFiltroInsumo(activo, 'ACTIVOS')).toBe(true);
    expect(coincideFiltroInsumo(inactivo, 'ACTIVOS')).toBe(false);
    expect(coincideFiltroInsumo(activo, 'INACTIVOS')).toBe(false);
    expect(coincideFiltroInsumo(inactivo, 'INACTIVOS')).toBe(true);
    expect(coincideFiltroInsumo(activo, 'TODOS')).toBe(true);
    expect(coincideFiltroInsumo(inactivo, 'TODOS')).toBe(true);
    expect(coincideFiltroInsumo(inactivo, 'INACTIVOS', 'huayro')).toBe(true);
    expect(coincideFiltroInsumo(inactivo, 'INACTIVOS', 'aceite')).toBe(false);
    expect(coincideFiltroInsumo(activo, 'TODOS', 'papa')).toBe(true);
  });

  it('mapea proveedor y compra operativa', () => {
    const proveedor = mapearProveedor({
      id: 2,
      nombreRazonSocial: 'Avícola San Pedro',
      tipoDocumento: 'RUC',
      documento: '20123456789',
      celular: '987654321',
      personaContacto: 'Juan Perez',
      activo: true,
    });
    expect(proveedor.nombre).toBe('Avícola San Pedro');

    const compra = mapearCompra({
      id: 10,
      codigo: 'COMP-001',
      proveedorId: 2,
      proveedor: 'Avícola San Pedro',
      fechaRegistro: '2026-10-11T13:20:00',
      estado: 'PARCIAL',
      total: 500,
      registradoPor: 'supervisor',
      detalles: [
        {
          id: 11,
          insumoId: 1,
          insumo: 'Pollo Entero',
          unidadMedida: 'UNIDAD',
          cantidad: 50,
          precioUnitario: 10,
          subtotal: 500,
          cantidadRecibida: 40,
          cantidadPendiente: 10,
          estado: 'PARCIAL',
        },
      ],
    });
    expect(compra.codigo).toBe('COMP-001');
    expect(compra.estado).toBe('Parcial');
    expect(compra.lineas[0].recibido).toBe(40);
  });

  it('mapea movimiento de entrada', () => {
    const movimiento = mapearMovimiento({
      id: 3,
      fechaHora: '2026-10-11T13:20:00',
      insumoId: 1,
      insumo: 'Pollo Entero',
      unidadMedida: 'UNIDAD',
      tipo: 'ENTRADA',
      cantidad: 40,
      stockAnterior: 10,
      stockResultante: 50,
      origen: 'Recepción COMP-001',
      usuario: 'Rosa Huaman',
    });
    expect(movimiento.tipo).toBe('ENTRADA');
    expect(movimiento.origen).toBe('Recepción COMP-001');
    expect(movimiento.stockResultante).toBe(50);
  });
});
