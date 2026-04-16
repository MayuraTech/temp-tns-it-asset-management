import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AssignmentCardComponent } from './assignment-card.component';

describe('AssignmentCardComponent', () => {
  let component: AssignmentCardComponent;
  let fixture: ComponentFixture<AssignmentCardComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        AssignmentCardComponent,
        NoopAnimationsModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatTooltipModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AssignmentCardComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Assigned State', () => {
    beforeEach(() => {
      component.assignedUser = 'John Doe';
      component.assignedUserEmail = 'john.doe@example.com';
      component.assignmentDate = '2024-01-15T10:30:00Z';
      component.phone = '+1-555-0123';
      component.department = 'IT Department';
    });

    it('should display assigned user information', () => {
      fixture.detectChanges();
      
      const userName = debugElement.query(By.css('.user-name'));
      expect(userName.nativeElement.textContent.trim()).toBe('John Doe');
    });

    it('should display user email', () => {
      fixture.detectChanges();
      
      const emailElement = debugElement.query(By.css('.contact-item .contact-text'));
      expect(emailElement.nativeElement.textContent.trim()).toBe('john.doe@example.com');
    });

    it('should display phone number when provided', () => {
      fixture.detectChanges();
      
      const contactItems = debugElement.queryAll(By.css('.contact-item .contact-text'));
      const phoneText = contactItems.find(item => 
        item.nativeElement.textContent.trim() === '+1-555-0123'
      );
      expect(phoneText).toBeTruthy();
    });

    it('should display department when provided', () => {
      fixture.detectChanges();
      
      const contactItems = debugElement.queryAll(By.css('.contact-item .contact-text'));
      const deptText = contactItems.find(item => 
        item.nativeElement.textContent.trim() === 'IT Department'
      );
      expect(deptText).toBeTruthy();
    });

    it('should show reassign button', () => {
      fixture.detectChanges();
      
      const reassignButton = debugElement.query(By.css('.reassign-button'));
      expect(reassignButton).toBeTruthy();
      expect(reassignButton.nativeElement.textContent.trim()).toContain('Reassign Asset');
    });

    it('should emit reassignClick when reassign button is clicked', () => {
      spyOn(component.reassignClick, 'emit');
      fixture.detectChanges();
      
      const reassignButton = debugElement.query(By.css('.reassign-button'));
      reassignButton.nativeElement.click();
      
      expect(component.reassignClick.emit).toHaveBeenCalled();
    });

    it('should disable reassign button when readOnly is true', () => {
      component.readOnly = true;
      fixture.detectChanges();
      
      const reassignButton = debugElement.query(By.css('.reassign-button'));
      expect(reassignButton.nativeElement.disabled).toBeTrue();
    });
  });

  describe('Unassigned State', () => {
    beforeEach(() => {
      component.assignedUser = undefined;
    });

    it('should show unassigned template when no user assigned', () => {
      fixture.detectChanges();
      
      const unassignedCard = debugElement.query(By.css('.unassigned'));
      expect(unassignedCard).toBeTruthy();
    });

    it('should display unassigned title', () => {
      fixture.detectChanges();
      
      const title = debugElement.query(By.css('.unassigned-title'));
      expect(title.nativeElement.textContent.trim()).toBe('Unassigned Asset');
    });

    it('should display unassigned description', () => {
      fixture.detectChanges();
      
      const description = debugElement.query(By.css('.unassigned-description'));
      expect(description.nativeElement.textContent.trim()).toBe('This asset is not currently assigned to any user.');
    });

    it('should show assign button', () => {
      fixture.detectChanges();
      
      const assignButton = debugElement.query(By.css('.assign-button'));
      expect(assignButton).toBeTruthy();
      expect(assignButton.nativeElement.textContent.trim()).toContain('Assign Asset');
    });

    it('should emit assignClick when assign button is clicked', () => {
      spyOn(component.assignClick, 'emit');
      fixture.detectChanges();
      
      const assignButton = debugElement.query(By.css('.assign-button'));
      assignButton.nativeElement.click();
      
      expect(component.assignClick.emit).toHaveBeenCalled();
    });

    it('should disable assign button when readOnly is true', () => {
      component.readOnly = true;
      fixture.detectChanges();
      
      const assignButton = debugElement.query(By.css('.assign-button'));
      expect(assignButton.nativeElement.disabled).toBeTrue();
    });
  });

  describe('Avatar Generation', () => {
    it('should generate single initial for single name', () => {
      expect(component.getInitials('John')).toBe('J');
    });

    it('should generate initials for full name', () => {
      expect(component.getInitials('John Doe')).toBe('JD');
    });

    it('should handle multiple names correctly', () => {
      expect(component.getInitials('John Michael Doe')).toBe('JD');
    });

    it('should handle empty name', () => {
      expect(component.getInitials('')).toBe('?');
    });

    it('should handle whitespace-only name', () => {
      expect(component.getInitials('   ')).toBe('?');
    });

    it('should generate consistent avatar color', () => {
      const color1 = component.getAvatarColor('John Doe');
      const color2 = component.getAvatarColor('John Doe');
      expect(color1).toBe(color2);
    });

    it('should generate different colors for different names', () => {
      const color1 = component.getAvatarColor('John Doe');
      const color2 = component.getAvatarColor('Jane Smith');
      expect(color1).not.toBe(color2);
    });

    it('should return default color for empty name', () => {
      expect(component.getAvatarColor('')).toBe('#6c757d');
    });
  });

  describe('Date Formatting', () => {
    it('should format today correctly', () => {
      const today = new Date().toISOString();
      expect(component.formatAssignmentDate(today)).toBe('today');
    });

    it('should format yesterday correctly', () => {
      const yesterday = new Date();
      yesterday.setDate(yesterday.getDate() - 1);
      expect(component.formatAssignmentDate(yesterday.toISOString())).toBe('yesterday');
    });

    it('should format days ago correctly', () => {
      const threeDaysAgo = new Date();
      threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
      expect(component.formatAssignmentDate(threeDaysAgo.toISOString())).toBe('3 days ago');
    });

    it('should format weeks ago correctly', () => {
      const twoWeeksAgo = new Date();
      twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14);
      expect(component.formatAssignmentDate(twoWeeksAgo.toISOString())).toBe('2 weeks ago');
    });

    it('should format months ago correctly', () => {
      const twoMonthsAgo = new Date();
      twoMonthsAgo.setDate(twoMonthsAgo.getDate() - 60);
      const result = component.formatAssignmentDate(twoMonthsAgo.toISOString());
      expect(result).toContain('month');
    });

    it('should format years ago correctly', () => {
      const twoYearsAgo = new Date();
      twoYearsAgo.setFullYear(twoYearsAgo.getFullYear() - 2);
      const result = component.formatAssignmentDate(twoYearsAgo.toISOString());
      expect(result).toContain('year');
    });
  });

  describe('Component Methods', () => {
    it('should call onReassignClick when reassign method is called', () => {
      spyOn(component.reassignClick, 'emit');
      
      component.onReassignClick();
      
      expect(component.reassignClick.emit).toHaveBeenCalled();
    });

    it('should call onAssignClick when assign method is called', () => {
      spyOn(component.assignClick, 'emit');
      
      component.onAssignClick();
      
      expect(component.assignClick.emit).toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels on buttons', () => {
      component.assignedUser = 'John Doe';
      fixture.detectChanges();
      
      const reassignButton = debugElement.query(By.css('.reassign-button'));
      expect(reassignButton.nativeElement.getAttribute('aria-label')).toBe('Reassign asset');
    });

    it('should have proper tooltip on reassign button', () => {
      component.assignedUser = 'John Doe';
      fixture.detectChanges();
      
      const reassignButton = debugElement.query(By.css('.reassign-button'));
      expect(reassignButton.nativeElement.getAttribute('matTooltip')).toBe('Reassign this asset to another user');
    });
  });

  describe('Component Structure', () => {
    it('should render mat-card', () => {
      fixture.detectChanges();
      
      const card = debugElement.query(By.css('mat-card'));
      expect(card).toBeTruthy();
    });

    it('should have assignment-card class', () => {
      fixture.detectChanges();
      
      const card = debugElement.query(By.css('.assignment-card'));
      expect(card).toBeTruthy();
    });
  });
});