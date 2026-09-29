import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from '../core/config.service';

export interface ZipLookup {
  zipCode: string;
  street: string;
  neighborhood: string;
  city: string;
  state: string;
  country: string;
}

@Injectable({ providedIn: 'root' })
export class ZipcodeService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  lookup(zip: string) {
    const clean = zip.replace(/\D/g, '');
    return this.http.get<ZipLookup>(`${this.config.url()}/zipcode/${clean}`);
  }
}
