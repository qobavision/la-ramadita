import { ChangeDetectionStrategy, Component, effect, inject, input, output, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Rol, ROL_ETIQUETA } from '../../../auth/auth.models';
import { Icono } from '../../../shared/ui/icono/icono';
import { Empleado, nombreCompletoDe } from '../../../personal/personal.models';

export interface AltaAccesoFormulario {
  empleadoId: number;
  username: string;
  rol: Rol;
  activo: boolean;
  password: string;
}

function clavesCoinciden(grupo: AbstractControl): ValidationErrors | null {
  const password = grupo.get('password')?.value;
  const confirmacion = grupo.get('confirmacion')?.value;
  if (!password && !confirmacion) {
    return null;
  }
  return password === confirmacion ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-usuario-formulario',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, Icono],
  template: `
    <form class="lr-formulario" [formGroup]="formulario" (ngSubmit)="enviar()" novalidate>
      @if (!soloClave()) {
      <div class="lr-campo--completo">
        <label class="form-label" for="usr-empleado">Empleado</label>
        <select
          id="usr-empleado"
          class="form-select"
          formControlName="empleadoId"
          [class.is-invalid]="invalido('empleadoId')"
        >
          <option [ngValue]="0">Selecciona un empleado</option>
          @for (empleado of empleados(); track empleado.id) {
            <option [ngValue]="empleado.id">{{ nombreDe(empleado) }} · {{ empleado.cargo.nombre }}</option>
          }
        </select>
        <div class="invalid-feedback">Selecciona el empleado al que pertenece esta cuenta.</div>
        @if (empleados().length === 0) {
          <div class="form-text">No hay empleados sin cuenta. Registra un empleado primero o elige uno que aún no tenga acceso.</div>
        }
      </div>

      <div class="lr-campo--completo">
        <label class="form-label" for="usr-username">Username</label>
        <input
          id="usr-username"
          class="form-control"
          formControlName="username"
          autocomplete="off"
          autocapitalize="none"
          spellcheck="false"
          placeholder="maria@laramadita.com"
          [class.is-invalid]="invalido('username')"
        />
        <div class="invalid-feedback">Ingresa un username.</div>
        <div class="form-text">Identificador interno para iniciar sesión. Es distinto del correo personal del empleado.</div>
      </div>
      }

      @if (pedirClave()) {
        <div>
          <label class="form-label" for="usr-password">{{ etiquetaClave() }}</label>
          <div class="lr-password">
            <input
              id="usr-password"
              class="form-control"
              formControlName="password"
              [type]="mostrarClave() ? 'text' : 'password'"
              autocomplete="new-password"
              [class.is-invalid]="invalido('password')"
            />
            <button type="button" (click)="mostrarClave.set(!mostrarClave())" [attr.aria-label]="mostrarClave() ? 'Ocultar contraseña' : 'Mostrar contraseña'">
              <app-icono [nombre]="mostrarClave() ? 'ocultar' : 'ver'" [tamano]="16" />
            </button>
          </div>
          <div class="invalid-feedback">Mínimo 8 caracteres.</div>
        </div>

        <div>
          <label class="form-label" for="usr-confirmacion">Confirmar contraseña</label>
          <input
            id="usr-confirmacion"
            class="form-control"
            formControlName="confirmacion"
            [type]="mostrarClave() ? 'text' : 'password'"
            autocomplete="new-password"
            [class.is-invalid]="formulario.hasError('noCoinciden') && formulario.controls.confirmacion.touched"
          />
          <div class="invalid-feedback">Las contraseñas no coinciden.</div>
        </div>
      }

      @if (!soloClave()) {
      <div>
        <label class="form-label" for="usr-rol">Rol permanente</label>
        <select id="usr-rol" class="form-select" formControlName="rol" [class.is-invalid]="invalido('rol')">
          <option value="">Selecciona un rol</option>
          @for (rol of roles; track rol[0]) {
            <option [value]="rol[0]">{{ rol[1] }}</option>
          }
        </select>
        <div class="invalid-feedback">Selecciona el rol permanente.</div>
      </div>

      <div>
        <label class="form-label" for="usr-estado">Estado</label>
        <select id="usr-estado" class="form-select" formControlName="activo">
          <option [ngValue]="true">Activo (puede iniciar sesión)</option>
          <option [ngValue]="false">Inactivo (no puede iniciar sesión)</option>
        </select>
        @if (archivado()) {
          <div class="form-text">Este usuario está archivado y no puede reactivarse desde Editar.</div>
        }
      </div>
      }

      @if (error()) {
        <div class="alert alert-danger py-2 small mb-0 lr-campo--completo" role="alert">{{ error() }}</div>
      }

      <div class="lr-formulario__pie">
        <button type="button" class="btn btn-outline-secondary" (click)="cancelar.emit()">Cancelar</button>
        <button type="submit" class="btn btn-primary" [disabled]="!soloClave() && pedirClave() && empleados().length === 0">
          {{ etiquetaGuardar() }}
        </button>
      </div>
    </form>
  `,
})
export class UsuarioFormulario {
  private readonly fb = inject(NonNullableFormBuilder);

  readonly empleados = input<readonly Empleado[]>([]);
  readonly empleadoId = input<number | null>(null);
  readonly username = input('');
  readonly rol = input<Rol | ''>('');
  readonly activo = input(true);
  readonly archivado = input(false);
  readonly pedirClave = input(true);
  readonly soloClave = input(false);
  readonly error = input<string | null>(null);
  readonly etiquetaGuardar = input('Crear usuario');
  readonly etiquetaClave = input('Contraseña temporal');
  readonly guardar = output<AltaAccesoFormulario>();
  readonly cancelar = output<void>();

  protected readonly roles = Object.entries(ROL_ETIQUETA) as [Rol, string][];
  protected readonly mostrarClave = signal(false);

  readonly formulario = this.fb.group(
    {
      empleadoId: [0, [Validators.min(1)]],
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmacion: [''],
      rol: ['', Validators.required],
      activo: [true],
    },
    { validators: clavesCoinciden },
  );

  constructor() {
    effect(() => {
      const pideClave = this.pedirClave();
      const password = this.formulario.controls.password;
      const confirmacion = this.formulario.controls.confirmacion;
      if (pideClave) {
        password.setValidators([Validators.required, Validators.minLength(8)]);
        confirmacion.setValidators([Validators.required]);
      } else {
        password.clearValidators();
        confirmacion.clearValidators();
        password.setValue('');
        confirmacion.setValue('');
      }
      password.updateValueAndValidity({ emitEvent: false });
      confirmacion.updateValueAndValidity({ emitEvent: false });
    });

    effect(() => {
      this.formulario.patchValue({
        empleadoId: this.empleadoId() ?? 0,
        username: this.username(),
        rol: this.rol(),
        activo: this.activo(),
      });
      if (this.empleadoId()) {
        this.formulario.controls.empleadoId.disable();
      } else {
        this.formulario.controls.empleadoId.enable();
      }
      if (this.archivado()) {
        this.formulario.controls.activo.disable({ emitEvent: false });
      } else {
        this.formulario.controls.activo.enable({ emitEvent: false });
      }
    });
  }

  protected nombreDe = nombreCompletoDe;

  protected invalido(nombre: 'empleadoId' | 'username' | 'password' | 'rol'): boolean {
    const control = this.formulario.get(nombre);
    return !!control && control.invalid && (control.dirty || control.touched);
  }

  protected enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const valor = this.formulario.getRawValue();
    this.guardar.emit({
      empleadoId: valor.empleadoId,
      username: valor.username.trim(),
      rol: valor.rol as Rol,
      activo: valor.activo,
      password: valor.password,
    });
  }
}
