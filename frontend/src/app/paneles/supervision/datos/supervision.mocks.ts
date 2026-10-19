import {
  AsignacionTemporal,
  CompraDemo,
  HorarioBase,
  InsumoDemo,
  MesaDemo,
  MovimientoDemo,
  PedidoDemo,
  PendienteCierre,
  PersonalTurno,
  ProveedorDemo,
  TurnoOperativo,
  ZonaDemo,
} from './supervision.models';

export function horariosBaseIniciales(): HorarioBase[] {
  return [
    { unidad: 'Restaurante', horaInicio: '07:00', horaFin: '16:00', configurado: true },
    { unidad: 'Pollería', horaInicio: '16:00', horaFin: '01:00', configurado: true },
  ];
}

export function turnoInicial(): TurnoOperativo {
  return {
    id: null,
    unidad: 'Restaurante',
    horaInicio: '07:00',
    horaFin: '16:00',
    estado: 'CERRADO',
    caja: 'Cerrada',
    alerta: null,
    responsable: '',
    cerradoPor: '',
  };
}

export function pendientesCierreIniciales(): PendienteCierre[] {
  return [];
}

export function personalInicial(): PersonalTurno[] {
  return [];
}

export function asignacionesIniciales(): AsignacionTemporal[] {
  return [];
}

export function zonasIniciales(): ZonaDemo[] {
  return [];
}

export function mesasIniciales(): MesaDemo[] {
  return [];
}

export function pedidosIniciales(): PedidoDemo[] {
  return [];
}

export function proveedoresIniciales(): ProveedorDemo[] {
  return [];
}

export function comprasIniciales(): CompraDemo[] {
  return [];
}

export function insumosIniciales(): InsumoDemo[] {
  return [];
}

export function movimientosIniciales(): MovimientoDemo[] {
  return [];
}
