import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { AlertasSonoras } from '../../../cocina/alertas-sonoras';
import { TiempoRealService } from '../../../core/tiempo-real/tiempo-real.service';
import { Icono } from '../../../shared/ui/icono/icono';

interface AvisoPreparado {
  id: number;
  atencionId: number;
  identificacion: string;
  producto: string;
  completo: boolean;
}

const DURACION_MS = 30_000;
const MAXIMOS = 3;

/**
 * Aviso al mesero cuando Cocina marca un producto como Preparado (RN-13).
 * Acompaña a todas las páginas del panel mesero (ver `complemento` del panel)
 * y llega por WebSocket, sin recargar la pantalla.
 */
@Component({
  selector: 'app-avisos-mesero',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, Icono],
  template: `
    <div class="avisos" aria-live="polite">
      @for (a of avisos(); track a.id) {
        <div class="aviso" role="status">
          <app-icono nombre="cocina" [tamano]="20" />
          <div>
            <strong>{{ a.identificacion }}: {{ a.completo ? 'pedido listo' : 'producto listo' }}</strong>
            <small>{{ a.producto }}</small>
          </div>
          <a class="btn btn-sm btn-success" [routerLink]="['/mesero', 'atencion', a.atencionId]" (click)="descartar(a.id)">Ver</a>
          <button type="button" class="lr-accion" title="Descartar aviso" (click)="descartar(a.id)">
            <app-icono nombre="cerrar" [tamano]="14" />
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .avisos {
      position: fixed;
      right: 16px;
      bottom: 16px;
      z-index: 1080;
      display: flex;
      flex-direction: column;
      gap: 8px;
      width: min(380px, calc(100vw - 32px));
    }

    .aviso {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 12px 12px 14px;
      border: 1px solid #cfe5da;
      border-left: 5px solid var(--lr-verde);
      border-radius: 14px;
      background: #f1f8f4;
      box-shadow: var(--lr-sombra);
      color: var(--lr-tinta);

      > div {
        flex: 1;
        min-width: 0;
      }

      strong,
      small {
        display: block;
      }

      small {
        color: var(--lr-gris);
        overflow-wrap: anywhere;
      }

      app-icono {
        color: var(--lr-verde);
      }
    }

    @media (max-width: 575.98px) {
      .avisos {
        bottom: 84px;
      }
    }
  `,
})
export class AvisosMesero {
  private readonly sonidos = new AlertasSonoras();
  private secuencia = 0;

  protected readonly avisos = signal<readonly AvisoPreparado[]>([]);

  constructor() {
    inject(DestroyRef).onDestroy(() => this.sonidos.cerrar());
    inject(TiempoRealService)
      .escuchar()
      .pipe(takeUntilDestroyed())
      .subscribe((evento) => {
        if (evento.tipo !== 'PRODUCTO_PREPARADO') {
          return;
        }
        const aviso: AvisoPreparado = {
          id: ++this.secuencia,
          atencionId: Number(evento['atencionId']),
          identificacion: String(evento['identificacion'] ?? 'Pedido'),
          producto: String(evento['producto'] ?? ''),
          completo: evento['pedidoCompleto'] === true,
        };
        this.avisos.update((lista) => [aviso, ...lista].slice(0, MAXIMOS));
        this.sonidos.reproducir('PRODUCTO_PREPARADO');
        setTimeout(() => this.descartar(aviso.id), DURACION_MS);
      });
  }

  protected descartar(id: number): void {
    this.avisos.update((lista) => lista.filter((a) => a.id !== id));
  }
}
