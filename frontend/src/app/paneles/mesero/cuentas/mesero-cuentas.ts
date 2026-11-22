import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { Atencion, AtencionResumen, LineaPedido, tiempoDesde } from '../../../atencion/atencion.models';
import { AtencionesService } from '../../../atencion/atenciones.service';
import { AuthService } from '../../../auth/auth.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { formatearFechaHora } from '../../../personal/personal.models';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';

type FiltroCuentas = 'POR_PEDIR' | 'EN_CAJA';
type FiltroMesero = 'MIAS' | 'TODAS';

/**
 * Cuentas del mesero (M09): precuenta del consumo enviado a Cocina, pedido de
 * cuenta a Caja y reapertura (RN-07). Las reglas las valida el backend.
 */
@Component({
  selector: 'app-mesero-cuentas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Dialogo, EncabezadoPagina, EstadoVacio, Segmento],
  templateUrl: './mesero-cuentas.html',
})
export class MeseroCuentas {
  private readonly api = inject(AtencionesService);
  private readonly username = inject(AuthService).usuario()?.username;

  protected readonly atenciones = signal<AtencionResumen[]>([]);
  protected readonly cargando = signal(true);
  protected readonly ocupado = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly precuenta = signal<Atencion | null>(null);
  protected readonly filtro = signal<FiltroCuentas>('POR_PEDIR');
  protected readonly mesero = signal<FiltroMesero>('MIAS');
  protected readonly filtros: OpcionSegmento<FiltroCuentas>[] = [
    { valor: 'POR_PEDIR', etiqueta: 'Por pedir' },
    { valor: 'EN_CAJA', etiqueta: 'En caja' },
  ];
  protected readonly filtrosMesero: OpcionSegmento<FiltroMesero>[] = [
    { valor: 'MIAS', etiqueta: 'Mis atenciones' },
    { valor: 'TODAS', etiqueta: 'Todas' },
  ];
  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;
  protected readonly fecha = formatearFechaHora;
  protected readonly ahora = () => new Date().toISOString();

  /** "Por pedir": abiertas con consumo enviado. "En caja": cuenta pedida, pendiente de pago. */
  protected readonly visibles = computed(() =>
    this.atenciones().filter(
      (a) =>
        (this.mesero() === 'TODAS' || a.meseroUsername === this.username) &&
        (this.filtro() === 'EN_CAJA' ? a.estado === 'PENDIENTE_PAGO' : a.estado === 'ABIERTA' && a.total > 0),
    ),
  );

  /** Líneas cobrables de la precuenta: lo enviado a Cocina que no se canceló. */
  protected readonly lineas = computed<LineaPedido[]>(() =>
    (this.precuenta()?.enviados ?? []).flatMap((p) => p.detalles).filter((d) => d.estadoItem === 'ACTIVO'),
  );

  constructor() {
    this.cargar();
    inject(TiempoRealService)
      .escuchar(['salon'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (['SALON_ACTUALIZADO', 'CONECTADO'].includes(evento.tipo)) {
          this.cargar();
        }
      });
  }

  protected verPrecuenta(id: number): void {
    this.error.set(null);
    this.api.obtener(id).subscribe({
      next: (a) => this.precuenta.set(a),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cargar la precuenta.')),
    });
  }

  protected pedirCuenta(id: number): void {
    this.ejecutar(this.api.pedirCuenta(id), 'No se pudo pedir la cuenta.');
  }

  protected reabrir(id: number): void {
    this.ejecutar(this.api.reabrir(id), 'No se pudo reabrir la cuenta.');
  }

  protected imprimir(): void {
    window.print();
  }

  private ejecutar(peticion: Observable<Atencion>, mensaje: string): void {
    this.ocupado.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: () => {
        this.ocupado.set(false);
        this.precuenta.set(null);
        this.cargar();
      },
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, mensaje));
      },
    });
  }

  private cargar(): void {
    this.api.activas().subscribe({
      next: (lista) => {
        this.atenciones.set(lista);
        this.cargando.set(false);
      },
      error: (error) => {
        this.cargando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudieron cargar las cuentas.'));
      },
    });
  }
}
