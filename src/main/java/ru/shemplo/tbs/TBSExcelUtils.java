package ru.shemplo.tbs;

import java.time.LocalDate;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class TBSExcelUtils {
    
    public static Row getRow (Sheet sheet, int index) {
        final var row = sheet.getRow (index);
        return row == null ? sheet.createRow (index) : row;
    }
    
    public static Cell getCell (Row row, int index) {
        final var cell = row.getCell (index);
        return cell == null ? row.createCell (index) : cell;
    }
    
    public static void setValue (Cell cell, Object value) {
        if (value instanceof String string) {
            cell.setCellValue (string);
        } else if (value instanceof Number number) {
            cell.setCellValue (number.doubleValue ());
        } else if (value instanceof LocalDate date) {
            cell.setCellValue (date);
        } else {
            cell.setBlank ();
        }
    }
    
    public static Cell getCell (Sheet sheet, int row, int cell) {
        return getCell (getRow (sheet, row), cell);
    }
    
    public static void setValue (Sheet sheet, int row, int cell, Object value) {
        setValue (getCell (sheet, row, cell), value);
    }
    
}
