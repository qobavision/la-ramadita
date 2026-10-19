import { AmbitoProducto, CategoriaProducto, ComponenteProducto, EstadoProductoCarta, SeccionCarta, UnidadNegocio } from '../paneles/supervision/datos/supervision.models';

export interface ProductoApi {
  id: number;
  nombre: string;
  categoria: string;
  ambito: string;
  precio: number;
  activo: boolean;
  compuesto: boolean;
  componentes: { productoId: number; nombre: string; cantidad: number }[];
}

export interface RecetaApi {
  productoId: number;
  nombre: string;
  categoria: string;
  ambito: string;
  precio: number;
  activo: boolean;
  insumos: { insumoId: number; insumo: string; unidadMedida: string; cantidad: number }[];
}

export interface CartaDetalleApi {
  id: number;
  productoId: number;
  nombre: string;
  seccion: SeccionCarta;
  precioDia: number | null;
  precioBase: number;
  precioEfectivo: number;
  estado: EstadoProductoCarta;
  componentes: { productoId: number; nombre: string; cantidad: number }[];
}

export interface CartaApi {
  id: number;
  nombre: string;
  ambito: 'RESTAURANTE' | 'POLLERIA';
  fecha: string;
  estado: 'ACTIVA' | 'CERRADA';
  vigente: boolean;
  detalles: CartaDetalleApi[];
}

export function ambitoApi(unidad: UnidadNegocio): 'RESTAURANTE' | 'POLLERIA' {
  return unidad === 'Pollería' ? 'POLLERIA' : 'RESTAURANTE';
}

export function categoriaDesdeApi(valor: string): CategoriaProducto {
  const mapa: Record<string, CategoriaProducto> = {
    MENU: 'Menú',
    ENTRADA: 'Entrada',
    SEGUNDO: 'Segundo',
    PLATO: 'Plato ejecutivo',
    PLATO_EJECUTIVO: 'Plato ejecutivo',
    PLATO_MARINO: 'Plato marino',
    BEBIDA: 'Bebida',
    ENSALADA: 'Ensalada',
    POLLO_A_LA_BRASA: 'Plato a la brasa',
    CHIFA: 'Plato chifa',
    GUARNICION: 'Guarnición',
    COMBO: 'Combo',
    POSTRE: 'Postre',
    EXTRA: 'Extra',
    OTRO: 'Otro',
  };
  return mapa[valor] ?? 'Otro';
}

export function categoriaHaciaApi(valor: CategoriaProducto): string {
  const mapa: Record<CategoriaProducto, string> = {
    Menú: 'MENU',
    Entrada: 'ENTRADA',
    Segundo: 'SEGUNDO',
    'Plato ejecutivo': 'PLATO_EJECUTIVO',
    'Plato marino': 'PLATO_MARINO',
    'Plato chifa': 'CHIFA',
    'Plato a la brasa': 'POLLO_A_LA_BRASA',
    Bebida: 'BEBIDA',
    Ensalada: 'ENSALADA',
    Guarnición: 'GUARNICION',
    Combo: 'COMBO',
    Postre: 'POSTRE',
    Extra: 'EXTRA',
    Otro: 'OTRO',
  };
  return mapa[valor];
}

export function ambitoDesdeApi(valor: string): AmbitoProducto {
  if (valor === 'POLLERIA') {
    return 'POLLERIA';
  }
  if (valor === 'AMBOS') {
    return 'AMBOS';
  }
  return 'RESTAURANTE';
}

export function mapearComponentes(api: ProductoApi['componentes'] | CartaDetalleApi['componentes']): ComponenteProducto[] {
  return (api ?? []).map((c) => ({ productoId: c.productoId, nombre: c.nombre, cantidad: Number(c.cantidad) }));
}
