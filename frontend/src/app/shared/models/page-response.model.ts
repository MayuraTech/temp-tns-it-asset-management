/**
 * Paginated response models
 */

/**
 * Generic paginated response interface
 */
export interface PageResponse<T> {
  content: T[];
  page: PageInfo;
  links?: PageLinks;
}

/** Raw Spring Data `Page` JSON (flat pagination fields). */
export interface SpringPagePayload<T> {
  content?: T[];
  size?: number;
  number?: number;
  totalElements?: number;
  totalPages?: number;
  page?: PageInfo;
  links?: PageLinks;
}

/**
 * Maps Spring Boot {@code Page} JSON to {@link PageResponse} (nested {@code page}).
 */
export function mapSpringToPageResponse<T>(raw: SpringPagePayload<T>): PageResponse<T> {
  const content = raw.content ?? [];
  const nested = raw.page;
  const page: PageInfo = nested
    ? { ...nested }
    : {
        size: raw.size ?? content.length,
        number: raw.number ?? 0,
        totalElements: raw.totalElements ?? content.length,
        totalPages: raw.totalPages ?? (content.length > 0 ? 1 : 0)
      };
  return { content, page, links: raw.links };
}

/**
 * Page information interface
 */
export interface PageInfo {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

/**
 * Page navigation links interface
 */
export interface PageLinks {
  self?: string;
  first?: string;
  prev?: string;
  next?: string;
  last?: string;
}

/**
 * Pageable request parameters interface
 */
export interface Pageable {
  page: number;
  size: number;
  sort?: string[];
}
