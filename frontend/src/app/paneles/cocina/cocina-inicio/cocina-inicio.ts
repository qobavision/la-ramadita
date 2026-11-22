import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { EMPTY, Subject, catchError, interval, switchMap } from 'rxjs';
import { horaDe } from '../../../atencion/atencion.models';
import { AlertasSonoras } from '../../../cocina/alertas-sonoras';
import {
  Comanda,
  ETIQUETA_AVISO,
  ETIQUETA_ESTADO_COMANDA,
  LineaComanda,
  MINUTOS_DEMORA,
  TableroCocina,
  TipoAvisoCocina,
  esAvisoCocina,
  minutosEntre,
} from '../../../cocina/cocina.models';
import { CocinaService } from '../../../cocina/cocina.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { EventoTiempoReal, TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';

interface AvisoVisible {
  id: number;
  tipo: TipoAvisoCocina;
  titulo: string;
  texto: string;
}

const SIN_ZONA = 'Para llevar';
const CLAVE_SONIDO = 'la-ramadita.cocina.sonido';
const CLAVE_ZONA = 'la-ramadita.cocina.zona';
const AVISOS_MAXIMOS = 4;
const DURACION_AVISO_MS = 20_000;
const DURACION_DESTACADO_MS = 60_000;

/**
 * Monitor de Cocina (M08): comandas pendientes por orden de llegada y las
 * recién terminadas. Se actualiza por WebSocket (pedido nuevo, modificado o
 * cancelado) y marca productos como Preparados, lo que avisa al mesero.
 */
@Component({
  selector: 'app-cocina-inicio',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, Segmento],
  templateUrl: './cocina-inicio.html',
})
export class CocinaInicio {
  private readonly api = inject(CocinaService);
  private readonly sonidos = new AlertasSonoras();
  private readonly recargas = new Subject<void>();
  private secuenciaAviso = 0;

  protected readonly tablero = signal<TableroCocina>({ pendientes: [], preparados: [] });
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly conectado = signal(false);
  protected readonly ahora = signal(Date.now());
  protected readonly avisos = signal<readonly AvisoVisible[]>([]);
  protected readonly destacados = signal<ReadonlyMap<number, TipoAvisoCocina>>(new Map());
  protected readonly enCurso = signal<ReadonlySet<string>>(new Set());
  protected readonly sonidoActivo = signal(leerPreferencia(CLAVE_SONIDO, 'si') === 'si');
  protected readonly zona = signal(leerPreferencia(CLAVE_ZONA, ''));

  protected readonly demora = MINUTOS_DEMORA;
  protected readonly etiquetaEstado = ETIQUETA_ESTADO_COMANDA;
  protected readonly hora = horaDe;

  protected readonly opcionesZona = computed<readonly OpcionSegmento[]>(() => {
    const { pendientes, preparados } = this.tablero();
    const zonas = new Set([...pendientes, ...preparados].map(zonaDe));
    const elegida = this.zona();
    if (elegida) {
      zonas.add(elegida);
    }
    return [
      { valor: '', etiqueta: 'Todas' },
      ...[...zonas].sort((a, b) => a.localeCompare(b, 'es')).map((z) => ({ valor: z, etiqueta: z })),
    ];
  });

  protected readonly pendientes = computed(() => this.filtrar(this.tablero().pendientes));
  protected readonly preparados = computed(() => this.filtrar(this.tablero().preparados));

  constructor() {
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => this.sonidos.cerrar());

    this.recargas
      .pipe(
        switchMap(() =>
          this.api.tablero().pipe(
            catchError((e: unknown) => {
              this.error.set(mensajeDeError(e, 'No se pudieron cargar los pedidos de cocina.'));
              this.cargando.set(false);
              return EMPTY;
            }),
          ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((t) => {
        this.tablero.set(t);
        this.cargando.set(false);
      });
    this.recargar();

    inject(TiempoRealService)
      .escuchar(['cocina'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => this.alRecibir(evento));

    interval(15_000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.ahora.set(Date.now()));
    // Respaldo por si el WebSocket quedó colgado sin cerrarse.
    interval(60_000)
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.recargar());
  }

  protected espera(c: Comanda): number {
    return minutosEntre(c.enviado, this.ahora());
  }

  protected zonaDe(c: Comanda): string {
    return zonaDe(c);
  }

  protected destacado(c: Comanda): TipoAvisoCocina | undefined {
    return this.destacados().get(c.pedidoId);
  }

  protected ocupado(clave: string): boolean {
    return this.enCurso().has(clave);
  }

  protected lineasPendientes(c: Comanda): number {
    return c.lineas.filter((l) => l.estado === 'PENDIENTE').length;
  }

  protected resumen(c: Comanda): string {
    return c.lineas
      .filter((l) => l.estado !== 'CANCELADO')
      .map((l) => `${l.cantidad} × ${l.producto}`)
      .join(', ');
  }

  protected elegirZona(valor: string | undefined): void {
    this.zona.set(valor ?? '');
    guardarPreferencia(CLAVE_ZONA, valor ?? '');
  }

  protected alternarSonido(): void {
    const activo = !this.sonidoActivo();
    this.sonidoActivo.set(activo);
    guardarPreferencia(CLAVE_SONIDO, activo ? 'si' : 'no');
    if (activo) {
      this.sonidos.activar();
      this.sonidos.reproducir('PEDIDO_NUEVO');
    }
  }

  protected prepararLinea(linea: LineaComanda): void {
    this.ejecutar(`l${linea.id}`, () => this.api.prepararLinea(linea.id));
  }

  protected prepararComanda(c: Comanda): void {
    this.ejecutar(`c${c.pedidoId}`, () => this.api.prepararComanda(c.pedidoId));
  }

  protected descartar(id: number): void {
    this.avisos.update((lista) => lista.filter((a) => a.id !== id));
  }

  private ejecutar(clave: string, llamada: () => ReturnType<CocinaService['prepararLinea']>): void {
    if (this.ocupado(clave)) {
      return;
    }
    if (this.sonidoActivo()) {
      this.sonidos.activar();
    }
    this.error.set(null);
    this.enCurso.update((s) => new Set(s).add(clave));
    llamada().subscribe({
      next: (comanda) => {
        this.aplicar(comanda);
        this.liberar(clave);
      },
      error: (e: unknown) => {
        this.error.set(mensajeDeError(e, 'No se pudo marcar como preparado.'));
        this.liberar(clave);
        this.recargar();
      },
    });
  }

  private liberar(clave: string): void {
    this.enCurso.update((s) => {
      const copia = new Set(s);
      copia.delete(clave);
      return copia;
    });
  }

  /** Refleja al instante la comanda devuelta por el backend. */
  private aplicar(c: Comanda): void {
    this.tablero.update((t) => {
      if (c.estado === 'PENDIENTE') {
        return { ...t, pendientes: t.pendientes.map((x) => (x.pedidoId === c.pedidoId ? c : x)) };
      }
      return {
        pendientes: t.pendientes.filter((x) => x.pedidoId !== c.pedidoId),
        preparados: [c, ...t.preparados.filter((x) => x.pedidoId !== c.pedidoId)],
      };
    });
  }

  private alRecibir(evento: EventoTiempoReal): void {
    switch (evento.tipo) {
      case 'CONECTADO':
        this.conectado.set(true);
        this.recargar();
        return;
      case 'DESCONECTADO':
        this.conectado.set(false);
        return;
      case 'COCINA_ACTUALIZADA':
        this.recargar();
        return;
    }
    if (!esAvisoCocina(evento.tipo)) {
      return;
    }
    const tipo = evento.tipo;
    const identificacion = String(evento['identificacion'] ?? 'Pedido');
    const zona = evento['zona'] ? ` · ${String(evento['zona'])}` : '';
    const detalle = evento['detalle'] ? ` — ${String(evento['detalle'])}` : '';
    this.mostrarAviso(tipo, `${identificacion}${zona}${detalle}`);
    this.destacar(Number(evento['pedidoId']), tipo);
    if (this.sonidoActivo()) {
      this.sonidos.reproducir(tipo);
    }
    this.recargar();
  }

  private mostrarAviso(tipo: TipoAvisoCocina, texto: string): void {
    const aviso: AvisoVisible = { id: ++this.secuenciaAviso, tipo, titulo: ETIQUETA_AVISO[tipo], texto };
    this.avisos.update((lista) => [aviso, ...lista].slice(0, AVISOS_MAXIMOS));
    setTimeout(() => this.descartar(aviso.id), DURACION_AVISO_MS);
  }

  private destacar(pedidoId: number, tipo: TipoAvisoCocina): void {
    if (!Number.isFinite(pedidoId)) {
      return;
    }
    this.destacados.update((m) => new Map(m).set(pedidoId, tipo));
    setTimeout(
      () =>
        this.destacados.update((m) => {
          const copia = new Map(m);
          copia.delete(pedidoId);
          return copia;
        }),
      DURACION_DESTACADO_MS,
    );
  }

  private recargar(): void {
    this.recargas.next();
  }

  private filtrar(lista: readonly Comanda[]): Comanda[] {
    const zona = this.zona();
    return zona ? lista.filter((c) => zonaDe(c) === zona) : [...lista];
  }
}

function zonaDe(c: Comanda): string {
  return c.zona ?? SIN_ZONA;
}

function leerPreferencia(clave: string, porDefecto: string): string {
  try {
    return localStorage.getItem(clave) ?? porDefecto;
  } catch {
    return porDefecto;
  }
}

function guardarPreferencia(clave: string, valor: string): void {
  try {
    localStorage.setItem(clave, valor);
  } catch {
    // Almacenamiento no disponible: la preferencia dura solo esta sesión.
  }
}
