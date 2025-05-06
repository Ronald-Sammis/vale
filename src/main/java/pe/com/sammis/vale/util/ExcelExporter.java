package pe.com.sammis.vale.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelExporter {

    private static final List<String> DATE_FORMATS = Arrays.asList(
            "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy"
    );

    private static Date tryParseDate(String value) {
        for (String format : DATE_FORMATS) {
            try {
                return new SimpleDateFormat(format).parse(value);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    /**
     * Genera un archivo Excel desde datos tabulares (filas y columnas), detectando fechas.
     *
     * @param headers Lista de encabezados
     * @param rows Lista de filas (cada fila es una lista de valores de celda)
     * @return ByteArrayOutputStream listo para ser descargado
     */
    public static ByteArrayOutputStream exportToExcel(List<String> headers, List<List<String>> rows) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Datos");

            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Estilo para celdas de fecha (formato ES)
            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            // Encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Datos
            for (int i = 0; i < rows.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                List<String> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    String cellValue = rowData.get(j);
                    Date parsedDate = tryParseDate(cellValue);
                    Cell cell = dataRow.createCell(j);

                    if (parsedDate != null) {
                        cell.setCellValue(parsedDate);
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(cellValue);
                    }
                }
            }

            // Auto ajustar columnas
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ByteArrayOutputStream exportToExcelOptimized(List<String> headers, List<List<String>> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 100 filas en memoria
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            SXSSFSheet sheet = workbook.createSheet("Datos");

            // Reutilizar estilos
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CreationHelper createHelper = workbook.getCreationHelper();
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            // Encabezados
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Datos
            for (int i = 0; i < rows.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                List<String> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    String cellValue = rowData.get(j);
                    Date parsedDate = tryParseDate(cellValue);
                    Cell cell = dataRow.createCell(j);

                    if (parsedDate != null) {
                        cell.setCellValue(parsedDate);
                        cell.setCellStyle(dateStyle);
                    } else {
                        cell.setCellValue(cellValue);
                    }
                }
            }

            // Establecer un ancho de columna predeterminado
            int defaultColumnWidth = 20 * 256; // Ancho para 20 caracteres
            for (int i = 0; i < headers.size(); i++) {
                sheet.setColumnWidth(i, defaultColumnWidth);
            }

            workbook.write(out);
            ((SXSSFWorkbook) workbook).dispose();  // Liberar recursos de SXSSFWorkbook
            return out;

        } catch (IOException e) {
            // Loggear la excepción de manera adecuada
            System.err.println("Error durante la exportación: " + e.getMessage());
            return null;
        }
    }

}
