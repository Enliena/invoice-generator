import { Component } from '@angular/core';
import { InvoiceFormComponent } from './components/invoice-form/invoice-form.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [InvoiceFormComponent],
  template: `<app-invoice-form></app-invoice-form>`,
})
export class AppComponent {}
