import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslationService } from '../../services/translation.service';

@Component({
  selector: 'app-items-table',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './items-table.component.html',
  styleUrls: ['./items-table.component.css'],
})
export class ItemsTableComponent {
  @Input({ required: true }) items!: FormArray<FormGroup>;
  @Input() currency = 'EUR';

  readonly translate = inject(TranslationService);

  constructor(private fb: FormBuilder) {}

  get rows(): FormGroup[] {
    return this.items.controls as FormGroup[];
  }

  addItem(): void {
    this.items.push(
      this.fb.group({
        description: ['', Validators.required],
        quantity: [1, [Validators.required, Validators.min(0)]],
        unitPrice: [0, [Validators.required, Validators.min(0)]],
        vatRate: [20, [Validators.required, Validators.min(0)]],
      }),
    );
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
    }
  }

  lineTotal(row: FormGroup): number {
    const q = Number(row.get('quantity')?.value) || 0;
    const p = Number(row.get('unitPrice')?.value) || 0;
    const v = Number(row.get('vatRate')?.value) || 0;
    return q * p * (1 + v / 100);
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat(this.translate.locale(), {
      style: 'currency',
      currency: this.currency || 'EUR',
    }).format(value || 0);
  }
}
