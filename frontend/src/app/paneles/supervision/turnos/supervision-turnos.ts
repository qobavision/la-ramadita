import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ROL_ETIQUETA } from '../../../auth/auth.models';
import { Dialogo } from '../../../shared/ui/dialogo/dialogo';
import { EncabezadoPagina } from '../../../shared/ui/encabezado-pagina/encabezado-pagina';
import { Icono } from '../../../shared/ui/icono/icono';
import { ColumnaTabla, TablaListado } from '../../../shared/ui/tabla-listado/tabla-listado';
import { SupervisionDemo } from '../datos/supervision.demo';
import {
  AsignacionTemporal,
  ROLES_TEMPORALES,
  RolTemporal,
  claseEstadoTurno,
} from '../datos/supervision.models';

@Component({
  selector: 'app-supervision-turnos',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, EncabezadoPagina, Icono, TablaListado, Dialogo],
  templateUrl: './supervision-turnos.html',
})
export class SupervisionTurnos {
  private readonly demo = inject(SupervisionDemo);

  protected readonly horarios = this.demo.horarios;
  protected readonly turno = this.demo.turno;
  protected readonly pendientes = this.demo.pendientesCierre;
  protected readonly personal = this.demo.personal;
  protected readonly asignaciones = this.demo.asignaciones;
  protected readonly claseTurno = claseEstadoTurno;
  protected readonly etiquetaRol = ROL_ETIQUETA;
  protected readonly rolesTemporales = ROLES_TEMPORALES;

  protected readonly dialogo = signal<'asignar' | null>(null);
  protected readonly seleccionado = signal<AsignacionTemporal | null>(null);
  protected readonly rolElegido = signal<RolTemporal>('CAJA');

  protected readonly columnasPersonal: readonly ColumnaTabla[] = [
    { titulo: 'Empleado' },
    { titulo: 'Usuario' },
    { titulo: 'Rol permanente' },
    { titulo: 'Función actual' },
    { titulo: 'Ingreso' },
    { titulo: 'Estado' },
  ];

  protected readonly columnasAsignacion: readonly ColumnaTabla[] = [
    { titulo: 'Empleado' },
    { titulo: 'Rol permanente' },
    { titulo: 'Rol temporal' },
    { titulo: 'Turno' },
    { titulo: 'Estado' },
    { titulo: 'Acciones', numerica: true },
  ];

  protected iniciarAnticipado(): void {
    this.demo.iniciarTurnoAnticipado();
  }

  protected prepararCierre(): void {
    this.demo.prepararCierre();
  }

  protected finalizar(): void {
    this.demo.finalizarTurno();
  }

  protected abrirAsignar(fila: AsignacionTemporal): void {
    this.seleccionado.set(fila);
    this.rolElegido.set(fila.rolTemporal ?? (fila.rolPermanente === 'CAJA' ? 'MESERO' : 'CAJA'));
    this.dialogo.set('asignar');
  }

  protected confirmarAsignar(): void {
    const fila = this.seleccionado();
    if (fila) {
      this.demo.asignarTemporal(fila.id, this.rolElegido());
    }
    this.cerrar();
  }

  protected quitar(fila: AsignacionTemporal): void {
    this.demo.quitarTemporal(fila.id);
  }

  protected cerrar(): void {
    this.dialogo.set(null);
    this.seleccionado.set(null);
  }
}
