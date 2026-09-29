import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';

@Component({
  selector: 'app-signup',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './signup.component.html',
})
export class SignupComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly i18n = inject(I18nService);

  readonly form = inject(FormBuilder).nonNullable.group({
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/)]],
    name: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
  });
  readonly showPassword = signal(false);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.loading.set(true);
    const { cpf, name, email, password } = this.form.getRawValue();
    this.auth.signup(cpf.trim(), name.trim(), email.trim(), password).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/login']);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.signupFailed')));
      },
    });
  }
}
