import {
  AmbitoProducto,
  CartaDia,
  ComponenteProducto,
  DetalleCarta,
  LineaCartaVista,
  Producto,
  SeccionCarta,
  UnidadNegocio,
} from '../datos/supervision.models';

export function ambitoDe(unidad: UnidadNegocio): AmbitoProducto {
  return unidad === 'Pollería' ? 'POLLERIA' : 'RESTAURANTE';
}

export function productoSirveEn(producto: Pick<Producto, 'activo' | 'ambito'>, unidad: UnidadNegocio): boolean {
  return producto.activo && (producto.ambito === 'AMBOS' || producto.ambito === ambitoDe(unidad));
}

export function esProductoBase(producto: Pick<Producto, 'componentes'>): boolean {
  return !producto.componentes || producto.componentes.length === 0;
}

export function precioVigente(detalle: Pick<DetalleCarta, 'precioDia' | 'precioEfectivo'>, producto?: Pick<Producto, 'precio'>): number {
  if (detalle.precioDia != null) {
    return detalle.precioDia;
  }
  if (typeof detalle.precioEfectivo === 'number') {
    return detalle.precioEfectivo;
  }
  return producto?.precio ?? 0;
}

export function lineasDeSeccion(carta: Pick<CartaDia, 'detalles'> | undefined, seccion: SeccionCarta): LineaCartaVista[] {
  if (!carta) {
    return [];
  }
  return unicosDetalles(carta.detalles.filter((d) => d.seccion === seccion)).map((d) => ({
    detalleId: d.id,
    productoId: d.productoId,
    nombre: d.nombre,
    precio: precioVigente(d),
    estado: d.estado,
    componentes: d.componentes ?? [],
  }));
}

export function unicosDetalles<T extends { id?: number; productoId: number; seccion: string }>(detalles: readonly T[]): T[] {
  const porId = new Set<number>();
  const porClave = new Set<string>();
  const unicos: T[] = [];
  for (const detalle of detalles) {
    if (detalle.id && porId.has(detalle.id)) {
      continue;
    }
    const clave = `${detalle.productoId}:${detalle.seccion}`;
    if (porClave.has(clave)) {
      continue;
    }
    if (detalle.id) {
      porId.add(detalle.id);
    }
    porClave.add(clave);
    unicos.push(detalle);
  }
  return unicos;
}

export function seccionActiva(lineas: readonly Pick<LineaCartaVista, 'estado'>[]): boolean {
  return lineas.length === 0 || lineas.some((l) => l.estado === 'DISPONIBLE');
}

export function resumenComponentes(componentes: readonly Pick<ComponenteProducto, 'nombre'>[]): string {
  return componentes
    .map((c) => c.nombre)
    .filter((nombre) => nombre)
    .join(' + ');
}

export function reemplazarSeccion(carta: CartaDia, seccion: SeccionCarta, lineas: readonly DetalleCarta[]): CartaDia {
  return {
    ...carta,
    detalles: [...carta.detalles.filter((d) => d.seccion !== seccion), ...lineas],
  };
}
