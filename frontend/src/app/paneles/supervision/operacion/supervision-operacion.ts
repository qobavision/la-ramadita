import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import { MesaDemo, ZonaDemo, clasePedido } from '../datos/supervision.models';

type VistaOperacion = 'resumen' | 'mesas' | 'pedidos' | 'cocina' | 'config';

@Component({
  selector: 'app-supervision-operacion',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, Segmento, TablaListado, Dialogo],
  templateUrl: './supervision-operacion.html',
})
export class SupervisionOperacion {
  private readonly demo = inject(SupervisionDemo);

  protected readonly vista = signal<VistaOperacion>('resumen');
  protected readonly zonas = this.demo.zonas;
  protected readonly mesas = this.demo.mesas;
  protected readonly pedidos = this.demo.pedidos;
  protected readonly clasePedido = clasePedido;

  protected readonly vistas: readonly OpcionSegmento<VistaOperacion>[] = [
    { valor: 'resumen', etiqueta: 'Resumen' },
    { valor: 'mesas', etiqueta: 'Mesas' },
    { valor: 'pedidos', etiqueta: 'Pedidos' },
    { valor: 'cocina', etiqueta: 'Cocina' },
    { valor: 'config', etiqueta: 'Zonas y mesas' },
  ];

  protected readonly dialogo = signal<'zona' | 'mesa' | null>(null);
  protected readonly zonaEdicion = signal<ZonaDemo | null>(null);
  protected readonly mesaEdicion = signal<MesaDemo | null>(null);
  protected readonly nombreZona = signal('');
  protected readonly zonaActiva = signal(true);
  protected readonly nombreMesa = signal('');
  protected readonly zonaMesaId = signal(1);
  protected readonly mesaActiva = signal(true);

  protected readonly pendientesCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Pendiente cocina'));
  protected readonly enCursoCocina = computed(() => this.pedidos().filter((p) => p.estado === 'En curso' || p.estado === 'Modificado'));
  protected readonly listosCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Preparado'));
  protected readonly otrosCocina = computed(() => this.pedidos().filter((p) => p.estado === 'Cancelado' || p.estado === 'Modificado'));

  protected readonly kpis = computed(() => [
    { etiqueta: 'Mesas libres', valor: this.demo.mesasLibres(), icono: 'mesas' as const },
    { etiqueta: 'Mesas ocupadas', valor: this.demo.mesasOcupadas(), icono: 'mesas' as const },
    { etiqueta: 'Pedidos en curso', valor: this.demo.pedidosEnCurso(), icono: 'pedidos' as const },
    { etiqueta: 'Pendientes en cocina', valor: this.demo.pedidosCocina(), icono: 'cocina' as const },
    { etiqueta: 'Preparados', valor: this.demo.pedidosPreparados(), icono: 'cocina' as const },
    { etiqueta: 'Atenciones abiertas', valor: this.demo.atencionesAbiertas(), icono: 'operacion' as const },
  ]);

  protected readonly columnasMesas: readonly ColumnaTabla[] = [
    { titulo: 'Mesa' },
    { titulo: 'Zona' },
    { titulo: 'Estado' },
    { titulo: 'Atención' },
    { titulo: 'Mesero' },
    { titulo: 'Tiempo' },
  ];

  protected readonly columnasPedidos: readonly ColumnaTabla[] = [
    { titulo: 'Pedido' },
    { titulo: 'Mesa / origen' },
    { titulo: 'Estado' },
    { titulo: 'Hora' },
    { titulo: 'Responsable' },
    { titulo: 'Cocina' },
  ];

  protected readonly columnasZonas: readonly ColumnaTabla[] = [
    { titulo: 'Zona' },
    { titulo: 'Mesas' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected readonly columnasConfigMesas: readonly ColumnaTabla[] = [
    { titulo: 'Mesa' },
    { titulo: 'Zona' },
    { titulo: 'Configuración' },
    { titulo: 'Operación' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected mesasDe(zona: ZonaDemo): number {
    return this.mesas().filter((m) => m.zonaId === zona.id).length;
  }

  protected abrirZona(zona?: ZonaDemo): void {
    this.zonaEdicion.set(zona ?? null);
    this.nombreZona.set(zona?.nombre ?? '');
    this.zonaActiva.set(zona?.activa ?? true);
    this.dialogo.set('zona');
  }

  protected guardarZona(): void {
    const nombre = this.nombreZona().trim();
    if (!nombre) {
      return;
    }
    this.demo.guardarZona({ id: this.zonaEdicion()?.id, nombre, activa: this.zonaActiva() });
    this.cerrar();
  }

  protected abrirMesa(mesa?: MesaDemo): void {
    this.mesaEdicion.set(mesa ?? null);
    this.nombreMesa.set(mesa?.nombre ?? '');
    this.zonaMesaId.set(mesa?.zonaId ?? this.zonas()[0]?.id ?? 1);
    this.mesaActiva.set(mesa?.activa ?? true);
    this.dialogo.set('mesa');
  }

  protected guardarMesa(): void {
    const nombre = this.nombreMesa().trim();
    if (!nombre) {
      return;
    }
    this.demo.guardarMesa({ id: this.mesaEdicion()?.id, nombre, zonaId: this.zonaMesaId(), activa: this.mesaActiva() });
    this.cerrar();
  }

  protected alternarZona(zona: ZonaDemo): void {
    this.demo.alternarZona(zona);
  }

  protected alternarMesa(mesa: MesaDemo): void {
    this.demo.alternarMesa(mesa);
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.zonaEdicion.set(null);
    this.mesaEdicion.set(null);
  }
}
