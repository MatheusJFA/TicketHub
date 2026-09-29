import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toasts',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="pointer-events-none fixed bottom-4 left-1/2 z-50 flex w-full max-w-md -translate-x-1/2 flex-col items-center gap-2 px-4" aria-live="polite" role="status">
      @for (toast of toasts.toasts(); track toast.id) {
        <div class="pointer-events-auto flex w-full items-center justify-between gap-3 rounded-xl border-[1.5px] border-[#2a241a] bg-[#2a241a] px-4 py-2.5 text-sm font-bold text-[#fdf8ec] shadow-lg">
          <span>{{ toast.message }}</span>
          <button type="button" (click)="toasts.dismiss(toast.id)" class="shrink-0 underline opacity-80 hover:opacity-100" aria-label="Dismiss">✕</button>
        </div>
      }
    </div>
  `,
})
export class ToastsComponent {
  readonly toasts = inject(ToastService);
}
