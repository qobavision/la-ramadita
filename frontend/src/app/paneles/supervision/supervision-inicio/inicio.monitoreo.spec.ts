import {
  filtrarMesasMonitoreo,
  mesasMonitoreoDe,
  mesasMonitoreoIniciales,
  resumenMesas,
  resumenProductos,
} from './inicio.monitoreo';

describe('inicio.monitoreo', () => {
  it('resume el contenido de cocina sin listar todo', () => {
    expect(resumenProductos(['Pollo', 'arroz', 'ensalada', 'papa', 'gaseosa'])).toBe('Pollo + arroz +3 más');
    expect(resumenProductos(['Lomo saltado'])).toBe('Lomo saltado');
  });

  it('parte sin mesas de ejemplo y filtra por estado', () => {
    expect(mesasMonitoreoIniciales()).toEqual([]);
    const mesas = [
      { id: 1, nombre: 'Mesa 01', zona: 'Salón', estado: 'Ocupada' as const, mesero: null, pedido: null, tiempo: null, total: null },
      { id: 2, nombre: 'Mesa 02', zona: 'Salón', estado: 'Libre' as const, mesero: null, pedido: null, tiempo: null, total: null },
    ];
    expect(resumenMesas(mesas)).toBe('1 ocupadas · 1 libres');
    expect(filtrarMesasMonitoreo(mesas, 'Ocupada')).toHaveLength(1);
    expect(filtrarMesasMonitoreo(mesas, 'Libre')).toHaveLength(1);
    expect(filtrarMesasMonitoreo(mesas, 'Todas')).toHaveLength(2);
  });

  it('muestra mesas activas del salón sin mesero ni pedido', () => {
    const visibles = mesasMonitoreoDe([
      {
        id: 1,
        nombre: 'Mesa 1',
        zonaId: 4,
        zona: 'Salón',
        activa: true,
        estadoOp: 'Libre',
        atencion: null,
        mesero: null,
        tiempo: null,
      },
      {
        id: 2,
        nombre: 'Mesa 2',
        zonaId: 4,
        zona: 'Salón',
        activa: false,
        estadoOp: 'Libre',
        atencion: null,
        mesero: null,
        tiempo: null,
      },
    ]);
    expect(visibles).toHaveLength(1);
    expect(visibles[0].nombre).toBe('Mesa 1');
    expect(visibles[0].mesero).toBeNull();
    expect(visibles[0].total).toBeNull();
  });
});
