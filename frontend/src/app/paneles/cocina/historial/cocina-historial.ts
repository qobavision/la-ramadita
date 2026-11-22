import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { horaDe } from '../../../atencion/atencion.models';
import { Comanda, ETIQUETA_ESTADO_COMANDA, EstadoComanda, HistorialCocina, minutosEntre } from '../../../cocina/cocina.models';
import { CocinaService } from '../../../cocina/cocina.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';

type FiltroEstado = EstadoComanda | '';

/** Historial de Cocina: comandas del turno en curso (o del último cerrado) con su tiempo de preparación. */
@Component({
  selector: 'app-cocina-historial',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Segmento, TablaListado],
  templateUrl: './cocina-historial.html',
})
export class CocinaHistorial {
  private readonly api = inject(CocinaService);

  protected readonly historial = signal<HistorialCocina>({ turno: null, comandas: [] });
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly busqueda = signal('');
  protected readonly filtroEstado = signal<FiltroEstado>('');

  protected readonly etiquetaEstado = ETIQUETA_ESTADO_COMANDA;
  protected readonly hora = horaDe;

  protected readonly columnas: readonly ColumnaTabla[] = [
    { titulo: 'Envío' },
    { titulo: 'Pedido' },
    { titulo: 'Productos' },
    { titulo: 'Estado' },
    { titulo: 'Listo' },
    { titulo: 'Preparación', numerica: true },
  ];

  protected readonly filtrosEstado: readonly OpcionSegmento<FiltroEstado>[] = [
    { valor: '', etiqueta: 'Todos' },
    { valor: 'PENDIENTE', etiqueta: 'Pendientes' },
    { valor: 'PREPARADO', etiqueta: 'Preparados' },
    { valor: 'CANCELADO', etiqueta: 'Cancelados' },
  ];

  protected readonly filas = computed(() => {
    const texto = this.busqueda().trim().toLowerCase();
    const estado = this.filtroEstado();
    return this.historial().comandas.filter(
      (c) =>
        (!estado || c.estado === estado) &&
        (!texto ||
          `${c.identificacion} ${c.zona ?? ''} ${c.mesero} ${c.lineas.map((l) => l.producto).join(' ')}`
            .toLowerCase()
            .includes(texto)),
    );
  });

  protected readonly resumenTurno = computed(() => {
    const comandas = this.historial().comandas;
    const preparadas = comandas.filter((c) => c.estado === 'PREPARADO' && c.listo);
    const promedio = preparadas.length
      ? Math.round(preparadas.reduce((s, c) => s + minutosEntre(c.enviado, c.listo), 0) / preparadas.length)
      : null;
    return {
      total: comandas.length,
      preparadas: preparadas.length,
      modificadas: comandas.filter((c) => c.modificado).length,
      promedio,
    };
  });

  constructor() {
    this.api.historial().subscribe({
      next: (h) => {
        this.historial.set(h);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.error.set(mensajeDeError(e, 'No se pudo cargar el historial de cocina.'));
        this.cargando.set(false);
      },
    });
  }

  protected productos(c: Comanda): string {
    return c.lineas.map((l) => `${l.cantidad} × ${l.producto}${l.estado === 'CANCELADO' ? ' (cancelado)' : ''}`).join(', ');
  }

  protected preparacion(c: Comanda): string {
    return c.estado === 'PREPARADO' && c.listo ? `${minutosEntre(c.enviado, c.listo)} min` : '—';
  }
}
