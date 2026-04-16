/**
 * Page information metadata for paginated responses.
 */
export interface PageInfo {
  /** Number of items per page */
  size: number;
  
  /** Current page number (zero-based) */
  number: number;
  
  /** Total number of elements across all pages */
  totalElements: number;
  
  /** Total number of pages */
  totalPages: number;
}

/**
 * Generic paginated response wrapper for API endpoints.
 * Provides consistent pagination structure across the application.
 * 
 * Matches backend PageResponse structure exactly.
 * 
 * @template T The type of content in the page
 */
export interface Page<T> {
  /** Array of content items for the current page */
  content: T[];
  
  /** Page metadata information */
  page: PageInfo;
  
  /** Optional navigation links (self, first, next, last) */
  links?: {
    [key: string]: string;
  };
}

/** Raw Spring Data `Page` JSON (flat pagination fields). */
export interface SpringPagePayload<T> {
  content?: T[];
  size?: number;
  number?: number;
  totalElements?: number;
  totalPages?: number;
  page?: PageInfo;
  links?: Record<string, string>;
}

/**
 * Maps Spring Boot {@code Page} JSON to the app's nested {@link Page} shape.
 */
export function mapSpringPageToAppPage<T>(raw: SpringPagePayload<T>): Page<T> {
  const content = raw.content ?? [];
  const nested = raw.page;
  const page: PageInfo = nested
    ? {
        size: nested.size,
        number: nested.number,
        totalElements: nested.totalElements,
        totalPages: nested.totalPages
      }
    : {
        size: raw.size ?? content.length,
        number: raw.number ?? 0,
        totalElements: raw.totalElements ?? content.length,
        totalPages: raw.totalPages ?? (content.length > 0 ? 1 : 0)
      };
  return { content, page, links: raw.links };
}
