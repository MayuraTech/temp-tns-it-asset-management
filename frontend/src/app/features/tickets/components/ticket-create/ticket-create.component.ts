import { Component, OnInit, OnDestroy, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';
import { TicketPriority, AllocationTicketRequest, DeallocationTicketRequest } from '../../../../shared/models/ticket.model';
import { TicketService } from '../../services/ticket.service';

@Component({
  selector: 'app-ticket-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket-create.component.html',
  styleUrls: ['./ticket-create.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class TicketCreateComponent implements OnInit, OnDestroy {
  ticketForm!: FormGroup;
  selectedTab: 'allocation' | 'deallocation' = 'allocation';
  submitting$ = new BehaviorSubject<boolean>(false);
  error$ = new BehaviorSubject<string | null>(null);
  
  TicketPriority = TicketPriority;
  
  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private ticketService: TicketService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initializeForm(): void {
    this.ticketForm = this.fb.group({
      assetId: ['', Validators.required],
      priority: [TicketPriority.STANDARD, Validators.required],
      requestReason: [''],
      deallocationReason: [''],
      assignToUser: [''],
      assignToUserEmail: ['', Validators.email],
      assignToLocation: ['']
    });

    this.updateValidators();
  }

  onTabChange(tab: 'allocation' | 'deallocation'): void {
    this.selectedTab = tab;
    this.updateValidators();
  }

  updateValidators(): void {
    const requestReasonControl = this.ticketForm.get('requestReason');
    const deallocationReasonControl = this.ticketForm.get('deallocationReason');

    if (this.selectedTab === 'allocation') {
      requestReasonControl?.setValidators([Validators.required]);
      deallocationReasonControl?.clearValidators();
    } else {
      deallocationReasonControl?.setValidators([Validators.required]);
      requestReasonControl?.clearValidators();
    }

    requestReasonControl?.updateValueAndValidity();
    deallocationReasonControl?.updateValueAndValidity();
  }

  setPriority(priority: TicketPriority): void {
    this.ticketForm.patchValue({ priority });
  }

  onSubmit(): void {
    if (this.ticketForm.invalid) {
      this.ticketForm.markAllAsTouched();
      return;
    }

    this.submitting$.next(true);
    this.error$.next(null);

    const formValue = this.ticketForm.value;

    if (this.selectedTab === 'allocation') {
      const request: AllocationTicketRequest = {
        assetId: formValue.assetId,
        priority: formValue.priority,
        requestReason: formValue.requestReason,
        assignToUser: formValue.assignToUser || undefined,
        assignToUserEmail: formValue.assignToUserEmail || undefined,
        assignToLocation: formValue.assignToLocation || undefined
      };

      this.ticketService.createAllocationTicket(request)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => this.submitting$.next(false))
        )
        .subscribe({
          next: (ticket) => {
            this.router.navigate(['/tickets', ticket.id]);
          },
          error: (error) => {
            this.error$.next('Failed to create allocation ticket');
            console.error('Error creating ticket:', error);
          }
        });
    } else {
      const request: DeallocationTicketRequest = {
        assetId: formValue.assetId,
        priority: formValue.priority,
        deallocationReason: formValue.deallocationReason
      };

      this.ticketService.createDeallocationTicket(request)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => this.submitting$.next(false))
        )
        .subscribe({
          next: (ticket) => {
            this.router.navigate(['/tickets', ticket.id]);
          },
          error: (error) => {
            this.error$.next('Failed to create de-allocation ticket');
            console.error('Error creating ticket:', error);
          }
        });
    }
  }

  cancel(): void {
    this.router.navigate(['/tickets']);
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.ticketForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  getFieldError(fieldName: string): string {
    const field = this.ticketForm.get(fieldName);
    if (field?.hasError('required')) {
      return 'This field is required';
    }
    if (field?.hasError('email')) {
      return 'Invalid email format';
    }
    return '';
  }
}
