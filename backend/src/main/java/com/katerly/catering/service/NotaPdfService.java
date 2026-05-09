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
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotaPdfService {

    private final NotaRepository notaRepository;
    private final BusinessProfileRepository businessProfileRepository;

    private static final DeviceRgb PRIMARY   = new DeviceRgb(34, 139, 87);
    private static final DeviceRgb LIGHT_GREEN = new DeviceRgb(236, 253, 245);
    private static final DeviceRgb DARK_GRAY  = new DeviceRgb(55, 65, 81);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(243, 244, 246);
    private static final NumberFormat IDR = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));

    public byte[] generatePdf(Long userId, Long notaId) throws IOException {
        Nota nota = notaRepository.findByNotaIdAndUserUserId(notaId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nota tidak ditemukan"));

        BusinessProfile profile = businessProfileRepository.findByUserUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profil bisnis belum dibuat"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf, PageSize.A4);
        doc.setMargins(40, 50, 40, 50);

        PdfFont bold   = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);

        // ─── HEADER ──────────────────────────────────────────────────────────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();

        Cell bizCell = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        bizCell.add(new Paragraph(profile.getNamaUsaha()).setFont(bold).setFontSize(20).setFontColor(PRIMARY));
        bizCell.add(new Paragraph(profile.getKota() != null ? profile.getKota() : "").setFont(normal).setFontSize(10).setFontColor(DARK_GRAY));
        if (profile.getNoWhatsapp() != null) bizCell.add(new Paragraph("WA: " + profile.getNoWhatsapp()).setFont(normal).setFontSize(9));
        if (profile.getEmail() != null) bizCell.add(new Paragraph(profile.getEmail()).setFont(normal).setFontSize(9));
        header.addCell(bizCell);

        Cell invCell = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        invCell.add(new Paragraph("NOTA").setFont(bold).setFontSize(24).setFontColor(PRIMARY));
        invCell.add(new Paragraph("No: " + nota.getNomorInvoice()).setFont(bold).setFontSize(11));
        invCell.add(new Paragraph("Tanggal: " + java.time.LocalDate.now().format(DATE_FORMAT)).setFont(normal).setFontSize(9));
        header.addCell(invCell);

        doc.add(header);
        doc.add(new Paragraph("\n"));

        // ─── CLIENT INFO ─────────────────────────────────────────────────────────
        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();

        Cell clientLeft = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GREEN).setPadding(10);
        clientLeft.add(new Paragraph("Kepada Yth:").setFont(bold).setFontSize(10).setFontColor(PRIMARY));
        clientLeft.add(new Paragraph(nota.getNamaClient()).setFont(bold).setFontSize(12));
        if (nota.getNoWaClient() != null) clientLeft.add(new Paragraph("WA: " + nota.getNoWaClient()).setFont(normal).setFontSize(10));
        clientTable.addCell(clientLeft);

        Cell clientRight = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setBackgroundColor(LIGHT_GREEN).setPadding(10);
        clientRight.add(new Paragraph("Detail Acara:").setFont(bold).setFontSize(10).setFontColor(PRIMARY));
        if (nota.getNamaAcara() != null) clientRight.add(new Paragraph(nota.getNamaAcara()).setFont(bold).setFontSize(11));
        if (nota.getTanggalAcara() != null) clientRight.add(new Paragraph("Tanggal: " + nota.getTanggalAcara().format(DATE_FORMAT)).setFont(normal).setFontSize(10));
        clientTable.addCell(clientRight);

        doc.add(clientTable);
        doc.add(new Paragraph("\n"));

        // ─── ITEMS TABLE ─────────────────────────────────────────────────────────
        doc.add(new Paragraph("Rincian Pesanan").setFont(bold).setFontSize(12).setFontColor(PRIMARY));

        Table itemTable = new Table(UnitValue.createPercentArray(new float[]{5, 40, 15, 20, 20})).useAllAvailableWidth();
        String[] headers = {"No", "Menu", "Porsi", "Harga/Porsi", "Subtotal"};
        for (String h : headers) {
            itemTable.addHeaderCell(new Cell()
                    .setBackgroundColor(PRIMARY)
                    .add(new Paragraph(h).setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        int no = 1;
        for (NotaItem item : nota.getNotaItems()) {
            boolean even = no % 2 == 0;
            itemTable.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(String.valueOf(no++)).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
            itemTable.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(item.getRecipe().getNamaResep()).setFont(normal).setFontSize(10)));
            itemTable.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(item.getJumlahPorsi() + " porsi").setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
            itemTable.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(IDR.format(item.getHargaJualPerPorsi())).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));
            itemTable.addCell(new Cell().setBackgroundColor(even ? LIGHT_GRAY : ColorConstants.WHITE)
                    .add(new Paragraph(IDR.format(item.getSubtotal())).setFont(bold).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        // Biaya pengantaran
        if (nota.getBiayaPengantaran() != null && nota.getBiayaPengantaran().compareTo(java.math.BigDecimal.ZERO) > 0) {
            itemTable.addCell(new Cell(1, 4).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .add(new Paragraph("Biaya Pengantaran").setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10));
            itemTable.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .add(new Paragraph(IDR.format(nota.getBiayaPengantaran())).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        // Pajak
        if (nota.getPajakPersen() != null && nota.getPajakPersen().compareTo(java.math.BigDecimal.ZERO) > 0) {
            itemTable.addCell(new Cell(1, 4).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .add(new Paragraph("Pajak (" + nota.getPajakPersen() + "%)").setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10));
            itemTable.addCell(new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .add(new Paragraph(IDR.format(nota.getTotalHargaJual().subtract(nota.getTotalHargaJual().divide(java.math.BigDecimal.ONE.add(nota.getPajakPersen().divide(java.math.BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP)), 2, java.math.RoundingMode.HALF_UP)))).setFont(normal).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT));
        }

        // Total
        Cell totalLabel = new Cell(1, 4).setBorderTop(new SolidBorder(PRIMARY, 1.5f))
                .add(new Paragraph("TOTAL").setFont(bold).setFontSize(11).setFontColor(PRIMARY))
                .setTextAlignment(TextAlignment.RIGHT).setPaddingRight(10);
        Cell totalValue = new Cell().setBorderTop(new SolidBorder(PRIMARY, 1.5f))
                .setBackgroundColor(LIGHT_GREEN)
                .add(new Paragraph(IDR.format(nota.getTotalHargaJual())).setFont(bold).setFontSize(11).setFontColor(PRIMARY))
                .setTextAlignment(TextAlignment.RIGHT);
        itemTable.addCell(totalLabel);
        itemTable.addCell(totalValue);

        doc.add(itemTable);
        doc.add(new Paragraph("\n"));

        // ─── SIGNATURE ───────────────────────────────────────────────────────────
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        Cell sigLeft = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        sigLeft.add(new Paragraph("Hormat kami,").setFont(normal).setFontSize(10));
        sigLeft.add(new Paragraph("\n\n\n"));
        sigLeft.add(new Paragraph(profile.getNamaUsaha()).setFont(bold).setFontSize(11));
        sigTable.addCell(sigLeft);

        Cell sigRight = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
        sigRight.add(new Paragraph("Disetujui oleh,").setFont(normal).setFontSize(10));
        sigRight.add(new Paragraph("\n\n\n"));
        sigRight.add(new Paragraph("(................................)").setFont(normal).setFontSize(10));
        sigTable.addCell(sigRight);
        doc.add(sigTable);

        doc.close();
        return baos.toByteArray();
    }
}