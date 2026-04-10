import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { LifecycleTimelineComponent, LifecycleEvent } from './lifecycle-timeline.component';
import { LifecycleStatus } from '../../../models';

describe('LifecycleTimelineComponent', () => {
  let component: LifecycleTimelineComponent;
  let fixture: ComponentFixture<LifecycleTimelineComponent>;
  let debugElement: DebugElement;

  const mockEvents: LifecycleEvent[] = [
    {
      status: LifecycleStatus.IN_USE,
      date: '2024-01-15T10:30:00Z',
      description: 'Asset deployed to production',
      icon: 'play_circle',
      user: 'John Doe'
    },
    {
      status: LifecycleStatus.DEPLOYED,
      date: '2024-01-10T09:15:00Z',
      description: 'Asset deployed to staging',
      icon: 'rocket_launch',
      user: 'Jane Smith'
    },
    {
      status: LifecycleStatus.RECEIVED,
      date: '2024-01-05T14:20:00Z',
      description: 'Asset received in inventory',
      icon: 'inventory',
      user: 'Admin User'
    }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        LifecycleTimelineComponent,
        MatIconModule,
        MatTooltipModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LifecycleTimelineComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Events Display', () => {
    beforeEach(() => {
      component.events = mockEvents;
      fixture.detectChanges();
    });

    it('should display timeline container when events exist', () => {
      const container = debugElement.query(By.css('.timeline-container'));
      expect(container).toBeTruthy();
    });

    it('should display timeline title', () => {
      const title = debugElement.query(By.css('.timeline-title'));
      expect(title.nativeElement.textContent.trim()).toBe('Lifecycle History');
    });

    it('should render all timeline items', () => {
      const timelineItems = debugElement.queryAll(By.css('.timeline-item'));
      expect(timelineItems.length).toBe(3);
    });

    it('should display event descriptions', () => {
      const eventTitles = debugElement.queryAll(By.css('.event-title'));
      expect(eventTitles[0].nativeElement.textContent.trim()).toBe('Asset deployed to production');
      expect(eventTitles[1].nativeElement.textContent.trim()).toBe('Asset deployed to staging');
      expect(eventTitles[2].nativeElement.textContent.trim()).toBe('Asset received in inventory');
    });

    it('should display event icons', () => {
      const icons = debugElement.queryAll(By.css('.timeline-icon mat-icon'));
      expect(icons[0].nativeElement.textContent.trim()).toBe('play_circle');
      expect(icons[1].nativeElement.textContent.trim()).toBe('rocket_launch');
      expect(icons[2].nativeElement.textContent.trim()).toBe('inventory');
    });

    it('should display user information when available', () => {
      const userElements = debugElement.queryAll(By.css('.event-user'));
      expect(userElements[0].nativeElement.textContent.trim()).toContain('John Doe');
      expect(userElements[1].nativeElement.textContent.trim()).toContain('Jane Smith');
      expect(userElements[2].nativeElement.textContent.trim()).toContain('Admin User');
    });

    it('should apply correct status classes to timeline icons', () => {
      const icons = debugElement.queryAll(By.css('.timeline-icon'));
      expect(icons[0].nativeElement).toHaveClass('status-in-use');
      expect(icons[1].nativeElement).toHaveClass('status-deployed');
      expect(icons[2].nativeElement).toHaveClass('status-received');
    });

    it('should show timeline lines between items', () => {
      const timelineLines = debugElement.queryAll(By.css('.timeline-line'));
      expect(timelineLines.length).toBe(2); // One less than items (no line after last item)
    });

    it('should mark first and last items correctly', () => {
      const timelineItems = debugElement.queryAll(By.css('.timeline-item'));
      expect(timelineItems[0].nativeElement).toHaveClass('first-item');
      expect(timelineItems[2].nativeElement).toHaveClass('last-item');
    });
  });

  describe('Empty State', () => {
    beforeEach(() => {
      component.events = [];
      fixture.detectChanges();
    });

    it('should display empty state when no events', () => {
      const emptyState = debugElement.query(By.css('.empty-timeline'));
      expect(emptyState).toBeTruthy();
    });

    it('should display empty state title', () => {
      const title = debugElement.query(By.css('.empty-title'));
      expect(title.nativeElement.textContent.trim()).toBe('No History Available');
    });

    it('should display empty state description', () => {
      const description = debugElement.query(By.css('.empty-description'));
      expect(description.nativeElement.textContent.trim()).toBe('Lifecycle events will appear here as the asset progresses through its lifecycle.');
    });

    it('should display empty state icon', () => {
      const icon = debugElement.query(By.css('.empty-icon mat-icon'));
      expect(icon.nativeElement.textContent.trim()).toBe('timeline');
    });
  });

  describe('Helper Methods', () => {
    it('should return correct status classes', () => {
      expect(component.getStatusClass(LifecycleStatus.ORDERED)).toBe('status-ordered');
      expect(component.getStatusClass(LifecycleStatus.RECEIVED)).toBe('status-received');
      expect(component.getStatusClass(LifecycleStatus.DEPLOYED)).toBe('status-deployed');
      expect(component.getStatusClass(LifecycleStatus.IN_USE)).toBe('status-in-use');
      expect(component.getStatusClass(LifecycleStatus.MAINTENANCE)).toBe('status-maintenance');
      expect(component.getStatusClass(LifecycleStatus.STORAGE)).toBe('status-storage');
      expect(component.getStatusClass(LifecycleStatus.RETIRED)).toBe('status-retired');
    });

    it('should return correct status tooltips', () => {
      expect(component.getStatusTooltip(LifecycleStatus.ORDERED)).toBe('Asset has been ordered');
      expect(component.getStatusTooltip(LifecycleStatus.RECEIVED)).toBe('Asset has been received');
      expect(component.getStatusTooltip(LifecycleStatus.DEPLOYED)).toBe('Asset has been deployed');
      expect(component.getStatusTooltip(LifecycleStatus.IN_USE)).toBe('Asset is currently in use');
      expect(component.getStatusTooltip(LifecycleStatus.MAINTENANCE)).toBe('Asset is under maintenance');
      expect(component.getStatusTooltip(LifecycleStatus.STORAGE)).toBe('Asset is in storage');
      expect(component.getStatusTooltip(LifecycleStatus.RETIRED)).toBe('Asset has been retired');
    });

    it('should handle unknown status gracefully', () => {
      const unknownStatus = 'UNKNOWN' as LifecycleStatus;
      expect(component.getStatusClass(unknownStatus)).toBe('status-ordered');
      expect(component.getStatusTooltip(unknownStatus)).toBe('Unknown status');
    });
  });

  describe('Date Formatting', () => {
    it('should format today correctly', () => {
      const today = new Date().toISOString();
      const formatted = component.formatEventDate(today);
      expect(formatted).toContain('Today');
    });

    it('should format yesterday correctly', () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      const formatted = component.formatEventDate(yesterday.toISOString());
      expect(formatted).toContain('Yesterday');
    });

    it('should format recent dates with weekday', () => {
      const threeDaysAgo = new Date();
      threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
      const formatted = component.formatEventDate(threeDaysAgo.toISOString());
      expect(formatted).toMatch(/^(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)/);
    });

    it('should format older dates with full date', () => {
      const twoWeeksAgo = new Date();
      twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14);
      const formatted = component.formatEventDate(twoWeeksAgo.toISOString());
      expect(formatted).toMatch(/\d{4}/); // Should contain year
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      component.events = mockEvents;
      fixture.detectChanges();
    });

    it('should have proper ARIA labels on timeline', () => {
      const timeline = debugElement.query(By.css('.timeline'));
      expect(timeline.nativeElement.getAttribute('role')).toBe('list');
      expect(timeline.nativeElement.getAttribute('aria-label')).toBe('Asset lifecycle history');
    });

    it('should have proper ARIA labels on timeline items', () => {
      const timelineItems = debugElement.queryAll(By.css('.timeline-item'));
      timelineItems.forEach(item => {
        expect(item.nativeElement.getAttribute('role')).toBe('listitem');
      });
    });

    it('should have proper ARIA labels on icons', () => {
      const icons = debugElement.queryAll(By.css('.timeline-icon mat-icon'));
      expect(icons[0].nativeElement.getAttribute('aria-label')).toBe('Asset is currently in use');
      expect(icons[1].nativeElement.getAttribute('aria-label')).toBe('Asset has been deployed');
      expect(icons[2].nativeElement.getAttribute('aria-label')).toBe('Asset has been received');
    });

    it('should have proper datetime attributes', () => {
      const timeElements = debugElement.queryAll(By.css('time'));
      expect(timeElements[0].nativeElement.getAttribute('datetime')).toBe('2024-01-15T10:30:00Z');
      expect(timeElements[1].nativeElement.getAttribute('datetime')).toBe('2024-01-10T09:15:00Z');
      expect(timeElements[2].nativeElement.getAttribute('datetime')).toBe('2024-01-05T14:20:00Z');
    });
  });

  describe('Component Structure', () => {
    it('should render timeline container', () => {
      component.events = mockEvents;
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.timeline-container'));
      expect(container).toBeTruthy();
    });

    it('should have proper styling classes', () => {
      component.events = mockEvents;
      fixture.detectChanges();
      
      const timeline = debugElement.query(By.css('.timeline'));
      expect(timeline).toBeTruthy();
    });
  });
});