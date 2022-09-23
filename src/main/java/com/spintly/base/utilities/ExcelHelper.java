package com.spintly.base.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.spintly.base.managers.ResultManager;
import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHelper {

    public List<Object> extractExcel(String filename) throws IOException {
        List<Object> content = new ArrayList<Object>();
        try {
            FileInputStream fis = new FileInputStream(new File(filename));
            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);
            FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
            String data = "";
            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case NUMERIC:
                            System.out.print(cell.getNumericCellValue() + "\t\t");
                            data = data + cell.getNumericCellValue();
                            content.add(cell.getNumericCellValue());
                            break;
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t\t");
                            data = data + cell.getStringCellValue();
                            content.add(cell.getStringCellValue());
                            break;
                    }
                }
            }
            wb.close();
            new File(filename).delete();
            File dir = new File(Paths.get("").toAbsolutePath() + File.separator + "Downloads");
//            FileUtils.cleanDirectory(dir);
        }
        catch(Exception e)
        {
            ResultManager.error("Excel file should be read", "Excel file not read :"+ filename, e.getMessage(), true);
        }
        return content;
    }

}
