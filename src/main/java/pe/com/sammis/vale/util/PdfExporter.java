package pe.com.sammis.vale.util;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class PdfExporter {

    public static ByteArrayOutputStream exportToPdf(List<String> headers, List<List<String>> rows) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            // Crear tabla con el número de columnas basado en los headers
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            // Agregar encabezados
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new Color(220, 220, 220));
                table.addCell(cell);
            }

            // Agregar filas de datos
            for (List<String> row : rows) {
                for (String cellData : row) {
                    PdfPCell cell = new PdfPCell(new Phrase(cellData, cellFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream;
    }
}
