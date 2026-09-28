import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { I18nService } from './i18n.service';

/** Segmented PT / EN / ES switcher for the header. */
@Component({
  selector: 'app-language-switcher',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="flex items-center rounded-full border-[1.5px] border-[#2a241a] bg-[#f3ecdd] p-0.5 text-xs font-bold"
      role="group"
      aria-label="Language / Idioma">
      @for (option of i18n.langs; track option.id) {
        <button
          type="button"
          (click)="i18n.setLang(option.id)"
          [attr.aria-pressed]="i18n.lang() === option.id"
          [class.bg-[#2a241a]]="i18n.lang() === option.id"
          [class.text-[#fdf8ec]]="i18n.lang() === option.id"
          [class.shadow-sm]="i18n.lang() === option.id"
          [class.text-[#6f6353]]="i18n.lang() !== option.id"
          class="rounded-full px-2.5 py-1.5 transition hover:text-[#2a241a]">
          {{ option.label }}
        </button>
      }
    </div>
  `,
})
export class LanguageSwitcherComponent {
  readonly i18n = inject(I18nService);
}
