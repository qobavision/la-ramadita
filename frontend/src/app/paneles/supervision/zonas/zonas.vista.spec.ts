import {
  detalleCapacidadZona,
  enteroNoNegativo,
  mesasParaCard,
  puedeEliminarZona,
  puedeInactivarZona,
  usoZona,
} from './zonas.vista';

describe('zonas.vista', () => {
  const mesas = [
    { id: 1, nombre: 'Mesa 1', zonaId: 1, zona: 'Salón', activa: true, estadoOp: 'Ocupada' as const, atencion: 'A-1', mesero: 'Luis', tiempo: '10 min' },
    { id: 8, nombre: 'Mesa 8', zonaId: 3, zona: 'Segundo piso', activa: false, estadoOp: 'Libre' as const, atencion: null, mesero: null, tiempo: null },
  ];

  it('describe la capacidad de una zona', () => {
    expect(detalleCapacidadZona({ mesas: 10, cajas: 2, cocinas: 1 })).toBe('10 mesas · 2 cajas · 1 cocina');
    expect(detalleCapacidadZona({ mesas: 1, cajas: 1, cocinas: 2 })).toBe('1 mesa · 1 caja · 2 cocinas');
  });

  it('normaliza cantidades a enteros no negativos', () => {
    expect(enteroNoNegativo(10.8)).toBe(10);
    expect(enteroNoNegativo(-3)).toBe(0);
    expect(enteroNoNegativo('2')).toBe(2);
  });

  it('permite eliminar solo una zona nueva y sin uso', () => {
    expect(usoZona({ id: 4 }, mesas)).toBe('sin-uso');
    expect(puedeEliminarZona('sin-uso')).toBe(true);
    expect(puedeEliminarZona('historial')).toBe(false);
    expect(puedeEliminarZona('operacion-activa')).toBe(false);
  });

  it('bloquea inactivar si hay operación activa y permite historial', () => {
    expect(usoZona({ id: 1 }, mesas)).toBe('operacion-activa');
    expect(usoZona({ id: 3 }, mesas)).toBe('historial');
    expect(puedeInactivarZona({ activa: true }, 'operacion-activa')).toBe(false);
    expect(puedeInactivarZona({ activa: true }, 'historial')).toBe(true);
    expect(puedeInactivarZona({ activa: false }, 'operacion-activa')).toBe(true);
  });

  it('muestra mesas compactas y un resto si hay más de 12', () => {
    expect(mesasParaCard(10).visibles).toEqual(['M1', 'M2', 'M3', 'M4', 'M5', 'M6', 'M7', 'M8', 'M9', 'M10']);
    expect(mesasParaCard(10).extra).toBe(0);
    expect(mesasParaCard(15).visibles).toHaveLength(12);
    expect(mesasParaCard(15).extra).toBe(3);
  });
});
