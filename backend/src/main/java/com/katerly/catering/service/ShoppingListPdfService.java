package com.katerly.catering.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.katerly.catering.entity.*;
import com.katerly.catering.exception.ResourceNotFoundException;
import com.katerly.catering.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShoppingListPdfService {

    private final ShoppingListRepository shoppingListRepository;
    private final BusinessProfileRepository businessProfileRepository;

    private static final DeviceRgb PRIMARY    = new DeviceRgb(34, 139, 87);
    private static final DeviceRgb LIGHT_GREEN = new DeviceRgb(236, 253, 245);
    private static final DeviceRgb LIGHT_GRAY  = new DeviceRgb(243, 244, 246);
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));

    public byte[] generatePdf(Long userId, Long shoppingListId) throws IOException {
        ShoppingList sl = shoppingListRepository.findByShoppingListIdAndUserUserId(shoppingListId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Daftar belanja tidak ditemukan"));

        BusinessProfile profile = businessProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil bisnis belum dibuat"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        PdfFont bold   = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ─── HEADER ──────────────────────────────────────────────────────────────
        doc.add(new Paragraph(profile.getNamaUsaha())
                .setFont(bold).setFontSize(20).setFontColor(PRIMARY));
        doc.add(new Paragraph("Daftar Belanja")
                .setFont(bold).setFontSize(16).setFontColor(PRIMARY));
        doc.add(new Paragraph("Tanggal: " + LocalDate.now().format(DATE_FORMAT))
                .setFont(normal).setFontSize(10));

        // Nama resep
        String resepList = sl.getShoppingListRecipes().stream()
                .map(r -> r.getRecipe().getNamaResep())
                .reduce((a, b) -> a + ", " + b).orElse("-");
        doc.add(new Paragraph("Resep: " + resepList).setFont(normal).setFontSize(10));
        doc.add(new Paragraph("\n"));

        // ─── ITEMS TABLE ─────────────────────────────────────────────────────────
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 35, 15, 15, 15, 15}))
                .useAllAvailableWidth();

        String[] headers = {"No", "Bahan", "Jumlah", "Satuan", "Harga/Satuan", "Total"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .setBackgroundColor(PRIMARY)
                    .add(new Paragraph(h).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int no = 1;
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (ShoppingListItem item : sl.getShoppingListItems()) {
            boolean even = no % 2 == 0;
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(String.valueOf(no++)).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(item.getIngredient().getNama()).setFont(normal).setFontSize(10)));
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(item.getTotalQuantity().toPlainString()).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(item.getSatuan()).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(IDR.format(item.getIngredient().getHargaPerSatuan())).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));
            table.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(IDR.format(item.getTotalHarga())).setFont(bold).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));

            grandTotal = grandTotal.add(item.getTotalHarga());
        }

        // Grand Total
        Cell totalLabel = new Cell(1, 5).setBorderTop(new SolidBorder(PRIMARY, 1.5f))
                .add(new Paragraph("TOTAL ESTIMASI").setFont(bold).setFontSize(11).setFontColor(PRIMARY))
                .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10);
        Cell totalValue = new Cell().setBorderTop(new SolidBorder(PRIMARY, 1.5f))
                .setBackgroundColor(LIGHT_GREEN)
                .add(new Paragraph(IDR.format(grandTotal)).setFont(bold).setFontSize(11).setFontColor(PRIMARY))
                .setTextAlignment(TextAlignment.RIGHT);
        table.addCell(totalLabel);
        table.addCell(totalValue);

        doc.add(table);
        doc.close();
        return baos.toByteArray();
    }
}