import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { TranslationService } from './translation.service';

export interface CompanyInfo {
  name: string;
  address: string;
  city: string;
  country: string;
  email: string;
  phone: string;
  siret?: string;
}

export interface InvoiceItem {
  description: string;
  quantity: number;
  unitPrice: number;
  vatRate: number;
}

export interface InvoiceRequest {
  invoiceNumber: string;
  issueDate: string;
  dueDate: string;
  currency: string;
  sender: CompanyInfo;
  recipient: CompanyInfo;
  items: InvoiceItem[];
  notes: string;
  paymentInfo: string;
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private readonly endpoint = 'http://localhost:8080/api/invoices/generate';
  private readonly http = inject(HttpClient);
  private readonly translate = inject(TranslationService);

  generateInvoice(data: InvoiceRequest): Observable<Blob> {
    return this.http
      .post(this.endpoint, data, { responseType: 'blob' })
      .pipe(
        catchError((err: HttpErrorResponse) => {
          const message =
            err.status === 0
              ? this.translate.t('error.network')
              : `${this.translate.t('error.server')} (${err.status}): ${err.statusText || 'unknown'}`;
          return throwError(() => new Error(message));
        }),
      );
  }
}
