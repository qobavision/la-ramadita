import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { AccesoConEmpleado, nombreCompletoDe } from '../personal/personal.models';
import { PersonalVisualStore } from '../personal/personal.store';
import { AltaAccesoFormulario, UsuarioFormulario } from './usuario-formulario';

type FiltroEstado = 'todos' | 'activos' | 'inactivos';
type DialogoUsuario = 'crear' | 'editar' | 'ver' | 'clave' | null;

@Component({
  selector: 'app-admin-usuarios',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo, UsuarioFormulario],
  templateUrl: './admin-usuarios.html',
})
export class AdminUsuarios {
  private readonly store = inject(PersonalVisualStore);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly params = toSignal(this.route.queryParamMap, {
    initialValue: this.route.snapshot.queryParamMap,
  });

  protected readonly roles = Object.entries(ROL_ETIQUETA) as [Rol, string][];
  protected readonly busqueda = signal('');
  protected readonly rol = signal('');
  protected readonly estado = signal<FiltroEstado>('todos');
  protected readonly dialogo = signal<DialogoUsuario>(null);
  protected readonly seleccionado = signal<AccesoConEmpleado | null>(null);
  protected readonly empleadoFijoId = signal<number | null>(null);
  protected readonly errorFormulario = signal<string | null>(null);
  protected readonly aviso = signal<string | null>(null);

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
    { titulo: 'Último acceso' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly empleadosDisponibles = computed(() =>
    this.store.empleadosSinAcceso(this.empleadoFijoId() ?? undefined),
  );

  protected readonly filtrados = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const rol = this.rol();
    const estado = this.estado();
    return this.store.accesosConEmpleado().filter((acceso) => {
      const nombre = acceso.empleado ? nombreCompletoDe(acceso.empleado) : '';
      const coincideTexto =
        !texto ||
        nombre.toLowerCase().includes(texto) ||
        acceso.username.toLowerCase().includes(texto);
      const coincideRol = !rol || acceso.rol === rol;
      const coincideEstado =
        estado === 'todos' || (estado === 'activos' ? acceso.activo : !acceso.activo);
      return coincideTexto && coincideRol && coincideEstado;
    });
  });

  protected readonly vacioPorFiltro = computed(
    () => this.store.accesos().length > 0 && this.filtrados().length === 0,
  );

  constructor() {
    effect(() => {
      const params = this.params();
      const crear = params.get('crear');
      const empleado = params.get('empleado');
      const ver = params.get('ver');
      if (crear === '1') {
        this.abrirCrear(empleado ? Number(empleado) : null);
        void this.limpiarParams();
      } else if (ver) {
        this.abrirVerPorId(Number(ver));
        void this.limpiarParams();
      }
    });
  }

  protected nombreDe = nombreCompletoDe;
  protected etiquetaRol(rol: Rol): string {
    return ROL_ETIQUETA[rol];
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

  protected abrirVer(acceso: AccesoConEmpleado): void {
    this.seleccionado.set(acceso);
    this.dialogo.set('ver');
  }

  protected abrirEditar(acceso: AccesoConEmpleado): void {
    this.errorFormulario.set(null);
    this.seleccionado.set(acceso);
    this.empleadoFijoId.set(acceso.empleadoId);
    this.dialogo.set('editar');
  }

  protected abrirClave(acceso: AccesoConEmpleado): void {
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
      this.aviso.set(
        `Contraseña preparada para ${actual.username}. Todavía no se envía al servidor.`,
      );
      this.cerrarDialogo();
      return;
    }

    const editando = this.seleccionado();
    if (this.store.usernameEnUso(alta.username, editando?.id)) {
      this.errorFormulario.set('Ese username ya está en uso en esta sesión.');
      return;
    }

    if (editando) {
      this.store.actualizarAcceso(editando.id, {
        username: alta.username,
        rol: alta.rol,
        activo: alta.activo,
      });
      this.aviso.set('Cambios aplicados en esta sesión. Todavía no se guardan en el servidor.');
    } else {
      const creado = this.store.crearAcceso(alta);
      if (!creado) {
        this.errorFormulario.set('Ese empleado ya tiene una cuenta de acceso.');
        return;
      }
      this.aviso.set(
        'Usuario creado en esta sesión. La contraseña temporal no se envía al servidor todavía.',
      );
    }
    this.cerrarDialogo();
  }

  protected cambiarEstado(acceso: AccesoConEmpleado): void {
    this.store.cambiarEstadoAcceso(acceso.id, !acceso.activo);
    this.aviso.set(
      acceso.activo
        ? 'Usuario inactivado: no podrá iniciar sesión.'
        : 'Usuario activado: podrá iniciar sesión.',
    );
    const actual = this.seleccionado();
    if (actual?.id === acceso.id) {
      this.seleccionado.set({ ...actual, activo: !acceso.activo });
    }
  }

  private abrirVerPorId(id: number): void {
    const encontrado = this.store.accesosConEmpleado().find((acceso) => acceso.id === id);
    if (encontrado) {
      this.abrirVer(encontrado);
    }
  }

  private limpiarParams(): Promise<boolean> {
    return this.router.navigate([], { relativeTo: this.route, queryParams: {}, replaceUrl: true });
  }
}
