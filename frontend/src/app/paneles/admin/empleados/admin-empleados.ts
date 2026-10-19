import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { mensajeDeError } from '../../../core/errores/api-error';
import {
  Cargo,
  Empleado,
  EmpleadoRequest,
  FiltroEstadoPersonal,
  claseEstadoDe,
  coincideFiltroEstado,
  compararInactivos,
  etiquetaEstadoDe,
  formatearFechaHora,
  nombreCompletoDe,
} from '../../../personal/personal.models';
import { EmpleadosService } from '../../../personal/empleados.service';
import { ordenarCargos } from '../admin.catalogos';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { EmpleadoFormulario } from './empleado-formulario';

type DialogoEmpleado = 'crear' | 'editar' | 'ver' | 'eliminar' | null;

@Component({
  selector: 'app-admin-empleados',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo, EmpleadoFormulario],
  templateUrl: './admin-empleados.html',
})
export class AdminEmpleados {
  private readonly api = inject(EmpleadosService);

  protected readonly cargos = signal<Cargo[]>([]);
  protected readonly empleados = signal<Empleado[]>([]);
  protected readonly busqueda = signal('');
  protected readonly cargo = signal('');
  protected readonly estado = signal<FiltroEstadoPersonal>('todos');
  protected readonly dialogo = signal<DialogoEmpleado>(null);
  protected readonly seleccionado = signal<Empleado | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected readonly estados: readonly OpcionSegmento<FiltroEstadoPersonal>[] = [
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
    { titulo: 'Tiene usuario' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const cargo = this.cargo();
    const estado = this.estado();
    const lista = this.empleados().filter((empleado) => {
      const coincideTexto =
        !texto ||
        nombreCompletoDe(empleado).toLowerCase().includes(texto) ||
        (empleado.dni ?? '').includes(texto);
      const coincideCargo = !cargo || empleado.cargo.nombre === cargo;
      return coincideTexto && coincideCargo && coincideFiltroEstado(empleado, estado);
    });
    if (estado === 'inactivos') {
      return [...lista].sort(
        (a, b) => compararInactivos(a, b) || nombreCompletoDe(a).localeCompare(nombreCompletoDe(b), 'es'),
      );
    }
    return lista;
  });

  protected readonly vacioPorFiltro = computed(
    () => this.empleados().length > 0 && this.filtrados().length === 0,
  );

  constructor() {
    this.recargar();
  }

  protected nombreDe = nombreCompletoDe;
  protected etiquetaEstado = etiquetaEstadoDe;
  protected claseEstado = claseEstadoDe;
  protected fechaDe = formatearFechaHora;

  protected recargar(): void {
    this.api.listarCargos().subscribe({
      next: (cargos) => this.cargos.set(ordenarCargos(cargos)),
      error: (error) => this.aviso.set(mensajeDeError(error)),
    });
    this.api.listar().subscribe({
      next: (lista) => this.empleados.set(lista),
      error: (error) => this.aviso.set(mensajeDeError(error, 'No se pudo cargar el listado de empleados.')),
    });
  }

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

  protected abrirEditar(empleado: Empleado): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(empleado);
    this.dialogo.set('editar');
  }

  protected abrirVer(empleado: Empleado): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(empleado);
    this.dialogo.set('ver');
  }

  protected abrirEliminar(empleado: Empleado): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(empleado);
    this.dialogo.set('eliminar');
  }

  protected cerrarDialogo(): void {
    this.dialogo.set(null);
    this.seleccionado.set(null);
    this.errorFormulario.set(null);
  }

  protected guardar(alta: EmpleadoRequest): void {
    const editando = this.seleccionado();
    this.enviando.set(true);
    this.errorFormulario.set(null);
    const peticion = editando ? this.api.actualizar(editando.id, alta) : this.api.crear(alta);
    peticion.subscribe({
      next: () => {
        this.enviando.set(false);
        this.aviso.set(editando ? 'Empleado actualizado.' : 'Empleado registrado.');
        this.cerrarDialogo();
        this.recargar();
      },
      error: (error) => {
        this.enviando.set(false);
        this.errorFormulario.set(mensajeDeError(error, 'No se pudo guardar el empleado.'));
      },
    });
  }

  protected confirmarEliminar(): void {
    const empleado = this.seleccionado();
    if (!empleado) {
      return;
    }
    this.api.archivar(empleado.id).subscribe({
      next: () => {
        this.aviso.set('Empleado archivado. Su información e historial se conservan.');
        this.cerrarDialogo();
        this.recargar();
      },
      error: (error) => this.errorFormulario.set(mensajeDeError(error, 'No se pudo eliminar el empleado.')),
    });
  }
}
