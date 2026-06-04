import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormArray,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ItemsTableComponent } from '../items-table/items-table.component';
import {
  PreviewData,
  PreviewPanelComponent,
} from '../preview-panel/preview-panel.component';
import {
  InvoiceRequest,
  InvoiceService,
} from '../../services/invoice.service';
import { TranslationService } from '../../services/translation.service';

interface Section {
  key: string;
  labelKey: string;
  open: boolean;
}

@Component({
  selector: 'app-invoice-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    ItemsTableComponent,
    PreviewPanelComponent,
  ],
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.css'],
})
export class InvoiceFormComponent implements OnInit {
  form!: FormGroup;
  loading = false;
  error: string | null = null;

  readonly currencies = ['EUR', 'USD', 'GBP', 'CHF', 'CAD'];

  readonly translate = inject(TranslationService);

  sections: Section[] = [
    { key: 'sender', labelKey: 'section.sender', open: true },
    { key: 'recipient', labelKey: 'section.recipient', open: true },
    { key: 'meta', labelKey: 'section.meta', open: true },
    { key: 'items', labelKey: 'section.items', open: true },
    { key: 'footer', labelKey: 'section.footer', open: true },
  ];

  constructor(private fb: FormBuilder, private invoiceService: InvoiceService) {}

  ngOnInit(): void {
    const today = new Date();
    const due = new Date();
    due.setDate(today.getDate() + 30);

    this.form = this.fb.group({
      invoiceNumber: [
        'INV-' + today.getFullYear() + '-001',
        [Validators.required, Validators.pattern(/^[A-Za-z0-9\-_/]+$/)],
      ],
      issueDate: [this.toIso(today), Validators.required],
      dueDate: [this.toIso(due), Validators.required],
      currency: ['EUR', Validators.required],
      sender: this.buildCompany(true),
      recipient: this.buildCompany(false),
      items: this.fb.array<FormGroup>([this.buildItem()]),
      notes: [''],
      paymentInfo: [''],
    });
  }

  private buildCompany(isSender: boolean): FormGroup {
    return this.fb.group({
      name: ['', Validators.required],
      address: [''],
      city: [''],
      country: [''],
      email: ['', [Validators.email]],
      phone: [''],
      siret: isSender ? [''] : [{ value: '', disabled: true }],
    });
  }

  private buildItem(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      quantity: [1, [Validators.required, Validators.min(0)]],
      unitPrice: [0, [Validators.required, Validators.min(0)]],
      vatRate: [20, [Validators.required, Validators.min(0)]],
    });
  }

  get items(): FormArray<FormGroup> {
    return this.form.get('items') as FormArray<FormGroup>;
  }

  toggleSection(key: string): void {
    const s = this.sections.find((x) => x.key === key);
    if (s) s.open = !s.open;
  }

  isInvalid(path: string): boolean {
    const ctrl = this.form.get(path);
    return !!ctrl && ctrl.invalid && (ctrl.dirty || ctrl.touched);
  }

  errorOf(path: string): string | null {
    const ctrl = this.form.get(path);
    if (!ctrl || !ctrl.errors || !(ctrl.dirty || ctrl.touched)) return null;
    if (ctrl.errors['required']) return this.translate.t('error.required');
    if (ctrl.errors['email']) return this.translate.t('error.email');
    if (ctrl.errors['pattern']) return this.translate.t('error.pattern');
    if (ctrl.errors['min']) return this.translate.t('error.min');
    return this.translate.t('error.invalid');
  }

  get preview(): PreviewData {
    const v = this.form?.getRawValue();
    const items = (v?.items ?? []) as Array<{
      quantity: number;
      unitPrice: number;
      vatRate: number;
    }>;
    let subtotal = 0;
    let vat = 0;
    for (const it of items) {
      const q = Number(it.quantity) || 0;
      const p = Number(it.unitPrice) || 0;
      const r = Number(it.vatRate) || 0;
      const s = q * p;
      subtotal += s;
      vat += s * (r / 100);
    }
    return {
      invoiceNumber: v?.invoiceNumber ?? '',
      senderName: v?.sender?.name ?? '',
      recipientName: v?.recipient?.name ?? '',
      recipientCity: v?.recipient?.city ?? '',
      itemsCount: items.length,
      subtotal,
      vat,
      total: subtotal + vat,
      currency: v?.currency ?? 'EUR',
      issueDate: v?.issueDate ?? '',
      dueDate: v?.dueDate ?? '',
    };
  }

  download(): void {
    this.error = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error = this.translate.t('error.fix');
      return;
    }

    const payload = this.form.getRawValue() as InvoiceRequest;
    this.loading = true;
    this.invoiceService.generateInvoice(payload).subscribe({
      next: (blob) => {
        this.loading = false;
        this.triggerDownload(blob, `invoice-${payload.invoiceNumber}.pdf`);
      },
      error: (err: Error) => {
        this.loading = false;
        this.error = err.message;
      },
    });
  }

  private triggerDownload(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  private toIso(d: Date): string {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
