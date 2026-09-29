import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/auth.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';
import { ConfigService } from '../../core/config.service';

interface DemoAccount {
  label: string;
  email: string;
  password: string;
}

@Component({
  selector: 'app-login',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly i18n = inject(I18nService);
  readonly config = inject(ConfigService);

  readonly form = inject(FormBuilder).nonNullable.group({
    identifier: ['', [Validators.required, Validators.minLength(3)]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });
  readonly showPassword = signal(false);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);

  /** Seed-dev accounts (scripts/seed-dev.js). Fill-only; user still submits. */
  readonly demoAccounts: DemoAccount[] = [
    { label: 'Admin', email: 'admin@tickethub.local', password: 'admin-local' },
    { label: 'Customer', email: 'customer@tickethub.local', password: 'customer-local' },
    { label: 'Partner', email: 'partner@tickethub.local', password: 'partner-local' },
  ];

  fillDemo(account: DemoAccount): void {
    this.error.set(null);
    this.form.setValue({ identifier: account.email, password: account.password });
  }

  submit(): void {
    if (this.form.invalid || this.loading()) {
      this.form.markAllAsTouched();
      return;
    }
    this.error.set(null);
    this.loading.set(true);
    const { identifier, password } = this.form.getRawValue();
    this.auth.login(identifier.trim(), password).subscribe({
      next: () => {
        this.loading.set(false);
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.invalidCreds')));
      },
    });
  }
}
