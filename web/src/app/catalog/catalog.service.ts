import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { EMPTY, Observable, expand, map, reduce } from 'rxjs';
import { ConfigService } from '../core/config.service';
import { Pagination, SectionSummary, ShowDetail, ShowSummary, SpotItem } from '../core/models';

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

  /**
   * Fetches every spot of a section sequentially (perPage <= 100, backend limit).
   * Emits once with the concatenated items. Stops on totalItems, short page or maxPages.
   */
  listAllSectionSpots(sectionId: string, perPage = 100, maxPages = 30): Observable<SpotItem[]> {
    const safePerPage = Math.max(1, Math.min(100, Math.floor(perPage) || 100));
    const safeMaxPages = Math.max(1, Math.min(100, Math.floor(maxPages) || 30));
    return this.listSectionSpots(sectionId, 0, safePerPage).pipe(
      expand((page, pageIndex) => {
        const fetched = (pageIndex + 1) * page.perPage;
        if (page.items.length === 0 || fetched >= page.totalItems || pageIndex + 1 >= safeMaxPages) {
          return EMPTY;
        }
        return this.listSectionSpots(sectionId, pageIndex + 1, safePerPage);
      }),
      map((page) => page.items),
      reduce((acc, items) => [...acc, ...items], [] as SpotItem[]),
    );
  }
}
