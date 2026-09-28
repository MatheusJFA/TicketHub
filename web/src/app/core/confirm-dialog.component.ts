import { ChangeDetectionStrategy, Component, effect, ElementRef, inject, viewChild } from '@angular/core';
import { ConfirmService } from './confirm.service';
import { I18nService } from './i18n/i18n.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @if (confirm.pending()) {
      <div
        class="fixed inset-0 z-50 grid place-items-center bg-black/50 p-4"
        (click)="onBackdrop($event)">
        <div
          role="alertdialog"
          aria-modal="true"
          aria-labelledby="confirm-title"
          aria-describedby="confirm-msg"
          class="th-ticket w-full max-w-sm p-6"
          (keydown.escape)="confirm.cancel()">
          <h2 id="confirm-title" class="th-display text-xl">{{ i18n.t('common.confirmTitle') }}</h2>
          <p id="confirm-msg" class="mt-2 text-sm font-medium">{{ confirm.pending()?.message }}</p>
          <div class="mt-5 flex justify-end gap-2">
            <button #cancelBtn type="button" (click)="confirm.cancel()" class="th-btn-ghost !px-4 !py-2 !text-xs">
              {{ i18n.t('common.cancel') }}
            </button>
            <button #confirmBtn type="button" (click)="confirm.confirm()" class="th-btn-stamp !px-4 !py-2 !text-xs">
              {{ i18n.t('common.confirm') }}
            </button>
          </div>
        </div>
      </div>
    }
  `,
})
export class ConfirmDialogComponent {
  readonly confirm = inject(ConfirmService);
  readonly i18n = inject(I18nService);
  private readonly confirmBtn =
    viewChild<ElementRef<HTMLButtonElement>>('confirmBtn');

  constructor() {
    // Foca o botão de confirmação ao abrir (a11y).
    effect(() => {
      if (this.confirm.pending()) {
        queueMicrotask(() => this.confirmBtn()?.nativeElement.focus());
      }
    });
  }

  onBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.confirm.cancel();
  }
}
