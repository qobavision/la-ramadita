import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { EncabezadoPagina } from '../../ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../ui/icono/icono';

/** Datos esperados en `route.data` para esta página. */
export interface DatosProximamente {
  titulo: string;
  descripcion?: string;
  /** Módulo funcional del proyecto al que pertenece, p. ej. "M05". */
  modulo?: string;
}

/**
 * Página de relleno para secciones del menú cuyo módulo aún no se implementa.
 * No simula datos del negocio: solo indica que la función llegará más adelante.
 */
@Component({
  selector: 'app-proximamente',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, RouterLink],
  template: `
    <app-encabezado-pagina
      [eyebrow]="datos.modulo ? 'Módulo ' + datos.modulo : 'En construcción'"
      [titulo]="datos.titulo"
      [descripcion]="datos.descripcion"
    />

    <section class="lr-tarjeta lr-vacio">
      <span class="lr-vacio__icono"><app-icono nombre="construccion" [tamano]="30" /></span>
      <h2>Próximamente</h2>
      <p>Esta sección todavía no está disponible. Se habilitará cuando se implemente el módulo correspondiente.</p>
      <a class="btn btn-outline-secondary" routerLink="../">
        <app-icono nombre="volver" [tamano]="16" />
        Volver al inicio del panel
      </a>
    </section>
  `,
})
export class Proximamente {
  protected readonly datos: DatosProximamente = {
    titulo: 'Sección',
    ...(inject(ActivatedRoute).snapshot.data as Partial<DatosProximamente>),
  };
}
