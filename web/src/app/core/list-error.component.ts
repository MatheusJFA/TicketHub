import { ChangeDetectionStrategy, Component, inject, input, output } from '@angular/core';
import { I18nService } from './i18n/i18n.service';

/** Caixa de erro padrão das listas: mensagem + botão tentar novamente. */
@Component({
  selector: 'app-list-error',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="th-alert-error" role="alert">
      {{ message() }}
      <button type="button" (click)="retry.emit()" class="font-bold underline">
        {{ i18n.t('checkout.retry') }}
      </button>
    </div>
  `,
})
export class ListErrorComponent {
  readonly i18n = inject(I18nService);
  readonly message = input<string>('');
  readonly retry = output<void>();
}
