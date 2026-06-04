import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TranslationService } from '../../services/translation.service';

export interface PreviewData {
  invoiceNumber: string;
  senderName: string;
  recipientName: string;
  recipientCity: string;
  itemsCount: number;
  subtotal: number;
  vat: number;
  total: number;
  currency: string;
  issueDate: string;
  dueDate: string;
}

@Component({
  selector: 'app-preview-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './preview-panel.component.html',
  styleUrls: ['./preview-panel.component.css'],
})
export class PreviewPanelComponent {
  @Input({ required: true }) data!: PreviewData;
  @Input() loading = false;
  @Input() error: string | null = null;
  @Output() download = new EventEmitter<void>();

  readonly translate = inject(TranslationService);

  onDownload(): void {
    if (!this.loading) {
      this.download.emit();
    }
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat(this.translate.locale(), {
      style: 'currency',
      currency: this.data?.currency || 'EUR',
    }).format(value || 0);
  }

  formatDate(iso: string): string {
    if (!iso) return '?';
    const d = new Date(iso);
    if (isNaN(d.getTime())) return '?';
    const dd = String(d.getDate()).padStart(2, '0');
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    return `${dd}/${mm}/${d.getFullYear()}`;
  }
}
