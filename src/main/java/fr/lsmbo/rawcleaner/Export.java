package fr.lsmbo.rawcleaner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Export {

    protected static final Logger logger = LoggerFactory.getLogger(Export.class);

    private final static String[] headers = new String[] { "File path", "Is RAW data", "Creation date", "Last modified", "Size", "Size (octets)", "File count", "Archive path", "Is unlocked", "Is valid", "Is checked for deletion", "Information" };

    public static File getDefaultFile() {
        String fileName = AppInfo.getAppName() + "_" + Global.getHostname() + "_"+Global.formatDate(new Date().getTime(), false) + "_" + Global.getUsername() + ".xlsx";
        File file = new File(Global.REPORTS_DIRECTORY, fileName);
        // if the file already exists, replace the date of the day by the full date and time
        if(file.exists()) {
            fileName = AppInfo.getAppName() + "_" + Global.getHostname() + "_"+Global.simpleFormatDate(new Date().getTime()) + "_" + Global.getUsername() + ".xlsx";
            file = new File(Global.REPORTS_DIRECTORY, fileName);
        }
        logger.info("Default report file name should be: "+fileName);
        return file;
    }

    public static void toXLSX(List<String[]> data, File file, String reportType) throws Throwable {

        logger.info("Creating the Excel report");

        // creation of the excel object
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(file.getName());
        int rowNum = 0;

        // Describe the environment first
        // App name and version
        addRow(sheet, rowNum++, new String[] { "Software name", AppInfo.getAppName() });
        addRow(sheet, rowNum++, new String[] { "Software version", AppInfo.getAppVersion() });
        // Report type and date of generation of the export
        addRow(sheet, rowNum++, new String[] { "Report type", reportType });
        addRow(sheet, rowNum++, new String[] { "Report date", Global.formatDate(new Date().getTime()) });
        // Local uname and user
        addRow(sheet, rowNum++, new String[] { "Hostname", Global.getHostname() });
        addRow(sheet, rowNum++, new String[] { "User name", Global.getUsername() });
        // Raw data directory
        addRow(sheet, rowNum++, new String[] { "RAW data directory", Global.RAW_DATA_DIRECTORY.getAbsolutePath() });
        // MS Access information
        addRow(sheet, rowNum++, new String[] { "MS Access path", Global.MS_ACCESS_PATH.getAbsolutePath() });
        addRow(sheet, rowNum++, new String[] { "MS Access version", MsAccess.getVersion() });
        addRow(sheet, rowNum++, new String[] { "MS Access build date", MsAccess.getBuildDate() });

        // add an empty line
        addRow(sheet, rowNum++, new String[] {});

        // write the headers
        addRow(sheet, rowNum++, headers);

        // write the data content
        for(String[] line : data) {
            addRow(sheet, rowNum++, line);
        }

        // resize all columns to fit the content size
        for (int i = 0; i < headers.length; i++) {
            if(i == 1) sheet.setColumnWidth(i, 3000);
            else sheet.autoSizeColumn(i);
        }

        // write the output to a file
        FileOutputStream fileOut = new FileOutputStream(file);
        workbook.write(fileOut);
        workbook.close();
        fileOut.close();

        logger.info("Excel report has been successfully created");
    }

    private static void addRow(Sheet sheet, int rowNumber, String[] items) {
        Row row = sheet.createRow(rowNumber);
        for(int i = 0; i < items.length; i++) {
            row.createCell(i).setCellValue(items[i]);
        }
    }
}
