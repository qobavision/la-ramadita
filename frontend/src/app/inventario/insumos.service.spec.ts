import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { InsumoApi } from './inventario.models';
import { InsumosService } from './insumos.service';

describe('InsumosService', () => {
  let service: InsumosService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/insumos`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(InsumosService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('lista activos por defecto', () => {
    service.cargar().subscribe((items) => {
      expect(items.length).toBe(1);
      expect(items[0].activo).toBe(true);
    });

    const peticion = http.expectOne((req) => req.url === base);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('estado')).toBe('ACTIVOS');
    expect(peticion.request.params.get('q')).toBeNull();
    peticion.flush([insumoApi({ id: 1, nombre: 'Papa Amarilla', activo: true })]);
  });

  it('lista inactivos y combina la búsqueda', () => {
    service.cargar('huayro', 'INACTIVOS').subscribe((items) => {
      expect(items[0].nombre).toBe('Papa Huayro');
      expect(items[0].activo).toBe(false);
    });

    const peticion = http.expectOne((req) => req.url === base);
    expect(peticion.request.params.get('estado')).toBe('INACTIVOS');
    expect(peticion.request.params.get('q')).toBe('huayro');
    peticion.flush([insumoApi({ id: 8, nombre: 'Papa Huayro', activo: false })]);
  });

  it('lista todos', () => {
    service.cargar('', 'TODOS').subscribe((items) => {
      expect(items.length).toBe(2);
    });

    const peticion = http.expectOne((req) => req.url === base);
    expect(peticion.request.params.get('estado')).toBe('TODOS');
    peticion.flush([
      insumoApi({ id: 1, nombre: 'Papa Amarilla', activo: true }),
      insumoApi({ id: 8, nombre: 'Papa Huayro', activo: false }),
    ]);
  });

  it('reactiva un insumo inactivo', () => {
    service.cargar('', 'INACTIVOS').subscribe();
    http.expectOne((req) => req.url === base).flush([insumoApi({ id: 8, nombre: 'Papa Huayro', activo: false })]);

    service.reactivar(8).subscribe((item) => {
      expect(item.activo).toBe(true);
      expect(item.id).toBe(8);
    });

    const peticion = http.expectOne(`${base}/8/reactivar`);
    expect(peticion.request.method).toBe('PATCH');
    peticion.flush(insumoApi({ id: 8, nombre: 'Papa Huayro', activo: true }));
    http.expectOne((req) => req.url === base && req.params.get('estado') === 'INACTIVOS').flush([]);
  });
});

function insumoApi(parcial: Partial<InsumoApi> & Pick<InsumoApi, 'id' | 'nombre' | 'activo'>): InsumoApi {
  return {
    unidadMedida: 'KILOGRAMO',
    stockActual: 10,
    stockMinimo: 3,
    estado: 'NORMAL',
    ...parcial,
  };
}
