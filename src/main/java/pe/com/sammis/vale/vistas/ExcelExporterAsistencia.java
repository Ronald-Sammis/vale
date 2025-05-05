package pe.com.sammis.vale.vistas;

import pe.com.sammis.vale.models.Asistencia;
import pe.com.sammis.vale.repositories.AsistenciaRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Date;
import java.time.ZoneId;

@Service
public class ExcelExporterAsistencia {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    public byte[] exportarAsistenciasAExcel() throws IOException {
        // Crear un nuevo workbook de Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Asistencias");

        // Crear la primera fila con los encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID");
        headerRow.createCell(1).setCellValue("Fecha");
        headerRow.createCell(2).setCellValue("Nombre Empleado");
        headerRow.createCell(3).setCellValue("Apellido Empleado");
        headerRow.createCell(4).setCellValue("Tipo de Asistencia");


        // Estilo de celda para la fecha con formato dd/MM/yyyy
        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper creationHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(creationHelper.createDataFormat().getFormat("dd/MM/yyyy"));

        // Obtener la lista de asistencias
        List<Asistencia> asistencias = asistenciaRepository.findAll();

        // Llenar las filas con los datos
        int rowNum = 1;
        for (Asistencia asistencia : asistencias) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(asistencia.getId());

            // Convertir LocalDate a Date para Excel y aplicar el estilo
            Cell dateCell = row.createCell(1);
            dateCell.setCellValue(Date.from(asistencia.getFecha().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            dateCell.setCellStyle(dateCellStyle);

            row.createCell(2).setCellValue(asistencia.getEmpleado().getNombre());
            row.createCell(3).setCellValue(asistencia.getEmpleado().getApellido());
            row.createCell(4).setCellValue(asistencia.getTipoAsistencia().getAlias());
        }

        for (int i = 0; i < 5; i++) {
            sheet.autoSizeColumn(i);
        }

        // Convertir el archivo a un array de bytes para enviarlo como respuesta en el controlador
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            workbook.write(bos);
            return bos.toByteArray();
        }
    }
}