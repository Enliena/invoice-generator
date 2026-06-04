package com.invoicegen.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Ligne de facture (quantité, prix unitaire, TVA). */
public class InvoiceItem {
    private String description;
    private BigDecimal quantity = BigDecimal.ONE;
    private BigDecimal unitPrice = BigDecimal.ZERO;
    private BigDecimal vatRate = BigDecimal.ZERO;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getVatRate() { return vatRate; }
    public void setVatRate(BigDecimal vatRate) { this.vatRate = vatRate; }

    public BigDecimal getLineSubtotal() {
        if (quantity == null || unitPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(unitPrice);
    }

    public BigDecimal getLineVat() {
        if (vatRate == null) return BigDecimal.ZERO;
        return getLineSubtotal().multiply(vatRate)
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getLineTotal() {
        return getLineSubtotal().add(getLineVat());
    }
}
