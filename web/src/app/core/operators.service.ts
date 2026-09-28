import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ConfigService } from './config.service';
import { IdResponse } from './admin.service';

@Injectable({ providedIn: 'root' })
export class OperatorsService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  create(name: string, email: string, password: string) {
    return this.http.post<IdResponse>(`${this.config.url()}/operators`, { name, email, password });
  }
}
