import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { I18nService } from '../../core/i18n/i18n.service';
import { OperatorsService } from '../../core/operators.service';
import { parseApiError } from '../../core/api-error';

@Component({
  selector: 'app-admin-operators',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-operators.component.html',
})
export class AdminOperatorsComponent {
  private readonly operators = inject(OperatorsService);
  readonly i18n = inject(I18nService);

  name = '';
  email = '';
  password = '';
  readonly creating = signal(false);
  readonly error = signal<string | null>(null);
  readonly createdId = signal<string | null>(null);

  submit(): void {
    this.error.set(null);
    this.createdId.set(null);
    this.creating.set(true);
    this.operators.create(this.name.trim(), this.email.trim(), this.password).subscribe({
      next: (res) => {
        this.creating.set(false);
        this.createdId.set(res.id);
        this.name = '';
        this.email = '';
        this.password = '';
      },
      error: (err) => {
        this.creating.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }
}
