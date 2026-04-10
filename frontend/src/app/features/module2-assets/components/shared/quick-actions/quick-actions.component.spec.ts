import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { QuickActionsComponent } from './quick-actions.component';
import { LifecycleStatus } from '../../../models';

describe('QuickActionsComponent', () => {
  let component: QuickActionsComponent;
  let fixture: ComponentFixture<QuickActionsComponent>;
  let debugElement: DebugElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        QuickActionsComponent,
        NoopAnimationsModule,
        MatButtonModule,
        MatIconModule,
        MatMenuModule,
        MatTooltipModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(QuickActionsComponent);
    component = fixture.componentInstance;
    debugElement = fixture.debugElement;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Component Initialization', () => {
    it('should initialize with default values', () => {
      expect(component.isAssigned).toBeFalse();
      expect(component.readOnly).toBeFalse();
      expect(component.showSecondaryActions).toBeTrue();
    });

    it('should render actions title', () => {
      fixture.detectChanges();
      
      const title = debugElement.query(By.css('.actions-title'));
      expect(title.nativeElement.textContent.trim()).toBe('Quick Actions');
    });

    it('should render actions grid', () => {
      fixture.detectChanges();
      
      const grid = debugElement.query(By.css('.actions-grid'));
      expect(grid).toBeTruthy();
    });
  });

  describe('Action Buttons', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should render edit asset button', () => {
      const editButton = debugElement.query(By.css('.primary-action'));
      expect(editButton).toBeTruthy();
      expect(editButton.nativeElement.textContent.trim()).toContain('Edit Asset');
    });

    it('should render change status button', () => {
      const statusButton = debugElement.query(By.css('button[matMenuTriggerFor]'));
      expect(statusButton).toBeTruthy();
      expect(statusButton.nativeElement.textContent.trim()).toContain('Change Status');
    });

    it('should render generate report button', () => {
      const reportButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Generate Report')
      );
      expect(reportButton).toBeTruthy();
    });

    it('should render assign/reassign button', () => {
      const assignButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Assign')
      );
      expect(assignButton).toBeTruthy();
    });

    it('should render delete button', () => {
      const deleteButton = debugElement.query(By.css('.danger-action'));
      expect(deleteButton).toBeTruthy();
      expect(deleteButton.nativeElement.textContent.trim()).toContain('Delete');
    });

    it('should render export button', () => {
      const exportButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Export')
      );
      expect(exportButton).toBeTruthy();
    });
  });

  describe('Button States', () => {
    it('should disable buttons when readOnly is true', () => {
      component.readOnly = true;
      fixture.detectChanges();
      
      const editButton = debugElement.query(By.css('.primary-action'));
      const deleteButton = debugElement.query(By.css('.danger-action'));
      
      expect(editButton.nativeElement.disabled).toBeTrue();
      expect(deleteButton.nativeElement.disabled).toBeTrue();
    });

    it('should disable change status button when asset is retired', () => {
      component.currentStatus = LifecycleStatus.RETIRED;
      fixture.detectChanges();
      
      const statusButton = debugElement.query(By.css('button[matMenuTriggerFor]'));
      expect(statusButton.nativeElement.disabled).toBeTrue();
    });

    it('should show "Reassign" when asset is assigned', () => {
      component.isAssigned = true;
      fixture.detectChanges();
      
      const assignButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Reassign')
      );
      expect(assignButton).toBeTruthy();
    });

    it('should show "Assign" when asset is not assigned', () => {
      component.isAssigned = false;
      fixture.detectChanges();
      
      const assignButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Assign') && 
        !btn.nativeElement.textContent.trim().includes('Reassign')
      );
      expect(assignButton).toBeTruthy();
    });
  });

  describe('Event Emissions', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should emit editClick when edit button is clicked', () => {
      spyOn(component.editClick, 'emit');
      
      const editButton = debugElement.query(By.css('.primary-action'));
      editButton.nativeElement.click();
      
      expect(component.editClick.emit).toHaveBeenCalled();
    });

    it('should emit generateReportClick when report button is clicked', () => {
      spyOn(component.generateReportClick, 'emit');
      
      const reportButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Generate Report')
      );
      reportButton!.nativeElement.click();
      
      expect(component.generateReportClick.emit).toHaveBeenCalled();
    });

    it('should emit assignmentClick when assign button is clicked', () => {
      spyOn(component.assignmentClick, 'emit');
      
      const assignButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Assign')
      );
      assignButton!.nativeElement.click();
      
      expect(component.assignmentClick.emit).toHaveBeenCalled();
    });

    it('should emit deleteClick when delete button is clicked', () => {
      spyOn(component.deleteClick, 'emit');
      
      const deleteButton = debugElement.query(By.css('.danger-action'));
      deleteButton.nativeElement.click();
      
      expect(component.deleteClick.emit).toHaveBeenCalled();
    });

    it('should emit exportClick when export button is clicked', () => {
      spyOn(component.exportClick, 'emit');
      
      const exportButton = debugElement.queryAll(By.css('.action-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Export')
      );
      exportButton!.nativeElement.click();
      
      expect(component.exportClick.emit).toHaveBeenCalled();
    });
  });

  describe('Secondary Actions', () => {
    beforeEach(() => {
      component.showSecondaryActions = true;
      fixture.detectChanges();
    });

    it('should show secondary actions when enabled', () => {
      const secondaryActions = debugElement.query(By.css('.secondary-actions'));
      expect(secondaryActions).toBeTruthy();
    });

    it('should hide secondary actions when disabled', () => {
      component.showSecondaryActions = false;
      fixture.detectChanges();
      
      const secondaryActions = debugElement.query(By.css('.secondary-actions'));
      expect(secondaryActions).toBeFalsy();
    });

    it('should render duplicate button', () => {
      const duplicateButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Duplicate')
      );
      expect(duplicateButton).toBeTruthy();
    });

    it('should render history button', () => {
      const historyButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('View History')
      );
      expect(historyButton).toBeTruthy();
    });

    it('should render print button', () => {
      const printButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Print')
      );
      expect(printButton).toBeTruthy();
    });

    it('should emit duplicateClick when duplicate button is clicked', () => {
      spyOn(component.duplicateClick, 'emit');
      
      const duplicateButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Duplicate')
      );
      duplicateButton!.nativeElement.click();
      
      expect(component.duplicateClick.emit).toHaveBeenCalled();
    });

    it('should emit historyClick when history button is clicked', () => {
      spyOn(component.historyClick, 'emit');
      
      const historyButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('View History')
      );
      historyButton!.nativeElement.click();
      
      expect(component.historyClick.emit).toHaveBeenCalled();
    });

    it('should emit printClick when print button is clicked', () => {
      spyOn(component.printClick, 'emit');
      
      const printButton = debugElement.queryAll(By.css('.secondary-button')).find(btn => 
        btn.nativeElement.textContent.trim().includes('Print')
      );
      printButton!.nativeElement.click();
      
      expect(component.printClick.emit).toHaveBeenCalled();
    });
  });

  describe('Status Transitions', () => {
    it('should return correct available statuses for ORDERED', () => {
      component.currentStatus = LifecycleStatus.ORDERED;
      const availableStatuses = component.getAvailableStatuses();
      
      expect(availableStatuses).toContain(LifecycleStatus.RECEIVED);
      expect(availableStatuses).toContain(LifecycleStatus.MAINTENANCE);
      expect(availableStatuses).not.toContain(LifecycleStatus.DEPLOYED);
    });

    it('should return correct available statuses for IN_USE', () => {
      component.currentStatus = LifecycleStatus.IN_USE;
      const availableStatuses = component.getAvailableStatuses();
      
      expect(availableStatuses).toContain(LifecycleStatus.STORAGE);
      expect(availableStatuses).toContain(LifecycleStatus.RETIRED);
      expect(availableStatuses).toContain(LifecycleStatus.MAINTENANCE);
      expect(availableStatuses).not.toContain(LifecycleStatus.ORDERED);
    });

    it('should return empty array for RETIRED status', () => {
      component.currentStatus = LifecycleStatus.RETIRED;
      const availableStatuses = component.getAvailableStatuses();
      
      expect(availableStatuses).toEqual([]);
    });

    it('should return all statuses except current for MAINTENANCE', () => {
      component.currentStatus = LifecycleStatus.MAINTENANCE;
      const availableStatuses = component.getAvailableStatuses();
      
      expect(availableStatuses).not.toContain(LifecycleStatus.MAINTENANCE);
      expect(availableStatuses.length).toBe(6); // All except MAINTENANCE
    });

    it('should emit statusChange when status is selected', () => {
      spyOn(component.statusChange, 'emit');
      
      component.onStatusChange(LifecycleStatus.DEPLOYED);
      
      expect(component.statusChange.emit).toHaveBeenCalledWith(LifecycleStatus.DEPLOYED);
    });
  });

  describe('Helper Methods', () => {
    it('should return correct status icons', () => {
      expect(component.getStatusIcon(LifecycleStatus.ORDERED)).toBe('shopping_cart');
      expect(component.getStatusIcon(LifecycleStatus.RECEIVED)).toBe('inventory');
      expect(component.getStatusIcon(LifecycleStatus.DEPLOYED)).toBe('rocket_launch');
      expect(component.getStatusIcon(LifecycleStatus.IN_USE)).toBe('play_circle');
      expect(component.getStatusIcon(LifecycleStatus.MAINTENANCE)).toBe('build');
      expect(component.getStatusIcon(LifecycleStatus.STORAGE)).toBe('archive');
      expect(component.getStatusIcon(LifecycleStatus.RETIRED)).toBe('delete_forever');
    });

    it('should return correct status icon classes', () => {
      expect(component.getStatusIconClass(LifecycleStatus.ORDERED)).toBe('status-icon-ordered');
      expect(component.getStatusIconClass(LifecycleStatus.IN_USE)).toBe('status-icon-in-use');
      expect(component.getStatusIconClass(LifecycleStatus.RETIRED)).toBe('status-icon-retired');
    });

    it('should format status correctly', () => {
      expect(component.formatStatus(LifecycleStatus.IN_USE)).toBe('In Use');
      expect(component.formatStatus(LifecycleStatus.ORDERED)).toBe('Ordered');
      expect(component.formatStatus(LifecycleStatus.MAINTENANCE)).toBe('Maintenance');
    });
  });

  describe('Accessibility', () => {
    beforeEach(() => {
      fixture.detectChanges();
    });

    it('should have proper ARIA labels on buttons', () => {
      const editButton = debugElement.query(By.css('.primary-action'));
      expect(editButton.nativeElement.getAttribute('aria-label')).toBe('Edit asset');
    });

    it('should have proper tooltips on buttons', () => {
      const editButton = debugElement.query(By.css('.primary-action'));
      expect(editButton.nativeElement.getAttribute('matTooltip')).toBe('Edit asset details');
    });
  });

  describe('Component Structure', () => {
    it('should render quick actions container', () => {
      fixture.detectChanges();
      
      const container = debugElement.query(By.css('.quick-actions-container'));
      expect(container).toBeTruthy();
    });

    it('should have proper styling classes', () => {
      fixture.detectChanges();
      
      const primaryAction = debugElement.query(By.css('.primary-action'));
      expect(primaryAction).toBeTruthy();
      
      const dangerAction = debugElement.query(By.css('.danger-action'));
      expect(dangerAction).toBeTruthy();
    });
  });
});