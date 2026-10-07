import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { CARGOS } from '../admin.catalogos';
import { EmpleadoConAcceso, nombreCompletoDe } from '../personal/personal.models';
import { AltaEmpleado, PersonalVisualStore } from '../personal/personal.store';
import { EmpleadoFormulario } from './empleado-formulario';

type FiltroEstado = 'todos' | 'activos' | 'inactivos';
type DialogoEmpleado = 'crear' | 'editar' | 'ver' | null;

@Component({
  selector: 'app-admin-empleados',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo, EmpleadoFormulario],
  templateUrl: './admin-empleados.html',
})
export class AdminEmpleados {
  private readonly store = inject(PersonalVisualStore);
  private readonly router = inject(Router);

  protected readonly cargos = CARGOS;
  protected readonly busqueda = signal('');
  protected readonly cargo = signal('');
  protected readonly estado = signal<FiltroEstado>('todos');
  protected readonly dialogo = signal<DialogoEmpleado>(null);
  protected readonly seleccionado = signal<EmpleadoConAcceso | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);

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
    { titulo: 'Acceso al sistema' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const cargo = this.cargo();
    const estado = this.estado();
    return this.store.empleadosConAcceso().filter((empleado) => {
      const coincideTexto =
        !texto ||
        nombreCompletoDe(empleado).toLowerCase().includes(texto) ||
        empleado.dni.includes(texto);
      const coincideCargo = !cargo || empleado.cargo === cargo;
      const coincideEstado =
        estado === 'todos' || (estado === 'activos' ? empleado.activo : !empleado.activo);
      return coincideTexto && coincideCargo && coincideEstado;
    });
  });

  protected readonly vacioPorFiltro = computed(
    () => this.store.empleados().length > 0 && this.filtrados().length === 0,
  );

  protected nombreDe = nombreCompletoDe;

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarCargo(evento: Event): void {
    this.cargo.set((evento.target as HTMLSelectElement).value);
  }

  protected abrirCrear(): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(null);
    this.dialogo.set('crear');
  }

  protected abrirVer(empleado: EmpleadoConAcceso): void {
    this.seleccionado.set(empleado);
    this.dialogo.set('ver');
  }

  protected abrirEditar(empleado: EmpleadoConAcceso): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(empleado);
    this.dialogo.set('editar');
  }

  protected cerrarDialogo(): void {
    this.dialogo.set(null);
    this.seleccionado.set(null);
    this.errorFormulario.set(null);
  }

  protected guardar(alta: AltaEmpleado): void {
    const editando = this.seleccionado();
    if (this.store.dniEnUso(alta.dni, editando?.id)) {
      this.errorFormulario.set('Ya existe un empleado con ese DNI en esta sesión.');
      return;
    }
    if (editando) {
      this.store.actualizarEmpleado(editando.id, alta);
      this.aviso.set('Cambios aplicados en esta sesión. Todavía no se guardan en el servidor.');
    } else {
      this.store.registrarEmpleado(alta);
      this.aviso.set('Empleado registrado en esta sesión. Todavía no se guarda en el servidor.');
    }
    this.cerrarDialogo();
  }

  protected cambiarEstado(empleado: EmpleadoConAcceso): void {
    this.store.cambiarEstadoEmpleado(empleado.id, !empleado.activo);
    this.aviso.set(
      empleado.activo
        ? 'Empleado marcado como inactivo (no trabaja actualmente). No afecta por sí solo el acceso al sistema.'
        : 'Empleado marcado como activo (trabaja actualmente).',
    );
    const actual = this.seleccionado();
    if (actual?.id === empleado.id) {
      this.seleccionado.set({ ...actual, activo: !empleado.activo });
    }
  }

  protected crearAcceso(empleado: EmpleadoConAcceso): void {
    void this.router.navigate(['/admin/usuarios'], {
      queryParams: { crear: '1', empleado: empleado.id },
    });
  }

  protected verAcceso(empleado: EmpleadoConAcceso): void {
    if (!empleado.acceso) {
      return;
    }
    void this.router.navigate(['/admin/usuarios'], {
      queryParams: { ver: empleado.acceso.id },
    });
  }
}
