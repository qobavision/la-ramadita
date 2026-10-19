import { esProductoBase, lineasDeSeccion, precioVigente, productoSirveEn, resumenComponentes, seccionActiva } from './carta.vista';
import { CartaDia, Producto } from '../datos/supervision.models';

const productos: Producto[] = [
  { id: 11, nombre: '1/4 Pollo', categoria: 'Plato a la brasa', ambito: 'POLLERIA', precio: 12, activo: true },
  { id: 18, nombre: 'Porción de papas fritas', categoria: 'Guarnición', ambito: 'POLLERIA', precio: 5, activo: true },
  { id: 7, nombre: 'Inca Kola personal', categoria: 'Bebida', ambito: 'AMBOS', precio: 4, activo: true },
  { id: 5, nombre: 'Lomo saltado', categoria: 'Plato ejecutivo', ambito: 'RESTAURANTE', precio: 22, activo: false },
  {
    id: 31,
    nombre: '1/4 Pollo clásico',
    categoria: 'Plato a la brasa',
    ambito: 'POLLERIA',
    precio: 14,
    activo: true,
    componentes: [
      { productoId: 11, nombre: '1/4 Pollo', cantidad: 1 },
      { productoId: 18, nombre: 'Porción de papas fritas', cantidad: 1 },
    ],
  },
];

const carta: CartaDia = {
  id: 1,
  nombre: 'Carta Pollería',
  unidad: 'Pollería',
  fecha: '2026-10-18',
  estado: 'ACTIVA',
  vigente: true,
  detalles: [
    {
      id: 1,
      productoId: 31,
      nombre: '1/4 Pollo clásico',
      seccion: 'BRASA',
      precioDia: null,
      precioEfectivo: 14,
      estado: 'DISPONIBLE',
      componentes: productos[4]!.componentes ?? [],
    },
    {
      id: 2,
      productoId: 7,
      nombre: 'Inca Kola personal',
      seccion: 'BEBIDA',
      precioDia: 4.5,
      precioEfectivo: 4.5,
      estado: 'AGOTADO',
      componentes: [],
    },
  ],
};

describe('carta.vista', () => {
  it('filtra productos por unidad y estado', () => {
    expect(productoSirveEn(productos[0]!, 'Pollería')).toBe(true);
    expect(productoSirveEn(productos[0]!, 'Restaurante')).toBe(false);
    expect(productoSirveEn(productos[2]!, 'Restaurante')).toBe(true);
    expect(productoSirveEn(productos[3]!, 'Restaurante')).toBe(false);
  });

  it('distingue productos base de productos armados', () => {
    expect(esProductoBase(productos[0]!)).toBe(true);
    expect(esProductoBase(productos[4]!)).toBe(false);
  });

  it('usa el precio del día cuando existe y el efectivo cuando no', () => {
    expect(precioVigente({ precioDia: null, precioEfectivo: 14 })).toBe(14);
    expect(precioVigente({ precioDia: 15, precioEfectivo: 14 })).toBe(15);
  });

  it('arma las líneas de una sección', () => {
    const brasa = lineasDeSeccion(carta, 'BRASA');
    expect(brasa).toHaveLength(1);
    expect(brasa[0]).toMatchObject({ detalleId: 1, nombre: '1/4 Pollo clásico', precio: 14 });
    expect(resumenComponentes(brasa[0]!.componentes)).toBe('1/4 Pollo + Porción de papas fritas');
    expect(lineasDeSeccion(carta, 'BEBIDA')[0]!.precio).toBe(4.5);
    expect(lineasDeSeccion(undefined, 'BEBIDA')).toEqual([]);
  });

  it('considera inactiva una sección con todo agotado', () => {
    expect(seccionActiva([])).toBe(true);
    expect(seccionActiva([{ estado: 'DISPONIBLE' }, { estado: 'AGOTADO' }])).toBe(true);
    expect(seccionActiva([{ estado: 'AGOTADO' }])).toBe(false);
  });
});
