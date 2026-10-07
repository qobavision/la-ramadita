import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { PANEL_POR_ROL } from '../../../auth/paneles';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono, NombreIcono } from '../../../shared/ui/icono/icono';
import { TarjetaModulo } from '../../../shared/ui/tarjeta-modulo/tarjeta-modulo';

interface AccesoOperativo {
  rol: Rol;
  titulo: string;
  descripcion: string;
  icono: NombreIcono;
}

/**
 * Accesos operativos: el ADMINISTRADOR entra a los paneles de los demás
 * roles con su misma sesión (no cambia de usuario ni de rol) para revisar
 * o intervenir. Desde esos paneles tiene un enlace de regreso en la cabecera.
 */
@Component({
  selector: 'app-admin-accesos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [EncabezadoPagina, Icono, TarjetaModulo],
  templateUrl: './admin-accesos.html',
})
export class AdminAccesos {
  protected readonly accesos: readonly AccesoOperativo[] = [
    {
      rol: 'SUPERVISOR',
      titulo: 'Supervisión',
      descripcion: 'Compras, recepción de insumos, inventario y carta diaria.',
      icono: 'operacion',
    },
    {
      rol: 'MESERO',
      titulo: 'Mesero',
      descripcion: 'Mesas, pedidos y consulta de cuentas en salón.',
      icono: 'mesas',
    },
    {
      rol: 'COCINA',
      titulo: 'Cocina',
      descripcion: 'Pedidos enviados a cocina y productos preparados.',
      icono: 'cocina',
    },
    {
      rol: 'CAJA',
      titulo: 'Caja',
      descripcion: 'Cobros, pagos, división de cuentas y apertura/cierre de caja.',
      icono: 'caja',
    },
  ];

  protected rutaDe(rol: Rol): string {
    return PANEL_POR_ROL[rol];
  }

  protected etiquetaDe(rol: Rol): string {
    return ROL_ETIQUETA[rol];
  }
}
