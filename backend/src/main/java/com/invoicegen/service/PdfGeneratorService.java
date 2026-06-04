package com.invoicegen.service;

import com.invoicegen.model.CompanyInfo;
import com.invoicegen.model.Invoice;
import com.invoicegen.model.InvoiceItem;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;

/** Génère le PDF A4 de la facture */
@Service
public class PdfGeneratorService {

    private static final DeviceRgb TEXT_PRIMARY = new DeviceRgb(0x1D, 0x1D, 0x1F);
    private static final DeviceRgb TEXT_SECONDARY = new DeviceRgb(0x6E, 0x6E, 0x73);
    private static final DeviceRgb TEXT_TERTIARY = new DeviceRgb(0xAE, 0xAE, 0xB2);
    private static final DeviceRgb BORDER = new DeviceRgb(0xD2, 0xD2, 0xD7);
    private static final DeviceRgb ACCENT = new DeviceRgb(0x00, 0x66, 0xCC);
    private static final DeviceRgb BG_SECONDARY = new DeviceRgb(0xF5, 0xF5, 0xF7);
    private static final DeviceRgb BG_TERTIARY = new DeviceRgb(0xFA, 0xFA, 0xFA);
    private static final DeviceRgb WHITE = new DeviceRgb(0xFF, 0xFF, 0xFF);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final class Labels {
        static final String invoice = "FACTURE";
        static final String billTo = "FACTURER \u00C0";
        static final String details = "D\u00C9TAILS DE LA FACTURE";
        static final String issueDate = "Date d'\u00E9mission\u00A0: ";
        static final String dueDate = "Date d'\u00E9ch\u00E9ance\u00A0: ";
        static final String currency = "Devise\u00A0: ";
        static final String description = "DESCRIPTION";
        static final String qty = "QT\u00C9";
        static final String unitPrice = "PRIX UNITAIRE";
        static final String vat = "TVA";
        static final String total = "TOTAL";
        static final String subtotal = "Sous-total (HT)";
        static final String vatLine = "TVA";
        static final String totalBox = "Total TTC";
        static final String thanks = "Merci de votre confiance.";
        static final Locale locale = Locale.FRANCE;
    }

    public byte[] generate(Invoice invoice) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(out);
             PdfDocument pdf = new PdfDocument(writer);
             Document doc = new Document(pdf, PageSize.A4)) {

            doc.setMargins(48, 48, 48, 48);

            PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont italic = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            doc.setFont(regular).setFontColor(TEXT_PRIMARY);

            buildHeader(doc, invoice, regular, bold);
            buildMeta(doc, invoice, regular, bold);
            buildItemsTable(doc, invoice, regular, bold);
            buildTotals(doc, invoice, regular, bold);
            buildFooter(doc, invoice, regular, italic);
        }
        return out.toByteArray();
    }

    private void buildHeader(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        CompanyInfo s = invoice.getSender() != null ? invoice.getSender() : new CompanyInfo();

        Paragraph left = new Paragraph()
                .add(new Text(safe(s.getName())).setFont(bold).setFontSize(22)
                        .setFontColor(TEXT_PRIMARY).setCharacterSpacing(-0.4f))
                .add("\n")
                .add(new Text(joinCompanyLines(s)).setFont(regular).setFontSize(10)
                        .setFontColor(TEXT_SECONDARY));
        Cell leftCell = new Cell().add(left).setBorder(Border.NO_BORDER).setPaddingLeft(0);

        Paragraph right = new Paragraph()
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Text(Labels.invoice).setFont(regular).setFontSize(34)
                        .setFontColor(TEXT_PRIMARY).setCharacterSpacing(-0.7f))
                .add("\n")
                .add(new Text(safe(invoice.getInvoiceNumber())).setFont(regular)
                        .setFontSize(13).setFontColor(TEXT_SECONDARY));
        Cell rightCell = new Cell().add(right).setBorder(Border.NO_BORDER).setPaddingRight(0);

        header.addCell(leftCell);
        header.addCell(rightCell);
        doc.add(header);

        SolidLine line = new SolidLine(0.5f);
        line.setColor(BORDER);
        doc.add(new Paragraph().setMarginTop(24).setMarginBottom(0)
                .add(new LineSeparator(line)));
    }

    private String joinCompanyLines(CompanyInfo c) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, c.getAddress());
        String cityCountry = joinNonEmpty(", ", c.getCity(), c.getCountry());
        appendLine(sb, cityCountry);
        appendLine(sb, c.getEmail());
        appendLine(sb, c.getPhone());
        if (c.getSiret() != null && !c.getSiret().isBlank()) {
            appendLine(sb, "SIRET: " + c.getSiret());
        }
        return sb.toString();
    }

    private void appendLine(StringBuilder sb, String value) {
        if (value == null || value.isBlank()) return;
        if (sb.length() > 0) sb.append("\n");
        sb.append(value);
    }

    private String joinNonEmpty(String sep, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p == null || p.isBlank()) continue;
            if (sb.length() > 0) sb.append(sep);
            sb.append(p);
        }
        return sb.toString();
    }

    private void buildMeta(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        Table meta = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(24);

        CompanyInfo r = invoice.getRecipient() != null ? invoice.getRecipient() : new CompanyInfo();

        Paragraph billTo = new Paragraph()
                .add(label(Labels.billTo, regular))
                .add("\n")
                .add(new Text(safe(r.getName())).setFont(bold).setFontSize(13)
                        .setFontColor(TEXT_PRIMARY));
        String recipientRest = joinCompanyLines(r);
        if (!recipientRest.isBlank()) {
            billTo.add("\n").add(new Text(recipientRest).setFont(regular)
                    .setFontSize(11).setFontColor(TEXT_SECONDARY));
        }
        meta.addCell(new Cell().add(billTo).setBorder(Border.NO_BORDER).setPaddingLeft(0));

        Paragraph details = new Paragraph()
                .add(label(Labels.details, regular))
                .add("\n")
                .add(new Text(Labels.issueDate).setFont(regular).setFontSize(11)
                        .setFontColor(TEXT_SECONDARY))
                .add(new Text(fmtDate(invoice.getIssueDate())).setFont(regular)
                        .setFontSize(13).setFontColor(TEXT_PRIMARY))
                .add("\n")
                .add(new Text(Labels.dueDate).setFont(regular).setFontSize(11)
                        .setFontColor(TEXT_SECONDARY))
                .add(new Text(fmtDate(invoice.getDueDate())).setFont(bold)
                        .setFontSize(13).setFontColor(ACCENT))
                .add("\n")
                .add(new Text(Labels.currency).setFont(regular).setFontSize(11)
                        .setFontColor(TEXT_SECONDARY))
                .add(new Text(safe(invoice.getCurrency())).setFont(regular)
                        .setFontSize(13).setFontColor(TEXT_PRIMARY));

        meta.addCell(new Cell().add(details).setBorder(Border.NO_BORDER).setPaddingRight(0));

        doc.add(meta);
    }

    private Text label(String text, PdfFont regular) {
        return new Text(text).setFont(regular).setFontSize(9)
                .setFontColor(TEXT_TERTIARY).setCharacterSpacing(1.2f);
    }

    private void buildItemsTable(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 1, 2, 1, 2}))
                .useAllAvailableWidth()
                .setMarginTop(32);

        addHeaderCell(table, Labels.description, TextAlignment.LEFT, bold);
        addHeaderCell(table, Labels.qty, TextAlignment.RIGHT, bold);
        addHeaderCell(table, Labels.unitPrice, TextAlignment.RIGHT, bold);
        addHeaderCell(table, Labels.vat, TextAlignment.RIGHT, bold);
        addHeaderCell(table, Labels.total, TextAlignment.RIGHT, bold);

        int idx = 0;
        int last = invoice.getItems().size() - 1;
        NumberFormat money = currencyFormat(invoice.getCurrency(), Labels.locale);
        for (InvoiceItem it : invoice.getItems()) {
            DeviceRgb bg = (idx % 2 == 0) ? WHITE : BG_TERTIARY;
            boolean isLast = idx == last;

            table.addCell(bodyCell(safe(it.getDescription()), TextAlignment.LEFT, regular, bg, isLast));
            table.addCell(bodyCell(stripZeros(it.getQuantity()), TextAlignment.RIGHT, regular, bg, isLast));
            table.addCell(bodyCell(money.format(nz(it.getUnitPrice())), TextAlignment.RIGHT, regular, bg, isLast));
            table.addCell(bodyCell(stripZeros(it.getVatRate()) + "%", TextAlignment.RIGHT, regular, bg, isLast));
            table.addCell(bodyCell(money.format(it.getLineTotal()), TextAlignment.RIGHT, regular, bg, isLast));
            idx++;
        }

        doc.add(table);
    }

    private void addHeaderCell(Table table, String text, TextAlignment align, PdfFont bold) {
        Cell c = new Cell().add(new Paragraph(text)
                        .setFont(bold).setFontSize(10).setFontColor(TEXT_SECONDARY)
                        .setCharacterSpacing(1.0f).setTextAlignment(align))
                .setBackgroundColor(BG_SECONDARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(10);
        table.addHeaderCell(c);
    }

    private Cell bodyCell(String text, TextAlignment align, PdfFont regular, DeviceRgb bg, boolean isLast) {
        Cell c = new Cell().add(new Paragraph(text)
                        .setFont(regular).setFontSize(11).setFontColor(TEXT_PRIMARY)
                        .setTextAlignment(align))
                .setBackgroundColor(bg)
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new SolidBorder(BG_SECONDARY, 0.5f))
                .setPadding(10);
        if (isLast) {
            c.setBorderBottom(new SolidBorder(BORDER, 1f));
        }
        return c;
    }

    private void buildTotals(Document doc, Invoice invoice, PdfFont regular, PdfFont bold) {
        NumberFormat money = currencyFormat(invoice.getCurrency(), Labels.locale);

        Table wrapper = new Table(UnitValue.createPercentArray(new float[]{2, 1}))
                .useAllAvailableWidth()
                .setMarginTop(24);
        wrapper.addCell(new Cell().setBorder(Border.NO_BORDER));

        Table totals = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth();

        totals.addCell(totalLabel(Labels.subtotal, regular));
        totals.addCell(totalValue(money.format(invoice.getSubtotal()), regular));

        totals.addCell(totalLabel(Labels.vatLine, regular));
        totals.addCell(totalValue(money.format(invoice.getVatTotal()), regular));

        Cell rule = new Cell(1, 2).setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(BORDER, 0.5f))
                .setPaddingTop(4).setPaddingBottom(4);
        totals.addCell(rule);

        Cell totalBoxLabel = new Cell()
                .add(new Paragraph(Labels.totalBox).setFont(bold).setFontSize(15)
                        .setFontColor(TEXT_PRIMARY))
                .setBackgroundColor(BG_SECONDARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(16);
        Cell totalBoxValue = new Cell()
                .add(new Paragraph(money.format(invoice.getTotal()))
                        .setFont(bold).setFontSize(15).setFontColor(TEXT_PRIMARY)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBackgroundColor(BG_SECONDARY)
                .setBorder(Border.NO_BORDER)
                .setPadding(16);
        totals.addCell(totalBoxLabel);
        totals.addCell(totalBoxValue);

        Cell wrap = new Cell().add(totals).setBorder(Border.NO_BORDER).setPadding(0);
        wrapper.addCell(wrap);
        doc.add(wrapper);
    }

    private Cell totalLabel(String text, PdfFont regular) {
        return new Cell().add(new Paragraph(text).setFont(regular).setFontSize(11)
                        .setFontColor(TEXT_SECONDARY))
                .setBorder(Border.NO_BORDER).setPadding(6);
    }

    private Cell totalValue(String text, PdfFont regular) {
        return new Cell().add(new Paragraph(text).setFont(regular).setFontSize(13)
                        .setFontColor(TEXT_PRIMARY).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(6);
    }

    private void buildFooter(Document doc, Invoice invoice, PdfFont regular, PdfFont italic) {
        if (invoice.getNotes() != null && !invoice.getNotes().isBlank()) {
            doc.add(new Paragraph(invoice.getNotes())
                    .setFont(italic).setFontSize(11).setFontColor(TEXT_SECONDARY)
                    .setMarginTop(32));
        }
        if (invoice.getPaymentInfo() != null && !invoice.getPaymentInfo().isBlank()) {
            doc.add(new Paragraph(invoice.getPaymentInfo())
                    .setFont(regular).setFontSize(11).setFontColor(TEXT_PRIMARY)
                    .setMarginTop(8));
        }
        doc.add(new Paragraph(Labels.thanks)
                .setFont(regular).setFontSize(9).setFontColor(TEXT_TERTIARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(48));
    }

    private NumberFormat currencyFormat(String code, Locale locale) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(locale);
        try {
            nf.setCurrency(Currency.getInstance(code == null ? "EUR" : code));
        } catch (Exception ignored) {
            nf.setCurrency(Currency.getInstance("EUR"));
        }
        return nf;
    }

    private String safe(String v) { return v == null ? "" : v; }

    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String stripZeros(BigDecimal v) {
        if (v == null) return "0";
        BigDecimal s = v.stripTrailingZeros();
        if (s.scale() < 0) s = s.setScale(0, RoundingMode.UNNECESSARY);
        return s.toPlainString();
    }

    private String fmtDate(LocalDate d) {
        return d == null ? "?" : d.format(DATE_FMT);
    }
}
