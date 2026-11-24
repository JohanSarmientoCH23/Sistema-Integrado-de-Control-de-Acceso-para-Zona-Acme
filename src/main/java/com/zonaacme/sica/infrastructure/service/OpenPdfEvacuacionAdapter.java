package com.zonaacme.sica.infrastructure.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.zonaacme.sica.application.port.out.GeneradorReportePdfPort;
import com.zonaacme.sica.application.usecase.reporte.ResumenAforo;
import com.zonaacme.sica.domain.entity.AforoActual;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Adaptador para generar el Reporte Oficial de Evacuación y Aforo en Sitio en formato PDF
 * con paleta corporativa verde oscuro (#143D2B) y blanco usando LibrePDF OpenPDF.
 */
public class OpenPdfEvacuacionAdapter implements GeneradorReportePdfPort {

    private static final Color VERDE_OSCURO_CORPORATIVO = new Color(20, 61, 43);
    private static final Color VERDE_ACENTO = new Color(29, 92, 63);
    private static final Color VERDE_FILA_ALTERNA = new Color(242, 247, 244);
    private static final Color GRIS_BORDE = new Color(210, 220, 214);
    private static final Color BLANCO = Color.WHITE;

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Path generarReporteEvacuacion(List<AforoActual> personasEnSitio, ResumenAforo resumen, Path archivoDestino) {
        if (archivoDestino == null) {
            throw new IllegalArgumentException("La ruta de destino del archivo PDF no puede ser nula.");
        }

        List<AforoActual> listaSegura = personasEnSitio != null ? personasEnSitio : Collections.emptyList();

        try {
            if (archivoDestino.getParent() != null) {
                Files.createDirectories(archivoDestino.getParent());
            }

            Document document = new Document(PageSize.A4.rotate(), 28f, 28f, 32f, 42f);
            try (FileOutputStream fos = new FileOutputStream(archivoDestino.toFile())) {
                PdfWriter writer = PdfWriter.getInstance(document, fos);
                writer.setPageEvent(new PieDePaginaEvacuacionEvent());

                document.open();

                agregarEncabezadoCorporativo(document);
                agregarResumenEjecutivo(document, listaSegura, resumen);
                agregarTablaDetalladaEvacuacion(document, listaSegura);

                document.close();
            }
            return archivoDestino;
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Error al generar el reporte PDF de evacuación en: " + archivoDestino, e);
        }
    }

    private void agregarEncabezadoCorporativo(Document document) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f, BLANCO);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, BLANCO);

        ZonedDateTime ahoraBogota = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        String fechaGeneracion = ahoraBogota.format(FORMATO_FECHA_HORA) + " (America/Bogota)";

        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100f);
        headerTable.setSpacingAfter(14f);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(VERDE_OSCURO_CORPORATIVO);
        cell.setPadding(14f);
        cell.setBorder(Rectangle.NO_BORDER);

        Paragraph titulo = new Paragraph("SICA — Reporte Oficial de Evacuación y Aforo en Sitio (Zona Acme)", fontTitulo);
        titulo.setSpacingAfter(4f);
        cell.addElement(titulo);

        Paragraph subtitulo = new Paragraph("Fecha y hora exacta de generación: " + fechaGeneracion, fontSubtitulo);
        cell.addElement(subtitulo);

        headerTable.addCell(cell);
        document.add(headerTable);
    }

    private void agregarResumenEjecutivo(Document document, List<AforoActual> personasEnSitio, ResumenAforo resumen) throws DocumentException {
        Font fontLabel = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, VERDE_OSCURO_CORPORATIVO);
        Font fontValue = FontFactory.getFont(FontFactory.HELVETICA, 9f, Color.DARK_GRAY);

        int totalEnSitio = resumen != null ? resumen.totalEnSitio() : personasEnSitio.size();
        String desgloseEmpresa = formatearMapaDesglose(resumen != null ? resumen.conteoPorEmpresa() : Collections.emptyMap());
        String desgloseTipo = formatearMapaDesglose(resumen != null ? resumen.conteoPorTipoPersona() : Collections.emptyMap());

        PdfPTable summaryTable = new PdfPTable(new float[]{1.2f, 2.4f, 2.4f});
        summaryTable.setWidthPercentage(100f);
        summaryTable.setSpacingAfter(14f);

        summaryTable.addCell(crearCeldaResumen("Total Personas en Sitio", String.valueOf(totalEnSitio), fontLabel, fontValue));
        summaryTable.addCell(crearCeldaResumen("Desglose por Empresa", desgloseEmpresa, fontLabel, fontValue));
        summaryTable.addCell(crearCeldaResumen("Desglose por Tipo de Persona", desgloseTipo, fontLabel, fontValue));

        document.add(summaryTable);
    }

    private PdfPCell crearCeldaResumen(String titulo, String contenido, Font fontLabel, Font fontValue) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(VERDE_FILA_ALTERNA);
        cell.setBorderColor(GRIS_BORDE);
        cell.setPadding(9f);

        Paragraph pTitulo = new Paragraph(titulo, fontLabel);
        pTitulo.setSpacingAfter(3f);
        cell.addElement(pTitulo);

        Paragraph pValor = new Paragraph(contenido, fontValue);
        cell.addElement(pValor);
        return cell;
    }

    private void agregarTablaDetalladaEvacuacion(Document document, List<AforoActual> personasEnSitio) throws DocumentException {
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9f, BLANCO);
        Font fontCell = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
        Font fontCheck = FontFactory.getFont(FontFactory.COURIER_BOLD, 9f, VERDE_OSCURO_CORPORATIVO);

        PdfPTable table = new PdfPTable(new float[]{0.5f, 1.3f, 2.2f, 2.0f, 1.2f, 1.5f, 1.4f, 1.2f});
        table.setWidthPercentage(100f);
        table.setHeaderRows(1);

        String[] columnas = {"#", "Documento", "Nombre Completo", "Empresa", "Tipo", "Hora Ingreso", "Método", "Verificado"};
        for (String col : columnas) {
            PdfPCell headerCell = new PdfPCell(new Phrase(col, fontHeader));
            headerCell.setBackgroundColor(VERDE_ACENTO);
            headerCell.setBorderColor(GRIS_BORDE);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerCell.setPadding(7f);
            table.addCell(headerCell);
        }

        if (personasEnSitio.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("No se registran personas dentro de las instalaciones en este momento.", fontCell));
            emptyCell.setColspan(8);
            emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            emptyCell.setPadding(12f);
            emptyCell.setBorderColor(GRIS_BORDE);
            table.addCell(emptyCell);
        } else {
            int indice = 1;
            for (AforoActual persona : personasEnSitio) {
                Color bgFila = (indice % 2 == 0) ? VERDE_FILA_ALTERNA : BLANCO;
                String horaIngreso = persona.fechaEntrada() != null
                        ? persona.fechaEntrada().format(FORMATO_FECHA_HORA)
                        : "-";

                table.addCell(crearCeldaDato(String.valueOf(indice), fontCell, bgFila, Element.ALIGN_CENTER));
                table.addCell(crearCeldaDato(valorSeguro(persona.documentoIdentidad()), fontCell, bgFila, Element.ALIGN_LEFT));
                table.addCell(crearCeldaDato(valorSeguro(persona.nombrePersona()), fontCell, bgFila, Element.ALIGN_LEFT));
                table.addCell(crearCeldaDato(valorSeguro(persona.empresa()), fontCell, bgFila, Element.ALIGN_LEFT));
                table.addCell(crearCeldaDato(valorSeguro(persona.tipoPersona()), fontCell, bgFila, Element.ALIGN_CENTER));
                table.addCell(crearCeldaDato(horaIngreso, fontCell, bgFila, Element.ALIGN_CENTER));
                table.addCell(crearCeldaDato(valorSeguro(persona.metodoIngreso()), fontCell, bgFila, Element.ALIGN_CENTER));
                table.addCell(crearCeldaDato("[ ] Verificado", fontCheck, bgFila, Element.ALIGN_CENTER));
                indice++;
            }
        }

        document.add(table);
    }

    private PdfPCell crearCeldaDato(String texto, Font font, Color background, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(background);
        cell.setBorderColor(GRIS_BORDE);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        return cell;
    }

    private String formatearMapaDesglose(Map<String, Long> mapa) {
        if (mapa == null || mapa.isEmpty()) {
            return "Sin registros activos";
        }
        StringBuilder sb = new StringBuilder();
        mapa.forEach((clave, cantidad) -> {
            if (!sb.isEmpty()) {
                sb.append(" | ");
            }
            sb.append(clave).append(": ").append(cantidad);
        });
        return sb.toString();
    }

    private String valorSeguro(String val) {
        return val != null ? val : "-";
    }

    private static class PieDePaginaEvacuacionEvent extends PdfPageEventHelper {
        private final Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8f, new Color(90, 105, 96));

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable footer = new PdfPTable(2);
            try {
                footer.setWidths(new int[]{4, 1});
                footer.setTotalWidth(document.right() - document.left());
                footer.setLockedWidth(true);

                PdfPCell left = new PdfPCell(new Phrase(
                        "SICA Zona Acme — Documento Oficial de Emergencia y Control de Evacuación en Punto de Encuentro",
                        fontFooter
                ));
                left.setBorder(Rectangle.TOP);
                left.setBorderColor(GRIS_BORDE);
                left.setPaddingTop(5f);

                PdfPCell right = new PdfPCell(new Phrase("Página " + writer.getPageNumber(), fontFooter));
                right.setBorder(Rectangle.TOP);
                right.setBorderColor(GRIS_BORDE);
                right.setHorizontalAlignment(Element.ALIGN_RIGHT);
                right.setPaddingTop(5f);

                footer.addCell(left);
                footer.addCell(right);
                footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 10f, writer.getDirectContent());
            } catch (DocumentException ignored) {
            }
        }
    }
}
