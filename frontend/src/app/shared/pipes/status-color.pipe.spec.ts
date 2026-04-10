import { StatusColorPipe } from './status-color.pipe';
import { LifecycleStatus, TicketStatus } from '../models';

describe('StatusColorPipe', () => {
  let pipe: StatusColorPipe;

  beforeEach(() => {
    pipe = new StatusColorPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  describe('LifecycleStatus color mapping', () => {
    it('should return info class for ORDERED status', () => {
      const result = pipe.transform(LifecycleStatus.ORDERED, 'class');
      expect(result).toBe('status-info');
    });

    it('should return primary class for RECEIVED status', () => {
      const result = pipe.transform(LifecycleStatus.RECEIVED, 'class');
      expect(result).toBe('status-primary');
    });

    it('should return success class for IN_USE status', () => {
      const result = pipe.transform(LifecycleStatus.IN_USE, 'class');
      expect(result).toBe('status-success');
    });

    it('should return warning class for IN_MAINTENANCE status', () => {
      const result = pipe.transform(LifecycleStatus.IN_MAINTENANCE, 'class');
      expect(result).toBe('status-warning');
    });

    it('should return secondary class for IN_STORAGE status', () => {
      const result = pipe.transform(LifecycleStatus.IN_STORAGE, 'class');
      expect(result).toBe('status-secondary');
    });

    it('should return dark class for RETIRED status', () => {
      const result = pipe.transform(LifecycleStatus.RETIRED, 'class');
      expect(result).toBe('status-dark');
    });

    it('should return dark class for DISPOSED status', () => {
      const result = pipe.transform(LifecycleStatus.DISPOSED, 'class');
      expect(result).toBe('status-dark');
    });
  });

  describe('TicketStatus color mapping', () => {
    it('should return warning class for PENDING status', () => {
      const result = pipe.transform(TicketStatus.PENDING, 'class');
      expect(result).toBe('status-warning');
    });

    it('should return success class for APPROVED status', () => {
      const result = pipe.transform(TicketStatus.APPROVED, 'class');
      expect(result).toBe('status-success');
    });

    it('should return danger class for REJECTED status', () => {
      const result = pipe.transform(TicketStatus.REJECTED, 'class');
      expect(result).toBe('status-danger');
    });

    it('should return success class for COMPLETED status', () => {
      const result = pipe.transform(TicketStatus.COMPLETED, 'class');
      expect(result).toBe('status-success');
    });

    it('should return secondary class for CANCELLED status', () => {
      const result = pipe.transform(TicketStatus.CANCELLED, 'class');
      expect(result).toBe('status-secondary');
    });
  });

  describe('Hex color output', () => {
    it('should return hex color for ORDERED status', () => {
      const result = pipe.transform(LifecycleStatus.ORDERED, 'hex');
      expect(result).toBe('#0dcaf0');
    });

    it('should return hex color for APPROVED status', () => {
      const result = pipe.transform(TicketStatus.APPROVED, 'hex');
      expect(result).toBe('#198754');
    });

    it('should return hex color for REJECTED status', () => {
      const result = pipe.transform(TicketStatus.REJECTED, 'hex');
      expect(result).toBe('#dc3545');
    });

    it('should return hex color for IN_USE status', () => {
      const result = pipe.transform(LifecycleStatus.IN_USE, 'hex');
      expect(result).toBe('#198754');
    });
  });

  describe('Edge cases', () => {
    it('should return default class for null', () => {
      const result = pipe.transform(null, 'class');
      expect(result).toBe('status-default');
    });

    it('should return default hex for null', () => {
      const result = pipe.transform(null, 'hex');
      expect(result).toBe('#6c757d');
    });

    it('should return default class for undefined', () => {
      const result = pipe.transform(undefined, 'class');
      expect(result).toBe('status-default');
    });

    it('should return default class for unknown status', () => {
      const result = pipe.transform('UNKNOWN_STATUS', 'class');
      expect(result).toBe('status-default');
    });

    it('should return default hex for unknown status', () => {
      const result = pipe.transform('UNKNOWN_STATUS', 'hex');
      expect(result).toBe('#6c757d');
    });

    it('should use class output type by default', () => {
      const result = pipe.transform(LifecycleStatus.IN_USE);
      expect(result).toBe('status-success');
    });
  });

  describe('String status values', () => {
    it('should handle string status values (case insensitive)', () => {
      const result = pipe.transform('in_use', 'class');
      expect(result).toBe('status-success');
    });

    it('should handle uppercase string status values', () => {
      const result = pipe.transform('PENDING', 'class');
      expect(result).toBe('status-warning');
    });
  });
});
