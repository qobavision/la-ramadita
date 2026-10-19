import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { mensajeDeError } from '../../core/errores/api-error';
import { Usuario } from '../auth.models';
import { AuthService } from '../auth.service';
import { rutaInicioDe } from '../paneles';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly formulario = this.fb.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  readonly mostrarPassword = signal(false);
  readonly enviando = signal(false);
  readonly error = signal<string | null>(null);
  readonly aviso = signal<string | null>(null);

  constructor() {
    if (this.route.snapshot.queryParamMap.get('motivo') === 'expirada') {
      this.aviso.set('Tu sesión expiró o fue cerrada. Vuelve a iniciar sesión.');
    }
  }

  campoInvalido(nombre: 'username' | 'password'): boolean {
    const control = this.formulario.controls[nombre];
    return control.invalid && (control.touched || control.dirty);
  }

  alternarPassword(): void {
    this.mostrarPassword.update((visible) => !visible);
  }

  ingresar(): void {
    if (this.enviando()) {
      return;
    }
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }

    const { username, password } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.aviso.set(null);

    this.auth
      .login({ username: username.trim(), password })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (usuario) => void this.router.navigateByUrl(this.destinoTrasLogin(usuario)),
        error: (err: unknown) => {
          this.enviando.set(false);
          this.error.set(mensajeDeError(err, 'No se pudo iniciar sesión. Inténtalo nuevamente.'));
        },
      });
  }

  /**
   * Vuelve a la ruta interna que pidió el usuario antes de ser enviado al login;
   * si no hay ninguna, va al panel de su rol. Si la ruta pedida pertenece a
   * otro panel, el guard de rol lo redirigirá a su propio panel.
   */
  private destinoTrasLogin(usuario: Usuario): string {
    const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
    const esInterna = !!returnUrl && returnUrl.startsWith('/') && !returnUrl.startsWith('/login');
    return esInterna ? returnUrl : rutaInicioDe(usuario);
  }
}
