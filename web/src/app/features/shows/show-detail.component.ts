import { ChangeDetectionStrategy, Component, effect, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CatalogService } from '../../core/catalog.service';
import { I18nService } from '../../core/i18n/i18n.service';
import { ListErrorComponent } from '../../core/list-error.component';
import { ShowDetail } from '../../core/models';

@Component({
  selector: 'app-show-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, DatePipe, ListErrorComponent],
  templateUrl: './show-detail.component.html',
})
export class ShowDetailComponent {
  private readonly catalog = inject(CatalogService);
  readonly i18n = inject(I18nService);

  /** Recebido via withComponentInputBinding (rota shows/:id). Reage a id->id sem recreate. */
  readonly id = input<string>('');

  readonly show = signal<ShowDetail | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  constructor() {
    effect(() => {
      const id = this.id();
      if (id) this.load(id);
    });
  }

  retry(): void {
    const id = this.id();
    if (id) this.load(id);
  }

  private load(id: string): void {    this.loading.set(true);
    this.error.set(null);
    this.catalog.getShow(id).subscribe({
      next: (show) => {
        this.show.set(show);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(this.i18n.t('errors.notFoundShow'));
        this.loading.set(false);
      },
    });
  }
}
