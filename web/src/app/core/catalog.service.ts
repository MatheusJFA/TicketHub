import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { ConfigService } from './config.service';
import { Pagination, SectionSummary, ShowDetail, ShowSummary, SpotItem } from './models';

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(ConfigService);

  listShows(search = '', page = 0, perPage = 12) {
    const params = new HttpParams({ fromObject: { search, page, perPage, sort: 'date', dir: 'asc' } });
    return this.http.get<Pagination<ShowSummary>>(`${this.config.url()}/shows`, { params });
  }

  getShow(id: string) {
    return this.http.get<ShowDetail>(`${this.config.url()}/shows/${id}`);
  }

  listSpots(search = '', page = 0, perPage = 60) {
    const params = new HttpParams({ fromObject: { search, page, perPage, sort: 'createdAt', dir: 'asc' } });
    return this.http.get<Pagination<SpotItem>>(`${this.config.url()}/spots`, { params });
  }

  listShowSections(showId: string, page = 0, perPage = 20) {
    const params = new HttpParams({ fromObject: { page, perPage, sort: 'name', dir: 'asc' } });
    return this.http.get<Pagination<SectionSummary>>(`${this.config.url()}/shows/${showId}/sections`, {
      params,
    });
  }

  listSectionSpots(sectionId: string, page = 0, perPage = 100) {
    const params = new HttpParams({ fromObject: { page, perPage, sort: 'createdAt', dir: 'asc' } });
    return this.http.get<Pagination<SpotItem>>(`${this.config.url()}/sections/${sectionId}/spots`, {
      params,
    });
  }
}
