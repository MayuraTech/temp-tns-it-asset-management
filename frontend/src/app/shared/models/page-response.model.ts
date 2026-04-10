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
