import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { CARGOS } from '../admin.catalogos';

type FiltroEstado = 'todos' | 'activos' | 'inactivos';

/**
 * Empleados de La Ramadita (trabajadores, tengan o no usuario en el sistema).
 * Pantalla visual: buscador, filtros, tabla y estado vacío. El registro real
 * llegará con el módulo correspondiente; aquí no se persiste nada.
 */
@Component({
  selector: 'app-admin-empleados',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado],
  templateUrl: './admin-empleados.html',
})
export class AdminEmpleados {
  protected readonly cargos = CARGOS;
  protected readonly busqueda = signal('');
  protected readonly cargo = signal('');
  protected readonly estado = signal<FiltroEstado>('todos');

  protected readonly estados: readonly OpcionSegmento<FiltroEstado>[] = [
    { valor: 'todos', etiqueta: 'Todos' },
    { valor: 'activos', etiqueta: 'Activos' },
    { valor: 'inactivos', etiqueta: 'Inactivos' },
  ];

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Nombre completo' },
    { titulo: 'DNI' },
    { titulo: 'Cargo' },
    { titulo: 'Teléfono' },
    { titulo: 'Correo personal' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  /** Todavía no hay persistencia: el registro queda preparado solo visualmente. */
  protected readonly avisoRegistro = signal(false);

  protected registrar(): void {
    this.avisoRegistro.set(true);
  }

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarCargo(evento: Event): void {
    this.cargo.set((evento.target as HTMLSelectElement).value);
  }
}
