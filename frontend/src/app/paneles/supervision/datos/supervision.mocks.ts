import {
  AsignacionTemporal,
  CompraDemo,
  HorarioBase,
  InsumoDemo,
  ItemCartaDemo,
  MesaDemo,
  MovimientoDemo,
  PedidoDemo,
  PendienteCierre,
  PersonalTurno,
  ProductoDemo,
  ProveedorDemo,
  RecetaDemo,
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
    unidad: 'Pollería',
    horaInicio: '16:00',
    horaFin: '01:00',
    estado: 'ACTIVO',
    caja: 'Abierta',
    alerta: 'El turno Pollería finaliza en 15 minutos.',
  };
}

export function pendientesCierreIniciales(): PendienteCierre[] {
  return [
    { etiqueta: 'Atenciones abiertas', cantidad: 4 },
    { etiqueta: 'Pedidos pendientes', cantidad: 3 },
    { etiqueta: 'Cajas pendientes de cierre', cantidad: 1 },
  ];
}

export function personalInicial(): PersonalTurno[] {
  return [
    {
      id: 1,
      empleado: 'Ana Torres',
      username: 'ana.torres',
      rolPermanente: 'MESERO',
      funcionActual: 'CAJA (temporal)',
      horaIngreso: '16:05',
      estado: 'Asignación temporal',
    },
    {
      id: 2,
      empleado: 'Carlos Quispe',
      username: 'carlos.quispe',
      rolPermanente: 'MESERO',
      funcionActual: 'MESERO',
      horaIngreso: '16:02',
      estado: 'En turno',
    },
    {
      id: 3,
      empleado: 'Luis Paredes',
      username: 'luis.paredes',
      rolPermanente: 'COCINA',
      funcionActual: 'COCINA',
      horaIngreso: '15:50',
      estado: 'En turno',
    },
    {
      id: 4,
      empleado: 'María Gómez',
      username: 'maria.gomez',
      rolPermanente: 'CAJA',
      funcionActual: 'CAJA',
      horaIngreso: '16:00',
      estado: 'En turno',
    },
    {
      id: 5,
      empleado: 'Pedro Ramos',
      username: 'pedro.ramos',
      rolPermanente: 'MESERO',
      funcionActual: 'MESERO',
      horaIngreso: '16:12',
      estado: 'En turno',
    },
  ];
}

export function asignacionesIniciales(): AsignacionTemporal[] {
  return [
    {
      id: 1,
      empleado: 'Ana Torres',
      username: 'ana.torres',
      rolPermanente: 'MESERO',
      rolTemporal: 'CAJA',
      turno: 'Pollería',
      estado: 'Vigente',
    },
    {
      id: 2,
      empleado: 'Carlos Quispe',
      username: 'carlos.quispe',
      rolPermanente: 'MESERO',
      rolTemporal: null,
      turno: 'Pollería',
      estado: 'Sin asignación',
    },
    {
      id: 3,
      empleado: 'Luis Paredes',
      username: 'luis.paredes',
      rolPermanente: 'COCINA',
      rolTemporal: null,
      turno: 'Pollería',
      estado: 'Sin asignación',
    },
    {
      id: 4,
      empleado: 'Pedro Ramos',
      username: 'pedro.ramos',
      rolPermanente: 'MESERO',
      rolTemporal: null,
      turno: 'Pollería',
      estado: 'Sin asignación',
    },
  ];
}

export function zonasIniciales(): ZonaDemo[] {
  return [
    { id: 1, nombre: 'Salón principal', activa: true },
    { id: 2, nombre: 'Terraza', activa: true },
    { id: 3, nombre: 'Segundo piso', activa: false },
  ];
}

export function mesasIniciales(): MesaDemo[] {
  return [
    { id: 1, nombre: 'Mesa 1', zonaId: 1, zona: 'Salón principal', activa: true, estadoOp: 'Ocupada', atencion: 'A-104', mesero: 'Carlos Quispe', tiempo: '28 min' },
    { id: 2, nombre: 'Mesa 2', zonaId: 1, zona: 'Salón principal', activa: true, estadoOp: 'Libre', atencion: null, mesero: null, tiempo: null },
    { id: 3, nombre: 'Mesa 3', zonaId: 1, zona: 'Salón principal', activa: true, estadoOp: 'Ocupada', atencion: 'A-105', mesero: 'Pedro Ramos', tiempo: '12 min' },
    { id: 4, nombre: 'Mesa 4', zonaId: 1, zona: 'Salón principal', activa: true, estadoOp: 'Libre', atencion: null, mesero: null, tiempo: null },
    { id: 5, nombre: 'Mesa 5', zonaId: 2, zona: 'Terraza', activa: true, estadoOp: 'Ocupada', atencion: 'A-106', mesero: 'Carlos Quispe', tiempo: '41 min' },
    { id: 6, nombre: 'Mesa 6', zonaId: 2, zona: 'Terraza', activa: true, estadoOp: 'Libre', atencion: null, mesero: null, tiempo: null },
    { id: 7, nombre: 'Para llevar', zonaId: 1, zona: 'Salón principal', activa: true, estadoOp: 'Ocupada', atencion: 'L-12', mesero: 'Pedro Ramos', tiempo: '8 min' },
    { id: 8, nombre: 'Mesa 8', zonaId: 3, zona: 'Segundo piso', activa: false, estadoOp: 'Libre', atencion: null, mesero: null, tiempo: null },
  ];
}

export function pedidosIniciales(): PedidoDemo[] {
  return [
    { id: 'P-221', origen: 'Mesa 1', estado: 'En curso', hora: '16:18', responsable: 'Carlos Quispe', cocina: 'En preparación' },
    { id: 'P-222', origen: 'Mesa 3', estado: 'Pendiente cocina', hora: '16:24', responsable: 'Pedro Ramos', cocina: 'Pendiente' },
    { id: 'P-223', origen: 'Mesa 5', estado: 'Preparado', hora: '16:10', responsable: 'Carlos Quispe', cocina: 'Listo' },
    { id: 'P-224', origen: 'Para llevar', estado: 'Pendiente cocina', hora: '16:31', responsable: 'Pedro Ramos', cocina: 'Pendiente' },
    { id: 'P-225', origen: 'Mesa 1', estado: 'Modificado', hora: '16:22', responsable: 'Carlos Quispe', cocina: 'Cambio anotado' },
    { id: 'P-220', origen: 'Mesa 2', estado: 'Cancelado', hora: '16:08', responsable: 'Carlos Quispe', cocina: 'Cancelado' },
  ];
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
  return [
    { id: 1, fecha: '06/10/2026 17:40', insumo: 'Pollo', tipo: 'Entrada', cantidad: '25 kg', motivo: 'Recepción C-1040', responsable: 'Rosa Huamán' },
    { id: 2, fecha: '07/10/2026 12:10', insumo: 'Papa', tipo: 'Salida', cantidad: '6 kg', motivo: 'Consumo receta', responsable: 'Luis Paredes' },
    { id: 3, fecha: '07/10/2026 13:02', insumo: 'Ensalada', tipo: 'Salida', cantidad: '10 porc.', motivo: 'Consumo receta', responsable: 'Luis Paredes' },
    { id: 4, fecha: '07/10/2026 16:20', insumo: 'Gaseosa 500 ml', tipo: 'Salida', cantidad: '8 unid.', motivo: 'Venta salón', responsable: 'Sistema' },
  ];
}

export function recetasIniciales(): RecetaDemo[] {
  return [
    {
      id: 1,
      producto: 'Pollo a la brasa',
      activa: true,
      insumos: [
        { insumo: 'Pollo', cantidad: '1/4 und' },
        { insumo: 'Papa', cantidad: '0.25 kg' },
        { insumo: 'Ensalada', cantidad: '1 porción' },
      ],
    },
    {
      id: 2,
      producto: '1/2 Pollo a la brasa',
      activa: true,
      insumos: [
        { insumo: 'Pollo', cantidad: '1/2 und' },
        { insumo: 'Papa', cantidad: '0.40 kg' },
        { insumo: 'Ensalada', cantidad: '1 porción' },
      ],
    },
    {
      id: 3,
      producto: 'Lomo saltado',
      activa: true,
      insumos: [
        { insumo: 'Papa', cantidad: '0.20 kg' },
        { insumo: 'Arroz', cantidad: '0.15 kg' },
        { insumo: 'Aceite', cantidad: '0.03 L' },
      ],
    },
  ];
}

export function productosIniciales(): ProductoDemo[] {
  return [
    { id: 1, nombre: 'Pollo a la brasa', categoria: 'Platos', precio: 'S/ 22.00', activo: true, unidades: ['Restaurante', 'Pollería'] },
    { id: 2, nombre: '1/2 Pollo a la brasa', categoria: 'Platos', precio: 'S/ 38.00', activo: true, unidades: ['Pollería'] },
    { id: 3, nombre: 'Lomo saltado', categoria: 'Platos', precio: 'S/ 28.00', activo: true, unidades: ['Restaurante'] },
    { id: 4, nombre: 'Ensalada criolla', categoria: 'Acompañamientos', precio: 'S/ 6.00', activo: true, unidades: ['Restaurante', 'Pollería'] },
    { id: 5, nombre: 'Gaseosa 500 ml', categoria: 'Bebidas', precio: 'S/ 4.00', activo: true, unidades: ['Restaurante', 'Pollería'] },
    { id: 6, nombre: 'Chicha morada', categoria: 'Bebidas', precio: 'S/ 5.00', activo: false, unidades: ['Restaurante'] },
  ];
}

export function cartaInicial(): ItemCartaDemo[] {
  return [
    { id: 1, productoId: 2, producto: '1/2 Pollo a la brasa', unidad: 'Pollería', precio: 'S/ 38.00', disponibilidad: 'Disponible' },
    { id: 2, productoId: 1, producto: 'Pollo a la brasa', unidad: 'Pollería', precio: 'S/ 22.00', disponibilidad: 'Disponible' },
    { id: 3, productoId: 4, producto: 'Ensalada criolla', unidad: 'Pollería', precio: 'S/ 6.00', disponibilidad: 'Agotado' },
    { id: 4, productoId: 5, producto: 'Gaseosa 500 ml', unidad: 'Pollería', precio: 'S/ 4.00', disponibilidad: 'Disponible' },
    { id: 5, productoId: 3, producto: 'Lomo saltado', unidad: 'Restaurante', precio: 'S/ 28.00', disponibilidad: 'Disponible' },
    { id: 6, productoId: 1, producto: 'Pollo a la brasa', unidad: 'Restaurante', precio: 'S/ 22.00', disponibilidad: 'Disponible' },
  ];
}
