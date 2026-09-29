import { ChangeDetectionStrategy, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, debounceTime } from 'rxjs';
import { AuditService, AuditEntry } from '../../admin/audit.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { ListErrorComponent } from '../../shared/ui/list-error.component';
import { PaginationComponent } from '../../shared/ui/pagination.component';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';

@Component({
  selector: 'app-admin-audit',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, DatePipe, RouterLink, ListErrorComponent, PaginationComponent, IconComponent],
  templateUrl: './admin-audit.component.html',
})
export class AdminAuditComponent {
  private readonly audit = inject(AuditService);
  readonly i18n = inject(I18nService);

  action = '';
  actor = '';
  outcome = '';
  readonly perPage = 20;

  private readonly query = signal({ action: '', actor: '', outcome: '', page: 0 });
  private readonly filter$ = new Subject<void>();

  private readonly resource = rxResource({
    params: () => this.query(),
    stream: ({ params }) =>
      this.audit.list(
        { action: params.action, actor: params.actor, outcome: params.outcome },
        params.page,
        this.perPage,
      ),
  });

  readonly page = computed(() => this.query().page);
  readonly items = computed<AuditEntry[]>(() => this.resource.value()?.items ?? []);
  readonly total = computed(() => this.resource.value()?.totalItems ?? 0);
  readonly loading = this.resource.isLoading;
  readonly error = computed(() => {
    const err = this.resource.error();
    return err ? parseApiError(err as unknown, this.i18n.t('errors.generic')) : null;
  });

  constructor() {
    this.filter$
      .pipe(debounceTime(500), takeUntilDestroyed(inject(DestroyRef)))
      .subscribe(() => this.load());
    // Carrega a primeira página imediatamente (sem esperar o debounce).
    this.load();
  }

  onFilterChange(): void {
    this.filter$.next();
  }

  load(reset = true): void {
    if (reset) {
      this.query.set({ action: this.action, actor: this.actor, outcome: this.outcome, page: 0 });
    } else {
      this.resource.reload();
    }
  }

  clear(): void {
    this.action = '';
    this.actor = '';
    this.outcome = '';
    this.load();
  }

  goTo(target: number): void {
    this.query.update((q) => ({ ...q, page: target }));
  }
}
