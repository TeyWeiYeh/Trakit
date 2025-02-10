package domain;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.File;

public class MonthlyReport {
    public HSSFWorkbook excelFile;
    public String monthYear;
    public String name;

    public MonthlyReport(){
    }

    public MonthlyReport(HSSFWorkbook excelFile, String monthYear, String name){
        this.excelFile = excelFile;
        this.monthYear = monthYear;
        this.name = name;
    }

    public HSSFWorkbook getExcelFile(){return excelFile;}
    public void setExcelFile(HSSFWorkbook excelFile){this.excelFile = excelFile;}
    public String getMonthYear() {return monthYear;}
    public void setMonthYear(String monthYear){this.monthYear = monthYear;}
    public String getName() {return name;}
    public void setName(String name){this.name = name;}

}
