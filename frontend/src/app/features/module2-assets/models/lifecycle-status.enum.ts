/**
 * Enumeration of asset lifecycle statuses.
 * Defines the 7 standard lifecycle stages from acquisition to retirement.
 * 
 * Must match backend LifecycleStatus enum exactly.
 */
export enum LifecycleStatus {
  ORDERED = 'ORDERED',
  RECEIVED = 'RECEIVED',
  DEPLOYED = 'DEPLOYED',
  IN_USE = 'IN_USE',
  MAINTENANCE = 'MAINTENANCE',
  STORAGE = 'STORAGE',
  RETIRED = 'RETIRED'
}
