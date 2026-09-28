import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { I18nService } from '../../core/i18n/i18n.service';
import { ConfirmService } from '../../core/confirm.service';
import { PartnersService, PartnerProfile } from '../../core/partners.service';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../core/icon.component';

@Component({
  selector: 'app-admin-partners',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DatePipe, RouterLink, IconComponent],
  templateUrl: './admin-partners.component.html',
})
export class AdminPartnersComponent {
  private readonly partners = inject(PartnersService);
  private readonly confirm = inject(ConfirmService);
  readonly i18n = inject(I18nService);

  readonly items = signal<PartnerProfile[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly busy = signal<string | null>(null);

  constructor() {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.partners.list(0, 50).subscribe({
      next: (page) => {
        this.items.set(page.items);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.i18n.t('errors.generic'));
        this.loading.set(false);
      },
    });
  }

  approve(id: string): void {
    this.act(id, (x) => this.partners.approve(x));
  }

  reject(id: string): void {
    this.act(id, (x) => this.partners.reject(x));
  }

  async remove(id: string): Promise<void> {
    if (!(await this.confirm.ask(this.i18n.t('partners.confirmRemove')))) return;
    this.act(id, (x) => this.partners.remove(x));
  }

  private act(id: string, call: (id: string) => { subscribe(o: object): void }): void {
    this.busy.set(id);
    this.error.set(null);
    call(id).subscribe({
      next: () => {
        this.busy.set(null);
        this.reload();
      },
      error: (err: { error?: { errors?: { message: string }[] } }) => {
        this.busy.set(null);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }
}
