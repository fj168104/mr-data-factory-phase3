package com.mr.common.util;

import com.google.common.collect.Lists;
import com.mr.modules.api.xls.importfile.FileImportExecutor;
import com.mr.modules.api.xls.importfile.domain.MapResult;
import com.mr.modules.api.xls.importfile.domain.common.Configuration;
import com.mr.modules.api.xls.importfile.domain.common.ImportCell;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pxu 2018/7/20 11:01
 */
@Slf4j
public class ExcelUtil {

    /**
     * 将excel文件内容生成List<Map<String, Object>>
     *
     * @param xlsPath     excel文件路径
     * @param columnNames 列名数组
     * @return
     * @throws Exception
     */
    public static List<Map<String, Object>> importFromXls(String xlsPath, String[] columnNames) throws Exception {
        File importFile = new File(xlsPath);
        Configuration configuration = new Configuration();
        configuration.setStartRowNo(1);
        List<ImportCell> importCells = Lists.newArrayList();
        for (int i = 0; i < columnNames.length; i++) {
            importCells.add(new ImportCell(i, columnNames[i]));
        }
        configuration.setImportCells(importCells);
        configuration.setImportFileType(Configuration.ImportFileType.EXCEL);

        MapResult mapResult = (MapResult) FileImportExecutor.importFile(configuration, importFile, importFile.getName());
        return mapResult.getResult();
    }

    /**
     * 提取Excel中的内容作为一个字符串
     * @param filePath
     * @return
     */
    public static String textExtractXls(String filePath){
        String text = "";
        if(filePath.endsWith(".xls")){
            try {
                text = readXls(filePath).toString().replaceAll("\\[","").replaceAll("\\]","");
            } catch (Exception e) {
                log.error("处理xls异常···"+e.getMessage());
            }
        }else {
            try {
                text = readXlsx(filePath).toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll("[,][\\s]{0,}[,]","");
            } catch (Exception e) {
               log.error("处理xlsx异常···"+e.getMessage());
            }
        }
        return text;
    }
    /**
     * @Time：2018/8/23
     * @Description：读xls类型的文件
     * @修改人：
     * @修改时间：
     */
    public static List<List<String>> readXls(String filePath) throws Exception {
        InputStream is = new FileInputStream(filePath);
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook(is);//HSSFWorkbook表示整个Excel
        List<List<String>> result = new ArrayList<>();
        //循环每一页，并处理当前的循环页
        for (int numSheet = 0; numSheet < hssfWorkbook.getNumberOfSheets(); numSheet++) {
            HSSFSheet hssfSheet = hssfWorkbook.getSheetAt(numSheet);//HSSFSheet表示某一页
            if (hssfSheet == null) {
                continue;
            }
            //处理当前页，循环处理每一行的数据
            for (int rowNum = 1; rowNum <= hssfSheet.getLastRowNum(); rowNum++) {
                HSSFRow hssfRow = hssfSheet.getRow(rowNum);//HSSFRow表示每一行的数据
                int minColIx = hssfRow.getFirstCellNum();
                int maxColIx = hssfRow.getLastCellNum();
                List<String> rowList = new ArrayList<>();
                //遍历该行，并获取每一个cell的数据
                for (int colIx = minColIx; colIx < maxColIx; colIx++) {
                    HSSFCell hssfCell = hssfRow.getCell(colIx);
                    if (hssfCell == null) {
                        continue;
                    }
                    rowList.add(getStringVal(hssfCell));
                }
                result.add(rowList);
            }

        }
        return result;
    }

    /**
     * @Time：2018/8/23
     * @Description：读xlsx类型的文件
     * @修改人：
     * @修改时间：
     */
    public static List<List<String>> readXlsx(String filePath) throws Exception {
        InputStream is = new FileInputStream(filePath);
        XSSFWorkbook xssfWorkbook = new XSSFWorkbook(is);
        List<List<String>> result = new ArrayList<>();
        //循环每一页，并处理当前的循环页
        for (Sheet sheet : xssfWorkbook) {
            if (sheet == null) {
                continue;
            }
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);//Row表示每一行的数据
                int minColIx = row.getFirstCellNum();
                int maxColIx = row.getLastCellNum();
                List<String> rowList = new ArrayList<>();
                //遍历该行，并获取每一个cell的数据
                for (int colIx = minColIx; colIx < maxColIx; colIx++) {
                    Cell cell = row.getCell(colIx);
                    if (cell == null) {
                        continue;
                    }
                    rowList.add(cell.toString());
                }
                result.add(rowList);
            }
        }
        return result;
    }

    /**
     * 根据类型格式化数据并输出
     * @param cell
     * @return
     */
    private static String getStringVal(HSSFCell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue() ? "true" : "false";
            case Cell.CELL_TYPE_FORMULA:
                return cell.getCellFormula();
            case Cell.CELL_TYPE_NUMERIC:
                cell.setCellType(Cell.CELL_TYPE_STRING);
                return cell.getStringCellValue();
            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();
            default:
                return "";
        }
    }
}
