import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import {
  Atencion,
  ETIQUETA_SECCION,
  horaDe,
  LineaPedido,
  ProductoCarta,
  tiempoDesde,
} from '../../../atencion/atencion.models';
import { AtencionesService } from '../../../atencion/atenciones.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { formatoSoles } from '../../supervision/datos/supervision.models';

/**
 * Atención de una mesa o pedido para llevar (M06/M07): carta del día,
 * pedido en borrador, envío a Cocina, consumo acumulado y solicitud de cuenta.
 * Las reglas (agotados, borrador, estados) se validan en el backend.
 */
@Component({
  selector: 'app-mesero-atencion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, EncabezadoPagina, EstadoVacio, Icono, Segmento, Dialogo],
  templateUrl: './mesero-atencion.html',
})
export class MeseroAtencion {
  private readonly api = inject(AtencionesService);
  private readonly router = inject(Router);
  private readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));

  protected readonly atencion = signal<Atencion | null>(null);
  protected readonly carta = signal<ProductoCarta[]>([]);
  protected readonly errorCarta = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly ocupado = signal(false);
  protected readonly busqueda = signal('');
  protected readonly seccion = signal('TODAS');
  protected readonly confirmar = signal<'cuenta' | 'liberar' | null>(null);
  /** Línea ya enviada que el mesero quiere cancelar (RN-06), a la espera de confirmación. */
  protected readonly cancelando = signal<LineaPedido | null>(null);

  protected readonly soles = formatoSoles;
  protected readonly tiempo = tiempoDesde;
  protected readonly hora = horaDe;

  protected readonly abierta = computed(() => this.atencion()?.estado === 'ABIERTA');
  protected readonly borrador = computed(() => this.atencion()?.borrador?.detalles ?? []);
  protected readonly secciones = computed<OpcionSegmento[]>(() => {
    const unicas = [...new Set(this.carta().map((p) => p.seccion))];
    return [
      { valor: 'TODAS', etiqueta: 'Todo' },
      ...unicas.map((s) => ({ valor: s, etiqueta: ETIQUETA_SECCION[s] ?? s })),
    ];
  });
  protected readonly productos = computed(() => {
    const texto = normalizar(this.busqueda());
    const seccion = this.seccion();
    return this.carta().filter(
      (p) => (seccion === 'TODAS' || p.seccion === seccion) && (!texto || normalizar(p.nombre).includes(texto)),
    );
  });

  constructor() {
    this.api.obtener(this.id).subscribe({
      next: (a) => this.atencion.set(a),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cargar la atención.')),
    });
    this.api.carta(this.id).subscribe({
      next: (carta) => this.carta.set(carta.detalles),
      error: (error) => this.errorCarta.set(mensajeDeError(error, 'No se pudo cargar la carta del día.')),
    });
    // RN-13: cuando Cocina marca algo de esta atención como preparado, se refleja sin recargar.
    // Canal salon: también cuando Caja la cobra u otro mesero cambia su estado.
    inject(TiempoRealService)
      .escuchar(['salon'])
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        const deEstaAtencion = Number(evento['atencionId']) === this.id;
        if (['PRODUCTO_PREPARADO', 'SALON_ACTUALIZADO'].includes(evento.tipo) && deEstaAtencion && !this.ocupado()) {
          this.api.obtener(this.id).subscribe({ next: (a) => this.atencion.set(a), error: () => undefined });
        }
      });
  }

  /** RN-07: el cliente pide algo más después de pedir la cuenta. */
  protected reabrir(): void {
    this.ejecutar(this.api.reabrir(this.id));
  }

  /** RN-06: una línea enviada se puede cambiar o cancelar mientras Cocina no la prepare. */
  protected cambiableEnCocina(linea: LineaPedido): boolean {
    return this.abierta() && linea.estadoItem === 'ACTIVO' && linea.estadoCocina === 'PENDIENTE';
  }

  protected confirmarCancelacion(): void {
    const linea = this.cancelando();
    this.cancelando.set(null);
    if (linea) {
      this.ejecutar(this.api.quitar(this.id, linea.id));
    }
  }

  protected buscar(evento: Event): void {
    this.busqueda.set((evento.target as HTMLInputElement).value);
  }

  protected agregar(producto: ProductoCarta): void {
    this.ejecutar(this.api.agregar(this.id, producto.id));
  }

  protected cambiarCantidad(linea: LineaPedido, delta: number): void {
    const cantidad = linea.cantidad + delta;
    this.ejecutar(
      cantidad < 1
        ? this.api.quitar(this.id, linea.id)
        : this.api.actualizar(this.id, linea.id, cantidad, linea.observacion),
    );
  }

  protected cambiarObservacion(linea: LineaPedido, evento: Event): void {
    const valor = (evento.target as HTMLInputElement).value.trim() || null;
    if (valor !== linea.observacion) {
      this.ejecutar(this.api.actualizar(this.id, linea.id, linea.cantidad, valor));
    }
  }

  protected quitar(linea: LineaPedido): void {
    this.ejecutar(this.api.quitar(this.id, linea.id));
  }

  protected enviar(): void {
    this.ejecutar(this.api.enviar(this.id));
  }

  protected confirmarAccion(): void {
    const accion = this.confirmar();
    this.confirmar.set(null);
    if (accion === 'cuenta') {
      this.ejecutar(this.api.pedirCuenta(this.id));
    } else if (accion === 'liberar') {
      this.ejecutar(this.api.liberar(this.id), () => void this.router.navigate(['/mesero', 'mesas']));
    }
  }

  private ejecutar(peticion: Observable<Atencion>, despues?: () => void): void {
    this.ocupado.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: (a) => {
        this.atencion.set(a);
        this.ocupado.set(false);
        despues?.();
      },
      error: (error) => {
        this.ocupado.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo completar la acción.'));
      },
    });
  }
}

function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .trim();
}
