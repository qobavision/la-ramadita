import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { mensajeDeError } from '../../../core/errores/api-error';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { EstadoVacio } from '../../../shared/ui/estado-vacio/estado-vacio';
import { Icono } from '../../../shared/ui/icono/icono';
import { ZonasService } from '../../../zonas/zonas.service';
import { ZonaDemo } from '../datos/supervision.models';
import {
  enteroNoNegativo,
  mesasParaCard,
  puedeEliminarZona,
  puedeInactivarZona,
  tituloActividadZona,
  tituloEliminarZona,
  UsoZona,
} from './zonas.vista';

@Component({
  selector: 'app-supervision-zonas',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, EstadoVacio, Icono, Dialogo],
  templateUrl: './supervision-zonas.html',
  styles: `
    .lr-accion:disabled {
      opacity: 0.4;
      pointer-events: none;
    }
  `,
})
export class SupervisionZonas {
  private readonly zonasApi = inject(ZonasService);

  protected readonly zonas = this.zonasApi.lista;
  protected readonly error = signal<string | null>(null);
  protected readonly dialogo = signal<'form' | 'eliminar' | null>(null);
  protected readonly zonaEdicion = signal<ZonaDemo | null>(null);
  protected readonly zonaBorrar = signal<ZonaDemo | null>(null);
  protected readonly nombre = signal('');
  protected readonly mesas = signal(0);
  protected readonly cajas = signal(0);
  protected readonly cocinas = signal(0);

  constructor() {
    this.zonasApi.cargar().subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar las zonas.')),
    });
  }

  protected usoDe(zona: ZonaDemo): UsoZona {
    return zona.uso ?? 'sin-uso';
  }

  protected puedeEliminar(zona: ZonaDemo): boolean {
    return puedeEliminarZona(this.usoDe(zona));
  }

  protected puedeInactivar(zona: ZonaDemo): boolean {
    return puedeInactivarZona(zona, this.usoDe(zona));
  }

  protected tituloEliminar(zona: ZonaDemo): string {
    return tituloEliminarZona(this.usoDe(zona));
  }

  protected tituloActividad(zona: ZonaDemo): string {
    return tituloActividadZona(zona, this.usoDe(zona));
  }

  protected previewMesas(zona: ZonaDemo) {
    return mesasParaCard(zona.mesas);
  }

  protected abrir(zona?: ZonaDemo): void {
    this.error.set(null);
    this.zonaEdicion.set(zona ?? null);
    this.nombre.set(zona?.nombre ?? '');
    this.mesas.set(zona?.mesas ?? 0);
    this.cajas.set(zona?.cajas ?? 0);
    this.cocinas.set(zona?.cocinas ?? 0);
    this.dialogo.set('form');
  }

  protected guardar(): void {
    const nombre = this.nombre().trim();
    if (!nombre) {
      return;
    }
    this.error.set(null);
    this.zonasApi
      .guardar({
        id: this.zonaEdicion()?.id,
        nombre,
        cantidadMesas: enteroNoNegativo(this.mesas()),
        cajas: enteroNoNegativo(this.cajas()),
        cocinas: enteroNoNegativo(this.cocinas()),
      })
      .subscribe({
        next: () => this.cerrar(),
        error: (error) => this.error.set(mensajeDeError(error, 'No se pudo guardar la zona.')),
      });
  }

  protected alternar(zona: ZonaDemo): void {
    if (!this.puedeInactivar(zona)) {
      return;
    }
    this.error.set(null);
    this.zonasApi.alternar(zona.id).subscribe({
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo cambiar el estado de la zona.')),
    });
  }

  protected pedirEliminar(zona: ZonaDemo): void {
    if (!this.puedeEliminar(zona)) {
      return;
    }
    this.error.set(null);
    this.zonaBorrar.set(zona);
    this.dialogo.set('eliminar');
  }

  protected confirmarEliminar(): void {
    const zona = this.zonaBorrar();
    if (!zona || !this.puedeEliminar(zona)) {
      return;
    }
    this.error.set(null);
    this.zonasApi.eliminar(zona.id).subscribe({
      next: () => this.cerrar(),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo eliminar la zona.')),
    });
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.zonaEdicion.set(null);
    this.zonaBorrar.set(null);
  }
}
