import { DateFormatPipe } from './date-format.pipe';

describe('DateFormatPipe', () => {
  let pipe: DateFormatPipe;

  beforeEach(() => {
    pipe = new DateFormatPipe();
  });

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  describe('Date object formatting', () => {
    it('should format Date object with short format', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const result = pipe.transform(date, 'short');
      expect(result).toMatch(/1\/15\/24/);
    });

    it('should format Date object with medium format (default)', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const result = pipe.transform(date);
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should format Date object with long format', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const result = pipe.transform(date, 'long');
      expect(result).toContain('January');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should format Date object with full format', () => {
      const date = new Date('2024-01-15T10:30:00Z');
      const result = pipe.transform(date, 'full');
      expect(result).toContain('January');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });
  });

  describe('ISO string formatting', () => {
    it('should format ISO string with short format', () => {
      const isoString = '2024-01-15T10:30:00Z';
      const result = pipe.transform(isoString, 'short');
      expect(result).toMatch(/1\/15\/24/);
    });

    it('should format ISO string with medium format', () => {
      const isoString = '2024-01-15T10:30:00Z';
      const result = pipe.transform(isoString, 'medium');
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });

    it('should format date-only ISO string', () => {
      const isoString = '2024-01-15';
      const result = pipe.transform(isoString, 'medium');
      expect(result).toContain('Jan');
      expect(result).toContain('15');
      expect(result).toContain('2024');
    });
  });

  describe('Edge cases', () => {
    it('should return empty string for null', () => {
      const result = pipe.transform(null);
      expect(result).toBe('');
    });

    it('should return empty string for undefined', () => {
      const result = pipe.transform(undefined);
      expect(result).toBe('');
    });

    it('should return empty string for invalid date string', () => {
      const result = pipe.transform('invalid-date');
      expect(result).toBe('');
    });

    it('should return empty string for empty string', () => {
      const result = pipe.transform('');
      expect(result).toBe('');
    });
  });
});
