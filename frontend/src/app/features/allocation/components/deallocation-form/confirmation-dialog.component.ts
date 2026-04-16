import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';

export interface ConfirmationDialogData {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
}

/**
 * Confirmation Dialog Component
 * 
 * Reusable confirmation dialog for destructive actions.
 * Used by deallocation form to confirm asset deallocation.
 */
@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule
  ],
  template: `
    <div class="confirmation-dialog">
      <h2 mat-dialog-title class="dialog-title">{{ data.title }}</h2>
      
      <mat-dialog-content class="dialog-content">
        <p class="dialog-message">{{ data.message }}</p>
      </mat-dialog-content>
      
      <mat-dialog-actions class="dialog-actions">
        <button 
          mat-raised-button 
          class="btn-ghost"
          (click)="onCancel()"
        >
          {{ data.cancelText }}
        </button>
        
        <button 
          mat-raised-button 
          color="warn"
          class="btn-confirm"
          (click)="onConfirm()"
        >
          {{ data.confirmText }}
        </button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .confirmation-dialog {
      font-family: var(--font-body, 'Inter', sans-serif);
    }

    .dialog-title {
      font-family: var(--font-heading, 'Manrope', sans-serif);
      font-size: 20px;
      font-weight: 700;
      color: var(--on-surface, #1a1b20);
      margin: 0 0 var(--space-lg, 16px) 0;
    }

    .dialog-content {
      padding: var(--space-lg, 16px) 0;
    }

    .dialog-message {
      font-size: 14px;
      color: var(--on-surface-variant, #434750);
      line-height: 1.5;
      margin: 0;
    }

    .dialog-actions {
      display: flex;
      justify-content: flex-end;
      gap: var(--space-md, 12px);
      padding: var(--space-lg, 16px) 0 0 0;
      margin: 0;
    }

    .btn-confirm {
      background: var(--secondary, #a9371d);
      color: white;
      border-radius: 8px;
      font-weight: 700;
      font-size: 12px;
      letter-spacing: 0.3px;
      text-transform: uppercase;
      padding: 10px 20px;
    }

    .btn-ghost {
      background: transparent;
      color: var(--primary, #143b7d);
      border: none;
      border-radius: 8px;
      font-weight: 600;
      font-size: 12px;
      letter-spacing: 0.3px;
      text-transform: uppercase;
      padding: 10px 20px;
    }

    .btn-ghost:hover {
      background: rgba(20, 59, 125, 0.05);
    }
  `]
})
export class ConfirmationDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmationDialogData
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
