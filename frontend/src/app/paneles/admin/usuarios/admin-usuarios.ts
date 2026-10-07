import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { mensajeDeError } from '../../../core/errores/api-error';
import {
  Empleado,
  FiltroEstadoPersonal,
  UsuarioAdmin,
  claseEstadoDe,
  coincideFiltroEstado,
  compararInactivos,
  etiquetaEstadoDe,
  formatearUltimoAcceso,
  nombreCompletoDe,
} from '../../../personal/personal.models';
import { EmpleadosService } from '../../../personal/empleados.service';
import { UsuariosAdminService } from '../../../personal/usuarios-admin.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { AltaAccesoFormulario, UsuarioFormulario } from './usuario-formulario';

type DialogoUsuario = 'crear' | 'editar' | 'clave' | 'eliminar' | null;

@Component({
  selector: 'app-admin-usuarios',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo, UsuarioFormulario],
  templateUrl: './admin-usuarios.html',
})
export class AdminUsuarios {
  private readonly usuariosApi = inject(UsuariosAdminService);
  private readonly empleadosApi = inject(EmpleadosService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly params = toSignal(this.route.queryParamMap, {
    initialValue: this.route.snapshot.queryParamMap,
  });
  private pendientesConsumidos = false;
  private readonly datosListos = signal(false);

  protected readonly roles = Object.entries(ROL_ETIQUETA) as [Rol, string][];
  protected readonly usuarios = signal<UsuarioAdmin[]>([]);
  protected readonly empleados = signal<Empleado[]>([]);
  protected readonly busqueda = signal('');
  protected readonly rol = signal('');
  protected readonly estado = signal<FiltroEstadoPersonal>('todos');
  protected readonly dialogo = signal<DialogoUsuario>(null);
  protected readonly seleccionado = signal<UsuarioAdmin | null>(null);
  protected readonly empleadoFijoId = signal<number | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);

  protected readonly estados: readonly OpcionSegmento<FiltroEstadoPersonal>[] = [
    { valor: 'todos', etiqueta: 'Todos' },
    { valor: 'activos', etiqueta: 'Activos' },
    { valor: 'inactivos', etiqueta: 'Inactivos' },
  ];

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Empleado' },
    { titulo: 'Username' },
    { titulo: 'Rol permanente' },
    { titulo: 'Estado' },
    { titulo: 'Último acceso' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly empleadosDisponibles = computed(() => {
    const fijo = this.empleadoFijoId();
    return this.empleados().filter(
      (empleado) =>
        empleado.id === fijo || (empleado.activo && !empleado.archivado && !empleado.usuario),
    );
  });

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const rol = this.rol();
    const estado = this.estado();
    const lista = this.usuarios().filter((acceso) => {
      const nombre = acceso.empleado ? nombreCompletoDe(acceso.empleado) : '';
      const coincideTexto =
        !texto ||
        nombre.toLowerCase().includes(texto) ||
        acceso.username.toLowerCase().includes(texto);
      const coincideRol = !rol || acceso.rol === rol;
      return coincideTexto && coincideRol && coincideFiltroEstado(acceso, estado);
    });
    if (estado === 'inactivos') {
      return [...lista].sort(
        (a, b) => compararInactivos(a, b) || a.username.localeCompare(b.username, 'es'),
      );
    }
    return lista;
  });

  protected readonly vacioPorFiltro = computed(
    () => this.usuarios().length > 0 && this.filtrados().length === 0,
  );

  constructor() {
    this.recargar();
    effect(() => {
      const params = this.params();
      if (this.pendientesConsumidos || !this.datosListos()) {
        return;
      }
      const crear = params.get('crear');
      const empleado = params.get('empleado');
      if (crear === '1') {
        this.pendientesConsumidos = true;
        this.abrirCrear(empleado ? Number(empleado) : null);
        void this.limpiarParams();
      }
    });
  }

  protected nombreDe = nombreCompletoDe;
  protected etiquetaEstado = etiquetaEstadoDe;
  protected claseEstado = claseEstadoDe;
  protected ultimoAccesoDe = formatearUltimoAcceso;
  protected etiquetaRol(rol: Rol): string {
    return ROL_ETIQUETA[rol];
  }

  protected recargar(): void {
    forkJoin({
      empleados: this.empleadosApi.listar(),
      usuarios: this.usuariosApi.listar(),
    }).subscribe({
      next: ({ empleados, usuarios }) => {
        this.empleados.set(empleados);
        this.usuarios.set(usuarios);
        this.datosListos.set(true);
      },
      error: (error) => this.aviso.set(mensajeDeError(error, 'No se pudo cargar empleados y usuarios.')),
    });
  }

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected actualizarRol(evento: Event): void {
    this.rol.set((evento.target as HTMLSelectElement).value);
  }

  protected abrirCrear(empleadoId: number | null = null): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(null);
    this.empleadoFijoId.set(empleadoId);
    this.dialogo.set('crear');
  }

  protected abrirEditar(acceso: UsuarioAdmin): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(acceso);
    this.empleadoFijoId.set(acceso.empleadoId);
    this.dialogo.set('editar');
  }

  protected abrirEliminar(acceso: UsuarioAdmin): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(acceso);
    this.dialogo.set('eliminar');
  }

  protected abrirClave(acceso: UsuarioAdmin): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(acceso);
    this.empleadoFijoId.set(acceso.empleadoId);
    this.dialogo.set('clave');
  }

  protected cerrarDialogo(): void {
    this.dialogo.set(null);
    this.seleccionado.set(null);
    this.empleadoFijoId.set(null);
    this.errorFormulario.set(null);
  }

  protected guardar(alta: AltaAccesoFormulario): void {
    if (this.dialogo() === 'clave') {
      const actual = this.seleccionado();
      if (!actual) {
        return;
      }
      this.usuariosApi.restablecerPassword(actual.id, alta.password).subscribe({
        next: () => {
          this.aviso.set('Contraseña actualizada.');
          this.cerrarDialogo();
        },
        error: (error) => this.errorFormulario.set(mensajeDeError(error, 'No se pudo restablecer la contraseña.')),
      });
      return;
    }

    const editando = this.seleccionado();
    if (editando) {
      this.usuariosApi
        .actualizar(editando.id, {
          username: alta.username,
          rol: alta.rol,
          activo: alta.activo,
        })
        .subscribe({
          next: () => {
            this.aviso.set('Usuario actualizado.');
            this.cerrarDialogo();
            this.recargar();
          },
          error: (error) => this.errorFormulario.set(mensajeDeError(error, 'No se pudo guardar el usuario.')),
        });
      return;
    }

    this.usuariosApi
      .crear({
        empleadoId: alta.empleadoId,
        username: alta.username,
        password: alta.password,
        rol: alta.rol,
        activo: alta.activo,
      })
      .subscribe({
        next: () => {
          this.aviso.set('Usuario creado. Ya puede iniciar sesión con ese username.');
          this.cerrarDialogo();
          this.recargar();
        },
        error: (error) => this.errorFormulario.set(mensajeDeError(error, 'No se pudo crear el usuario.')),
      });
  }

  protected confirmarEliminar(): void {
    const acceso = this.seleccionado();
    if (!acceso) {
      return;
    }
    this.usuariosApi.archivar(acceso.id).subscribe({
      next: () => {
        this.aviso.set('Usuario archivado. No podrá iniciar sesión y su historial se conserva.');
        this.cerrarDialogo();
        this.recargar();
      },
      error: (error) => this.errorFormulario.set(mensajeDeError(error, 'No se pudo eliminar el usuario.')),
    });
  }

  private limpiarParams(): Promise<boolean> {
    return this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
  }
}
