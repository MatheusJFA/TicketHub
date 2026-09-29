import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../auth/auth.service';
import { I18nService } from '../../shared/i18n/i18n.service';
import { PartnersService, PartnerProfile } from '../../admin/partners.service';
import { ZipcodeService } from '../../shared/zipcode.service';
import { Address } from '../../core/models';
import { parseApiError } from '../../core/api-error';
import { IconComponent } from '../../shared/ui/icon.component';

const EMPTY_ADDRESS: Address = {
  street: '',
  number: '',
  complement: null,
  neighborhood: '',
  city: '',
  state: '',
  country: 'Brasil',
  zipCode: '',
};

@Component({
  selector: 'app-partner',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [FormsModule, IconComponent],
  templateUrl: './partner.component.html',
})
export class PartnerComponent {
  readonly auth = inject(AuthService);
  readonly i18n = inject(I18nService);
  private readonly partners = inject(PartnersService);
  private readonly zipcode = inject(ZipcodeService);

  // signup
  name = '';
  cnpj = '';
  email = '';
  password = '';
  address: Address = { ...EMPTY_ADDRESS };
  readonly signingUp = signal(false);
  readonly signupDone = signal(false);

  // profile
  readonly profile = signal<PartnerProfile | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly saved = signal(false);
  webhookUrl = '';
  webhookSecret = '';

  readonly error = signal<string | null>(null);

  constructor() {
    if (this.auth.canManageCatalog && this.auth.customerId()) {
      this.loadProfile(this.auth.customerId()!);
    }
  }

  get isPartner(): boolean {
    return this.auth.canManageCatalog && this.profile() !== null;
  }

  loadProfile(id: string): void {
    this.loading.set(true);
    this.partners.get(id).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.name = profile.name;
        this.address = { ...profile.address };
        this.loading.set(false);
      },
      error: () => {
        this.profile.set(null);
        this.loading.set(false);
      },
    });
  }

  /** Autofills street/neighborhood/city/state from the zip code (ViaCep). */
  lookupZip(): void {
    const zip = (this.address.zipCode ?? '').replace(/\D/g, '');
    if (zip.length < 8) return;
    this.zipcode.lookup(zip).subscribe({
      next: (found) => {
        if (found.street) this.address.street = found.street;
        if (found.neighborhood) this.address.neighborhood = found.neighborhood;
        if (found.city) this.address.city = found.city;
        if (found.state) this.address.state = found.state;
        if (found.country) this.address.country = found.country;
      },
      error: () => undefined,
    });
  }

  signup(): void {    this.error.set(null);
    this.signingUp.set(true);
    this.partners
      .signup({ name: this.name, cnpj: this.cnpj, address: this.address, email: this.email, password: this.password })
      .subscribe({
        next: () => {
          this.signingUp.set(false);
          this.signupDone.set(true);
        },
        error: (err) => {
          this.signingUp.set(false);
          this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
        },
      });
  }

  save(): void {
    const id = this.auth.customerId();
    if (!id) return;
    this.saving.set(true);
    this.saved.set(false);
    this.error.set(null);
    this.partners.update(id, { name: this.name, address: this.address }).subscribe({
      next: () => {
        const needsWebhook = this.webhookUrl.trim() || this.webhookSecret.trim();
        if (!needsWebhook) {
          this.saving.set(false);
          this.saved.set(true);
          this.loadProfile(id);
          return;
        }
        this.partners.changeWebhook(id, this.webhookUrl.trim(), this.webhookSecret.trim()).subscribe({
          next: () => {
            this.saving.set(false);
            this.saved.set(true);
            this.loadProfile(id);
          },
          error: (err) => {
            this.saving.set(false);
            this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
          },
        });
      },
      error: (err) => {
        this.saving.set(false);
        this.error.set(parseApiError(err, this.i18n.t('errors.generic')));
      },
    });
  }
}
