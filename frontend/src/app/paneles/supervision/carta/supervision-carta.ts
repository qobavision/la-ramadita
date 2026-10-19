import { NgTemplateOutlet } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal, WritableSignal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, switchMap } from 'rxjs';
import { CartasService } from '../../../carta/cartas.service';
import { ProductosService } from '../../../carta/productos.service';
import { mensajeDeError } from '../../../core/errores/api-error';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { OpcionSegmento, Segmento } from '../../../shared/ui/segmento/segmento';
import {
  AmbitoProducto,
  CartaDia,
  CategoriaProducto,
  DetalleCarta,
  EstadoProductoCarta,
  formatoSoles,
  LineaCartaVista,
  Producto,
  SeccionCarta,
  UnidadNegocio,
} from '../datos/supervision.models';
import { lineasDeSeccion, productoSirveEn, seccionActiva, unicosDetalles } from './carta.vista';

type TarjetaRestaurante = 'criollo' | 'ejecutivo' | 'marino' | 'bebidas';
type TarjetaPolleria = 'brasa' | 'chifa' | 'combos' | 'bebidasPolleria';
type Tarjeta = TarjetaRestaurante | TarjetaPolleria;
type FiltroRestaurante = 'todos' | TarjetaRestaurante;
type FiltroPolleria = 'todos' | TarjetaPolleria;
type ListaBorrador = 'entradas' | 'segundos' | 'lineas';
type DialogoCarta = 'editar' | null;

interface ConfigTarjeta {
  unidad: UnidadNegocio;
  titulo: string;
  secciones: readonly SeccionCarta[];
}

const TARJETAS: Record<Tarjeta, ConfigTarjeta> = {
  criollo: { unidad: 'Restaurante', titulo: 'Menú Criollo', secciones: ['MENU_CRIOLLO', 'MENU_CRIOLLO_ENTRADA', 'MENU_CRIOLLO_SEGUNDO'] },
  ejecutivo: { unidad: 'Restaurante', titulo: 'Menú Ejecutivo', secciones: ['MENU_EJECUTIVO'] },
  marino: { unidad: 'Restaurante', titulo: 'Platos Marinos', secciones: ['A_LA_CARTA'] },
  bebidas: { unidad: 'Restaurante', titulo: 'Bebidas', secciones: ['BEBIDA'] },
  brasa: { unidad: 'Pollería', titulo: 'Platos a la brasa', secciones: ['BRASA'] },
  chifa: { unidad: 'Pollería', titulo: 'Platos chifa', secciones: ['CHIFA'] },
  combos: { unidad: 'Pollería', titulo: 'Combos', secciones: ['COMBO'] },
  bebidasPolleria: { unidad: 'Pollería', titulo: 'Bebidas', secciones: ['BEBIDA'] },
};

interface EditorLineas {
  seccion: SeccionCarta;
  titulo: string;
  categorias: readonly CategoriaProducto[];
  vacio: string;
  agregar: string;
  prefijo: string;
}

const EDITORES: Partial<Record<Tarjeta, EditorLineas>> = {
  marino: { seccion: 'A_LA_CARTA', titulo: 'Platos marinos', categorias: ['Plato marino'], vacio: 'Selecciona tu plato marino', agregar: 'Agregar plato', prefijo: 'marino' },
  brasa: { seccion: 'BRASA', titulo: 'Platos a la brasa', categorias: ['Plato a la brasa'], vacio: 'Selecciona tu plato a la brasa', agregar: 'Agregar plato', prefijo: 'brasa' },
  chifa: { seccion: 'CHIFA', titulo: 'Platos chifa', categorias: ['Plato chifa'], vacio: 'Selecciona tu plato chifa', agregar: 'Agregar plato', prefijo: 'chifa' },
  combos: { seccion: 'COMBO', titulo: 'Combos', categorias: ['Combo'], vacio: 'Selecciona tu combo', agregar: 'Agregar combo', prefijo: 'combo' },
  bebidas: { seccion: 'BEBIDA', titulo: 'Bebidas', categorias: ['Bebida'], vacio: 'Selecciona tu bebida', agregar: 'Agregar bebida', prefijo: 'bebida' },
  bebidasPolleria: { seccion: 'BEBIDA', titulo: 'Bebidas', categorias: ['Bebida'], vacio: 'Selecciona tu bebida', agregar: 'Agregar bebida', prefijo: 'bebida' },
};

interface LineaBorrador {
  clave: number;
  productoId: number;
  nombre: string;
  precio: number | null;
}

@Component({
  selector: 'app-supervision-carta',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, NgTemplateOutlet, Dialogo, EncabezadoPagina, Icono, Segmento],
  templateUrl: './supervision-carta.html',
})
export class SupervisionCarta implements OnInit {
  private readonly cartasApi = inject(CartasService);
  private readonly productosApi = inject(ProductosService);
  private siguiente = 1;

  protected readonly formato = formatoSoles;
  protected readonly productos = this.productosApi.lista;
  protected readonly unidad = signal<UnidadNegocio>('Restaurante');
  protected readonly carta = signal<CartaDia | null>(null);
  protected readonly filtro = signal<FiltroRestaurante>('todos');
  protected readonly filtroPolleria = signal<FiltroPolleria>('todos');
  protected readonly edicion = signal<Tarjeta | null>(null);
  protected readonly dialogo = signal<DialogoCarta>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly enviando = signal(false);

  protected readonly borradorPrecio = signal<number | null>(null);
  protected readonly borradorEntradas = signal<LineaBorrador[]>([]);
  protected readonly borradorSegundos = signal<LineaBorrador[]>([]);
  protected readonly borradorLineas = signal<LineaBorrador[]>([]);

  protected readonly unidades: readonly OpcionSegmento<UnidadNegocio>[] = [
    { valor: 'Restaurante', etiqueta: 'Restaurante' },
    { valor: 'Pollería', etiqueta: 'Pollería' },
  ];
  protected readonly menusCarta: readonly OpcionSegmento<TarjetaRestaurante>[] = [
    { valor: 'criollo', etiqueta: 'Menú criollo' },
    { valor: 'ejecutivo', etiqueta: 'Menú ejecutivo' },
    { valor: 'marino', etiqueta: 'Platos marinos' },
    { valor: 'bebidas', etiqueta: 'Bebidas' },
  ];
  protected readonly menusPolleria: readonly OpcionSegmento<TarjetaPolleria>[] = [
    { valor: 'brasa', etiqueta: 'Platos a la brasa' },
    { valor: 'chifa', etiqueta: 'Platos chifa' },
    { valor: 'combos', etiqueta: 'Combos' },
    { valor: 'bebidasPolleria', etiqueta: 'Bebidas' },
  ];

  ngOnInit(): void {
    this.cargarUnidad(this.unidad());
  }


  protected cambiarUnidad(unidad: UnidadNegocio | null | undefined): void {
    const actual = unidad ?? 'Restaurante';
    this.unidad.set(actual);
    this.filtro.set('todos');
    this.filtroPolleria.set('todos');
    this.cargarUnidad(actual);
  }

  protected elegirMenu(valor: TarjetaRestaurante | ''): void {
    if (valor) {
      this.filtro.set(valor);
    }
  }

  protected elegirPolleria(valor: TarjetaPolleria | ''): void {
    if (valor) {
      this.filtroPolleria.set(valor);
    }
  }

  protected visible(tarjeta: TarjetaRestaurante): boolean {
    const filtro = this.filtro();
    return filtro === 'todos' || filtro === tarjeta;
  }

  protected visiblePolleria(tarjeta: TarjetaPolleria): boolean {
    const filtro = this.filtroPolleria();
    return filtro === 'todos' || filtro === tarjeta;
  }

  protected lineas(tarjeta: Tarjeta, seccion: SeccionCarta): LineaCartaVista[] {
    return lineasDeSeccion(this.carta() ?? undefined, seccion);
  }

  protected lineasPlatos(tarjeta: Tarjeta, seccion: SeccionCarta): LineaCartaVista[] {
    return this.lineas(tarjeta, seccion).filter((l) => !this.esProductoDeMenu(l.productoId, l.nombre));
  }

  protected precioMenu(tarjeta: Tarjeta): number {
    if (tarjeta === 'ejecutivo') {
      const menu = this.lineas('ejecutivo', 'MENU_EJECUTIVO').find((l) => this.esProductoDeMenu(l.productoId, l.nombre));
      return menu?.precio ?? 0;
    }
    const seccion = TARJETAS[tarjeta].secciones[0];
    return seccion ? (this.lineas(tarjeta, seccion)[0]?.precio ?? 0) : 0;
  }

  protected activa(tarjeta: Tarjeta): boolean {
    return TARJETAS[tarjeta].secciones.every((s) => seccionActiva(this.lineas(tarjeta, s)));
  }

  protected alternar(tarjeta: Tarjeta): void {
    const carta = this.carta();
    if (!carta) {
      return;
    }
    const secciones = TARJETAS[tarjeta].secciones;
    const estado: EstadoProductoCarta = this.activa(tarjeta) ? 'AGOTADO' : 'DISPONIBLE';
    const actualizada: CartaDia = {
      ...carta,
      detalles: carta.detalles.map((d) => (secciones.includes(d.seccion) ? { ...d, estado } : d)),
    };
    this.persistir(actualizada);
  }

  protected editorLineas(): EditorLineas | null {
    const tarjeta = this.edicion();
    return tarjeta ? (EDITORES[tarjeta] ?? null) : null;
  }

  protected tituloEdicion(): string {
    const tarjeta = this.edicion();
    return tarjeta ? TARJETAS[tarjeta].titulo : '';
  }

  protected opciones(categorias: readonly CategoriaProducto[], actualId: number, tomadas: readonly LineaBorrador[]): Producto[] {
    return this.sinTomadas(
      this.productos().filter((p) =>
        productoSirveEn(p, this.unidad())
        && categorias.includes(p.categoria)
        && (p.categoria !== 'Combo' || (p.componentes?.length ?? 0) >= 2),
      ),
      actualId,
      tomadas,
    );
  }

  protected abrir(tarjeta: Tarjeta): void {
    this.edicion.set(tarjeta);
    this.error.set(null);
    this.dialogo.set('editar');
    if (tarjeta === 'criollo') {
      this.borradorPrecio.set(this.precioMenu('criollo') || null);
      this.borradorEntradas.set(this.copiar(this.lineas('criollo', 'MENU_CRIOLLO_ENTRADA')));
      this.borradorSegundos.set(this.copiar(this.lineas('criollo', 'MENU_CRIOLLO_SEGUNDO')));
      return;
    }
    if (tarjeta === 'ejecutivo') {
      const platos = this.lineasPlatos('ejecutivo', 'MENU_EJECUTIVO');
      this.borradorPrecio.set(this.precioMenu('ejecutivo') || platos[0]?.precio || null);
      this.borradorLineas.set(this.copiar(platos));
      return;
    }
    const editor = EDITORES[tarjeta];
    if (editor) {
      this.borradorLineas.set(this.copiar(this.lineas(tarjeta, editor.seccion), true));
    }
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.edicion.set(null);
    this.error.set(null);
    this.enviando.set(false);
  }

  protected aplicar(): void {
    const tarjeta = this.edicion();
    const carta = this.carta();
    if (!tarjeta || !carta) {
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    const trabajo: Observable<CartaDia> =
      tarjeta === 'criollo'
        ? this.asegurarProducto('Menú Criollo', 'Menú', 'RESTAURANTE', Number(this.borradorPrecio() ?? 0)).pipe(
            switchMap((menu) =>
              this.cartasApi.guardar(
                this.fusionar(carta, [
                  this.detalleDe(menu, 'MENU_CRIOLLO', this.borradorPrecio()),
                  ...this.detallesDe(this.borradorEntradas(), 'MENU_CRIOLLO_ENTRADA', null),
                  ...this.detallesDe(this.borradorSegundos(), 'MENU_CRIOLLO_SEGUNDO', null),
                ], ['MENU_CRIOLLO', 'MENU_CRIOLLO_ENTRADA', 'MENU_CRIOLLO_SEGUNDO']),
              ),
            ),
          )
        : tarjeta === 'ejecutivo'
          ? this.asegurarProducto('Menú Ejecutivo', 'Menú', 'RESTAURANTE', Number(this.borradorPrecio() ?? 0)).pipe(
              switchMap((menu) =>
                this.cartasApi.guardar(
                  this.fusionar(carta, [
                    this.detalleDe(menu, 'MENU_EJECUTIVO', this.borradorPrecio()),
                    ...this.detallesDe(this.borradorLineas(), 'MENU_EJECUTIVO', null),
                  ], ['MENU_EJECUTIVO']),
                ),
              ),
            )
          : this.cartasApi.guardar(
              this.fusionar(
                carta,
                this.detallesDe(this.borradorLineas(), EDITORES[tarjeta]!.seccion, 'precio'),
                [EDITORES[tarjeta]!.seccion],
              ),
            );
    trabajo.subscribe({
      next: (actual) => {
        this.enviando.set(false);
        this.cerrar();
        this.cargarUnidad(this.unidad(), actual.id);
      },
      error: (error) => {
        this.enviando.set(false);
        this.error.set(mensajeDeError(error, 'No se pudo guardar la carta.'));
      },
    });
  }

  protected nuevaFila(lista: ListaBorrador, prefijo: string): void {
    const clave = this.siguiente++;
    this.borradorDe(lista).update((actual) => [...actual, { clave, productoId: 0, nombre: '', precio: null }]);
    setTimeout(() => document.getElementById(`${prefijo}-${clave}`)?.focus());
  }

  protected elegir(lista: ListaBorrador, clave: number, id: number): void {
    const producto = this.productos().find((p) => p.id === Number(id));
    this.borradorDe(lista).update((actual) =>
      actual.map((l) => (l.clave === clave ? { ...l, productoId: producto?.id ?? 0, nombre: producto?.nombre ?? '', precio: l.precio ?? producto?.precio ?? null } : l)),
    );
  }

  protected precioFila(clave: number, precio: number | null): void {
    this.borradorLineas.update((actual) => actual.map((l) => (l.clave === clave ? { ...l, precio: this.precioLibre(precio) } : l)));
  }

  protected precioMenuBorrador(precio: number | null): void {
    this.borradorPrecio.set(this.precioLibre(precio));
  }

  protected quitarFila(lista: ListaBorrador, clave: number): void {
    this.borradorDe(lista).update((actual) => actual.filter((l) => l.clave !== clave));
  }

  private cargarUnidad(unidad: UnidadNegocio, cartaId?: number): void {
    this.productosApi.cargar(unidad, true).subscribe({ error: () => undefined });
    this.cartasApi.cargar(unidad).subscribe({
      next: (lista) => {
        const elegida = (cartaId && lista.find((c) => c.id === cartaId)) || lista.find((c) => c.vigente) || lista[0];
        if (elegida) {
          this.carta.set(elegida);
          return;
        }
        this.cartasApi.crear(unidad, unidad === 'Pollería' ? 'Carta Pollería' : 'Carta Restaurante').subscribe({
          next: (creada) => this.cargarUnidad(unidad, creada.id),
          error: (error) => this.error.set(mensajeDeError(error, 'No se pudo preparar la carta.')),
        });
      },
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudieron cargar las cartas.')),
    });
  }

  private persistir(carta: CartaDia): void {
    this.cartasApi.guardar(carta).subscribe({
      next: (actual) => this.cargarUnidad(carta.unidad, actual.id),
      error: (error) => this.error.set(mensajeDeError(error, 'No se pudo guardar la carta.')),
    });
  }

  private asegurarProducto(nombre: string, categoria: CategoriaProducto, ambito: AmbitoProducto, precio: number): Observable<Producto> {
    const existente = this.productosApi.buscarPorNombre(nombre);
    if (existente) {
      return this.productosApi.actualizar(existente.id, { nombre, categoria, ambito, precio, componentes: [] });
    }
    return this.productosApi.crear({ nombre, categoria, ambito, precio, componentes: [] });
  }

  private fusionar(carta: CartaDia, lineas: DetalleCarta[], secciones: readonly SeccionCarta[]): CartaDia {
    return {
      ...carta,
      detalles: unicosDetalles([
        ...carta.detalles.filter((d) => !secciones.includes(d.seccion)),
        ...lineas.filter((l) => l.productoId > 0),
      ]),
    };
  }

  private detalleDe(producto: Producto, seccion: SeccionCarta, precio: number | null): DetalleCarta {
    const previa = this.carta()?.detalles.find((d) => d.seccion === seccion && d.productoId === producto.id);
    return {
      id: previa?.id ?? 0,
      productoId: producto.id,
      nombre: producto.nombre,
      seccion,
      precioDia: precio,
      precioEfectivo: Number(precio ?? producto.precio),
      estado: previa?.estado ?? 'DISPONIBLE',
      componentes: producto.componentes ?? [],
    };
  }

  private detallesDe(lineas: readonly LineaBorrador[], seccion: SeccionCarta, precio: 'precio' | null): DetalleCarta[] {
    return unicosDetalles(lineas
      .filter((l) => l.productoId > 0)
      .map((l) => {
        const producto = this.productos().find((p) => p.id === l.productoId);
        const previa = this.carta()?.detalles.find((d) => d.seccion === seccion && d.productoId === l.productoId);
        const precioDia = precio === 'precio' ? l.precio : null;
        return {
          id: previa?.id ?? 0,
          productoId: l.productoId,
          nombre: l.nombre || producto?.nombre || '',
          seccion,
          precioDia,
          precioEfectivo: Number(precioDia ?? producto?.precio ?? 0),
          estado: previa?.estado ?? 'DISPONIBLE',
          componentes: producto?.componentes ?? [],
        };
      }));
  }

  private copiar(lineas: readonly LineaCartaVista[], conPrecio = false): LineaBorrador[] {
    return lineas.map((l) => ({
      clave: this.siguiente++,
      productoId: l.productoId,
      nombre: l.nombre,
      precio: conPrecio ? l.precio : null,
    }));
  }

  private sinTomadas(productos: readonly Producto[], actualId: number, tomadas: readonly LineaBorrador[]): Producto[] {
    const ids = new Set(tomadas.filter((l) => l.productoId !== actualId).map((l) => l.productoId));
    return productos.filter((p) => !ids.has(p.id));
  }

  private borradorDe(lista: ListaBorrador): WritableSignal<LineaBorrador[]> {
    if (lista === 'entradas') {
      return this.borradorEntradas;
    }
    if (lista === 'segundos') {
      return this.borradorSegundos;
    }
    return this.borradorLineas;
  }

  private esProductoDeMenu(productoId: number, nombre: string): boolean {
    const producto = this.productos().find((p) => p.id === productoId);
    return producto?.categoria === 'Menú' || nombre.trim().toLocaleLowerCase('es') === 'menú ejecutivo';
  }

  private precioLibre(valor: number | null): number | null {
    if (valor === null || valor === undefined || (valor as unknown) === '') {
      return null;
    }
    const precio = Number(valor);
    if (!Number.isFinite(precio)) {
      return null;
    }
    return precio < 0 ? 0 : precio;
  }
}
