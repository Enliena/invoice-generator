package com.invoicegen.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Données de la facture échangées avec le frontend. */
public class Invoice {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String currency = "EUR";
    private CompanyInfo sender;
    private CompanyInfo recipient;
    private List<InvoiceItem> items = new ArrayList<>();
    private String notes;
    private String paymentInfo;

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public CompanyInfo getSender() { return sender; }
    public void setSender(CompanyInfo sender) { this.sender = sender; }

    public CompanyInfo getRecipient() { return recipient; }
    public void setRecipient(CompanyInfo recipient) { this.recipient = recipient; }

    public List<InvoiceItem> getItems() { return items; }
    public void setItems(List<InvoiceItem> items) { this.items = items; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(String paymentInfo) { this.paymentInfo = paymentInfo; }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(InvoiceItem::getLineSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getVatTotal() {
        return items.stream()
                .map(InvoiceItem::getLineVat)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getVatTotal());
    }
}
