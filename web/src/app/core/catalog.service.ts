import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Pagination, ShowDetail, ShowSummary, SpotItem } from './models';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  listShows(search = '', page = 0, perPage = 12) {
    const params = new HttpParams({ fromObject: { search, page, perPage, sort: 'date', dir: 'asc' } });
    return this.http.get<Pagination<ShowSummary>>(`${this.base}/shows`, { params });
  }

  getShow(id: string) {
    return this.http.get<ShowDetail>(`${this.base}/shows/${id}`);
  }

  listSpots(search = '', page = 0, perPage = 60) {
    const params = new HttpParams({ fromObject: { search, page, perPage, sort: 'createdAt', dir: 'asc' } });
    return this.http.get<Pagination<SpotItem>>(`${this.base}/spots`, { params });
  }
}
