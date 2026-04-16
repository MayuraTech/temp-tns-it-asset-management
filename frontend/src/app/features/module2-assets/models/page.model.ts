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
