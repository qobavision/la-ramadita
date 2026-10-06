import { Route } from '@angular/router';
import { DatosProximamente, Proximamente } from './proximamente';

/** Ruta hija de un panel que todavía muestra la página "Próximamente". */
export function rutaProximamente(path: string, datos: DatosProximamente): Route {
  return {
    path,
    component: Proximamente,
    title: `${datos.titulo} · La Ramadita`,
    data: datos,
  };
}
