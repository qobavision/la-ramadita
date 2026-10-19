import {
  claseRolTemporal,
  coincidePersonalTurno,
  etiquetaEstadoTurno,
  etiquetaFuncionActual,
  etiquetaRolTemporal,
  funcionOperativaDe,
  marcadorTurno,
  minutosHastaFin,
  textoTiempoRestante,
} from './supervision.models';

describe('supervision.models turnos', () => {
  const ana = {
    id: 1,
    empleado: 'Ana Torres',
    username: 'ana.torres',
    rolPermanente: 'MESERO' as const,
    funcionActual: 'CAJA (temporal)',
    horaIngreso: '16:05',
    estado: 'Asignación temporal' as const,
  };

  const carlos = {
    id: 2,
    empleado: 'Carlos Quispe',
    username: 'carlos.quispe',
    rolPermanente: 'MESERO' as const,
    funcionActual: 'MESERO',
    horaIngreso: '16:02',
    estado: 'En turno' as const,
  };

  it('muestra la función temporal sin cambiar el rol permanente', () => {
    expect(funcionOperativaDe(ana)).toBe('CAJA');
    expect(etiquetaFuncionActual(ana)).toBe('Caja temporal');
    expect(etiquetaFuncionActual(carlos)).toBe('Mesero');
    expect(etiquetaRolTemporal(ana)).toBe('Caja');
    expect(etiquetaRolTemporal(carlos)).toBe('Mesero');
    expect(claseRolTemporal(ana)).toBe('lr-estado--abierta');
    expect(claseRolTemporal(carlos)).toBe('lr-estado--activo');
  });

  it('filtra por nombre, usuario y función actual', () => {
    expect(coincidePersonalTurno(ana, 'torres', '')).toBe(true);
    expect(coincidePersonalTurno(ana, 'ana.torres', 'CAJA')).toBe(true);
    expect(coincidePersonalTurno(ana, 'ana.torres', 'MESERO')).toBe(false);
    expect(coincidePersonalTurno(carlos, 'caja', '')).toBe(false);
  });

  it('etiqueta el estado del turno en español', () => {
    expect(etiquetaEstadoTurno('ACTIVO')).toBe('Activo');
    expect(etiquetaEstadoTurno('EN CIERRE')).toBe('En cierre');
    expect(etiquetaEstadoTurno('CERRADO')).toBe('Cerrado');
  });

  it('calcula el tiempo restante y el color del marcador', () => {
    expect(textoTiempoRestante(43)).toBe('Falta 43 minutos');
    expect(textoTiempoRestante(90)).toBe('Falta 1 h 30 min');
    expect(marcadorTurno('ACTIVO', 90).clase).toBe('lr-estado--activo');
    expect(marcadorTurno('ACTIVO', 20).clase).toBe('lr-estado--abierta');
    expect(marcadorTurno('EN CIERRE', 15).texto).toContain('En cierre');
    expect(marcadorTurno('EN CIERRE', 15).clase).toBe('lr-estado--alerta');
    const ahora = new Date('2026-10-13T23:30:00');
    expect(minutosHastaFin('01:00', ahora)).toBe(90);
  });
});
