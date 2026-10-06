import { NombreIcono } from '../../../shared/ui/icono/icono';

/** Entrada del menú lateral / inferior de un panel. */
export interface ItemMenu {
  etiqueta: string;
  icono: NombreIcono;
  /** Ruta relativa a la raíz del panel ('' = inicio del panel). */
  ruta: string;
  /** Si el módulo ya está implementado. Si no, se marca como "Próx." */
  disponible?: boolean;
}

/** Configuración que cada panel entrega al `PanelLayout` vía `route.data['panel']`. */
export interface ConfiguracionPanel {
  /** Nombre visible del panel, p. ej. "Administración". */
  nombre: string;
  /** Rótulo corto sobre el nombre, p. ej. "Panel de control". */
  eyebrow: string;
  menu: ItemMenu[];
}
