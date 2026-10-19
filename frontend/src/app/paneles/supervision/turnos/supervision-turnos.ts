import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { interval } from 'rxjs';
import { ROL_ETIQUETA } from '../../../auth/auth.models';
import { AuthService } from '../../../auth/auth.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { TurnosService } from '../../../turnos/turnos.service';
import {
  FUNCIONES_ADMINISTRADOR,
  HorarioBase,
  PersonalTurno,
  ROLES_TEMPORALES,
  RolTemporal,
  claseRolTemporal,
  coincidePersonalTurno,
  esAsignacionTemporal,
  etiquetaFuncionActual,
  marcadorTurno,
  minutosHastaFin,
} from '../datos/supervision.models';

type DialogoTurnos = 'horario' | 'asignar' | 'finalizar' | 'expulsar' | null;
type FiltroFuncion = RolTemporal | '';

@Component({
  selector: 'app-supervision-turnos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-turnos.html',
})
export class SupervisionTurnos {
  private readonly turnosApi = inject(TurnosService);
  private readonly auth = inject(AuthService);
  private readonly ahora = signal(Date.now());

  constructor() {
    interval(1000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.ahora.set(Date.now()));
    this.refrescar();
    interval(30_000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.refrescar());
  }

  protected readonly horarios = this.turnosApi.horarios;
  protected readonly turno = this.turnosApi.turno;
  protected readonly personal = this.turnosApi.personal;
  protected readonly error = signal<string | null>(null);
  protected readonly etiquetaRol = ROL_ETIQUETA;
  protected readonly etiquetaFuncion = etiquetaFuncionActual;
  protected readonly claseTemporal = claseRolTemporal;
  protected readonly esTemporal = esAsignacionTemporal;

  protected readonly busqueda = signal('');
  protected readonly filtroFuncion = signal<FiltroFuncion>('');
  protected readonly dialogo = signal<DialogoTurnos>(null);
  protected readonly horarioEditando = signal<HorarioBase | null>(null);
  protected readonly horaInicio = signal('');
  protected readonly horaFin = signal('');
  protected readonly seleccionado = signal<PersonalTurno | null>(null);
  protected readonly rolElegido = signal<RolTemporal>('MESERO');

  protected readonly filtrosFuncion: readonly OpcionSegmento<FiltroFuncion>[] = [
    { valor: '', etiqueta: 'Todos' },
    { valor: 'SUPERVISOR', etiqueta: 'Supervisor' },
    { valor: 'MESERO', etiqueta: 'Mesero' },
    { valor: 'COCINA', etiqueta: 'Cocina' },
    { valor: 'CAJA', etiqueta: 'Caja' },
  ];

  protected readonly esAdministrador = computed(() => this.auth.usuario()?.rol === 'ADMINISTRADOR');

  protected readonly funcionesAsignables = computed(() =>
    this.esAdministrador() ? FUNCIONES_ADMINISTRADOR : ROLES_TEMPORALES,
  );

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Empleado' },
    { titulo: 'Usuario' },
    { titulo: 'Rol permanente' },
    { titulo: 'Función actual' },
    { titulo: 'Ingreso' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly filtrados = computed(() =>
    this.personal().filter((p) => coincidePersonalTurno(p, this.busqueda(), this.filtroFuncion())),
  );

  protected readonly vacioPorFiltro = computed(() => this.personal().length > 0 && this.filtrados().length === 0);

  protected readonly responsable = computed(() => this.turno().responsable || '—');

  protected readonly cerradoPor = computed(() => this.turno().cerradoPor || '—');

  protected readonly tituloTurno = computed(() => {
    const t = this.turno();
    return t.estado === 'CERRADO' ? `Próximo turno ${t.unidad}` : t.unidad;
  });

  protected readonly horarioMostrado = computed(() => {
    const t = this.turno();
    return { inicio: t.horaInicio, fin: t.horaFin };
  });

  protected readonly marcador = computed(() => {
    const t = this.turno();
    return marcadorTurno(t.estado, minutosHastaFin(this.horarioMostrado().fin, new Date(this.ahora())));
  });

  protected readonly opcionesTemporales = computed(() => {
    const persona = this.seleccionado();
    const disponibles = this.funcionesAsignables();
    if (!persona) {
      return disponibles;
    }
    const actual = persona.funcionActual.toUpperCase();
    return disponibles.filter((rol) => rol !== actual);
  });

  protected puedeGestionar(persona: PersonalTurno): boolean {
    return this.esAdministrador() || persona.rolPermanente !== 'ADMINISTRADOR';
  }

  protected actualizarBusqueda(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected cambiarFiltro(funcion: FiltroFuncion): void {
    this.filtroFuncion.set(funcion);
  }

  protected abrirHorario(horario: HorarioBase): void {
    this.error.set(null);
    this.horarioEditando.set(horario);
    this.horaInicio.set(horario.horaInicio);
    this.horaFin.set(horario.horaFin);
    this.dialogo.set('horario');
  }

  protected guardarHorario(): void {
    const horario = this.horarioEditando();
    const inicio = this.horaInicio();
    const fin = this.horaFin();
    if (!horario || !inicio || !fin) {
      return;
    }
    this.error.set(null);
    this.turnosApi.actualizarHorario(horario.unidad, inicio, fin).subscribe({
      next: () => {
        this.turnosApi.cargarTurno().subscribe({ error: () => undefined });
        this.cerrar();
      },
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo guardar el horario.')),
    });
  }

  protected iniciarTurno(): void {
    this.error.set(null);
    this.turnosApi.iniciar(this.turno().unidad).subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo iniciar el turno.')),
    });
  }

  protected prepararCierre(): void {
    const id = this.turno().id;
    if (id == null) {
      return;
    }
    this.error.set(null);
    this.turnosApi.prepararCierre(id).subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo preparar el cierre.')),
    });
  }

  protected pedirFinalizar(): void {
    this.error.set(null);
    this.dialogo.set('finalizar');
  }

  protected confirmarFinalizar(): void {
    const id = this.turno().id;
    if (id == null) {
      return;
    }
    this.error.set(null);
    this.turnosApi.cerrar(id).subscribe({
      next: () => {
        this.turnosApi.cargarPersonal().subscribe({ error: () => undefined });
        this.cerrar();
      },
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo finalizar el turno.')),
    });
  }

  protected abrirAsignar(persona: PersonalTurno): void {
    this.error.set(null);
    this.seleccionado.set(persona);
    const actual = persona.funcionActual.toUpperCase();
    const opciones = this.funcionesAsignables().filter((rol) => rol !== actual);
    this.rolElegido.set(opciones[0] ?? 'MESERO');
    this.dialogo.set('asignar');
  }

  protected confirmarAsignar(): void {
    const persona = this.seleccionado();
    if (!persona) {
      return;
    }
    this.error.set(null);
    this.turnosApi.asignarFuncion(persona.id, this.rolElegido()).subscribe({
      next: () => this.cerrar(),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo asignar la función.')),
    });
  }

  protected volverPermanente(): void {
    const persona = this.seleccionado();
    if (!persona) {
      return;
    }
    this.error.set(null);
    this.turnosApi.asignarFuncion(persona.id, null).subscribe({
      next: () => this.cerrar(),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo volver a la función permanente.')),
    });
  }

  protected pedirExpulsar(persona: PersonalTurno): void {
    this.error.set(null);
    this.seleccionado.set(persona);
    this.dialogo.set('expulsar');
  }

  protected confirmarExpulsar(): void {
    const persona = this.seleccionado();
    if (!persona) {
      return;
    }
    this.error.set(null);
    this.turnosApi.expulsar(persona.id).subscribe({
      next: () => this.cerrar(),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo quitar a esa persona del turno.')),
    });
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.horarioEditando.set(null);
    this.seleccionado.set(null);
  }

  private refrescar(): void {
    this.turnosApi.cargarHorarios().subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar los horarios.')),
    });
    this.turnosApi.cargarTurno().subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cargar el turno.')),
    });
    this.turnosApi.cargarPersonal().subscribe({
      error: () => undefined,
    });
  }
}
