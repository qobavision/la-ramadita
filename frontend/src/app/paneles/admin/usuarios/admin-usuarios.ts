import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';

type FiltroEstado = 'todos' | 'activos' | 'inactivos';

/**
 * Usuarios y accesos: cuentas que pueden iniciar sesión. Cada usuario tiene
 * un único rol permanente y solo el ADMINISTRADOR crea cuentas o cambia roles.
 * Pantalla visual; el listado y el formulario se conectarán a M01 después.
 */
@Component({
  selector: 'app-admin-usuarios',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado],
  templateUrl: './admin-usuarios.html',
})
export class AdminUsuarios {
  protected readonly roles = Object.entries(ROL_ETIQUETA) as [Rol, string][];
  protected readonly busqueda = signal('');
  protected readonly rol = signal('');
  protected readonly estado = signal<FiltroEstado>('todos');

  protected readonly estados: readonly OpcionSegmento<FiltroEstado>[] = [
    { valor: 'todos', etiqueta: 'Todos' },
    { valor: 'activos', etiqueta: 'Activos' },
    { valor: 'inactivos', etiqueta: 'Inactivos' },
  ];

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Empleado' },
    { titulo: 'Username' },
    { titulo: 'Rol permanente' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly avisoCreacion = signal(false);

  protected crear(): void {
    this.avisoCreacion.set(true);
  }

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarRol(evento: Event): void {
    this.rol.set((evento.target as HTMLSelectElement).value);
  }
}
