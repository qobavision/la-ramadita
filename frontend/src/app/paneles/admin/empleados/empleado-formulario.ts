import { ChangeDetectionStrategy, Component, effect, inject, input, output } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Cargo, Empleado, EmpleadoRequest } from '../../../personal/personal.models';

@Component({
  selector: 'app-empleado-formulario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule],
  template: `
    <form class="lr-formulario" [formGroup]="formulario" (ngSubmit)="enviar()" novalidate>
      <div>
        <label class="form-label" for="emp-nombres">Nombres</label>
        <input id="emp-nombres" class="form-control" formControlName="nombres" autocomplete="given-name" [class.is-invalid]="invalido('nombres')" />
        <div class="invalid-feedback">Ingresa los nombres.</div>
      </div>

      <div>
        <label class="form-label" for="emp-apellidos">Apellidos</label>
        <input id="emp-apellidos" class="form-control" formControlName="apellidos" autocomplete="family-name" [class.is-invalid]="invalido('apellidos')" />
        <div class="invalid-feedback">Ingresa los apellidos.</div>
      </div>

      <div>
        <label class="form-label" for="emp-dni">DNI</label>
        <input id="emp-dni" class="form-control" formControlName="dni" inputmode="numeric" maxlength="8" autocomplete="off" [class.is-invalid]="invalido('dni')" />
        <div class="invalid-feedback">El DNI debe tener 8 dígitos.</div>
        @if (!dniObligatorio()) {
          <div class="form-text">Este trabajador se migró sin DNI. Puedes dejarlo vacío o completar los 8 dígitos.</div>
        }
      </div>

      <div>
        <label class="form-label" for="emp-telefono">Teléfono</label>
        <input id="emp-telefono" class="form-control" formControlName="telefono" inputmode="tel" autocomplete="tel" [class.is-invalid]="invalido('telefono')" />
        <div class="invalid-feedback">Ingresa un teléfono válido o déjalo vacío.</div>
      </div>

      <div class="lr-campo--completo">
        <label class="form-label" for="emp-correo">Correo personal</label>
        <input
          id="emp-correo"
          class="form-control"
          formControlName="correoPersonal"
          type="email"
          autocomplete="email"
          placeholder="maria@gmail.com"
          [class.is-invalid]="invalido('correoPersonal')"
        />
        <div class="invalid-feedback">Ingresa un correo válido o déjalo vacío.</div>
        <div class="form-text">Correo de contacto de la persona. No se usa para iniciar sesión.</div>
      </div>

      <div>
        <label class="form-label" for="emp-cargo">Cargo</label>
        <select id="emp-cargo" class="form-select" formControlName="cargoId" [class.is-invalid]="invalido('cargoId')">
          <option [ngValue]="0">Selecciona un cargo</option>
          @for (cargo of cargos(); track cargo.id) {
            <option [ngValue]="cargo.id">{{ cargo.nombre }}</option>
          }
        </select>
        <div class="invalid-feedback">Selecciona el cargo laboral.</div>
      </div>

      <div>
        <label class="form-label" for="emp-estado">Estado</label>
        <select id="emp-estado" class="form-select" formControlName="activo">
          <option [ngValue]="true">Activo (trabaja actualmente)</option>
          <option [ngValue]="false">Inactivo (no trabaja actualmente)</option>
        </select>
        @if (empleado()?.archivado) {
          <div class="form-text">Este empleado está archivado y no puede reactivarse desde Editar.</div>
        }
      </div>

      @if (error()) {
        <div class="alert alert-danger py-2 small mb-0 lr-campo--completo" role="alert">{{ error() }}</div>
      }

      <div class="lr-formulario__pie">
        <button type="button" class="btn btn-outline-secondary" (click)="cancelar.emit()">Cancelar</button>
        <button type="submit" class="btn btn-primary">{{ etiquetaGuardar() }}</button>
      </div>
    </form>
  `,
})
export class EmpleadoFormulario {
  private readonly fb = inject(NonNullableFormBuilder);

  readonly empleado = input<Empleado | null>(null);
  readonly cargos = input<readonly Cargo[]>([]);
  readonly error = input<string | null>(null);
  readonly etiquetaGuardar = input('Registrar empleado');
  readonly guardar = output<EmpleadoRequest>();
  readonly cancelar = output<void>();

  protected readonly dniObligatorio = () => !this.empleado() || !!this.empleado()?.dni;

  readonly formulario = this.fb.group({
    nombres: ['', [Validators.required, Validators.minLength(2)]],
    apellidos: ['', [Validators.required, Validators.minLength(2)]],
    dni: ['', [Validators.pattern(/^\d{8}$/)]],
    telefono: ['', [Validators.pattern(/^$|^\d{6,15}$/)]],
    correoPersonal: ['', [Validators.email]],
    cargoId: [0, [Validators.min(1)]],
    activo: [true],
  });

  constructor() {
    effect(() => {
      const actual = this.empleado();
      const dni = this.formulario.controls.dni;
      if (this.dniObligatorio()) {
        dni.setValidators([Validators.required, Validators.pattern(/^\d{8}$/)]);
      } else {
        dni.setValidators([Validators.pattern(/^$|^\d{8}$/)]);
      }
      dni.updateValueAndValidity({ emitEvent: false });
      if (actual) {
        this.formulario.patchValue({
          nombres: actual.nombres,
          apellidos: actual.apellidos,
          dni: actual.dni ?? '',
          telefono: actual.telefono ?? '',
          correoPersonal: actual.correoPersonal ?? '',
          cargoId: actual.cargo.id,
          activo: actual.activo,
        });
        if (actual.archivado) {
          this.formulario.controls.activo.disable({ emitEvent: false });
        } else {
          this.formulario.controls.activo.enable({ emitEvent: false });
        }
      } else {
        this.formulario.controls.activo.enable({ emitEvent: false });
      }
    });
  }

  protected invalido(nombre: 'nombres' | 'apellidos' | 'dni' | 'telefono' | 'correoPersonal' | 'cargoId'): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.dirty || control.touched);
  }

  protected enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    const dni = valor.dni.trim();
    this.guardar.emit({
      nombres: valor.nombres.trim(),
      apellidos: valor.apellidos.trim(),
      dni: dni.length === 0 ? null : dni,
      telefono: valor.telefono.trim() || null,
      correoPersonal: valor.correoPersonal.trim() || null,
      cargoId: valor.cargoId,
      activo: valor.activo,
    });
  }
}
