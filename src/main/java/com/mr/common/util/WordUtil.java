package com.mr.common.util;

import com.mr.framework.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hwpf.HWPFDocument;
    import org.apache.poi.hwpf.model.PicturesTable;
    import org.apache.poi.hwpf.usermodel.*;
    import org.apache.poi.poifs.filesystem.POIFSFileSystem;
    import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
    import org.apache.poi.xwpf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * WORD工具类
 *
 * @author pxu 2018/8/15 10:12
 */
@Slf4j
public class WordUtil {

    /**
     * 读取word表格数据(支持2003版和2007版word)
     *
     * @param filePath word文件路径
     * @return List三层结构为：<表格List<行List<列List>>>
     */
    public static List<List<List<String>>> readWordTable(String filePath) {
        if (filePath != null && filePath.endsWith(".doc")) {
            return readWordTable2003(filePath);
        } else {
            return readWordTable2007(filePath);
        }
    }


    /**
     * 读取2003版word表格数据
     *
     * @param filePath word文件路径
     * @return List三层结构为：<表格List<行List<列List>>>
     */
    public static List<List<List<String>>> readWordTable2003(String filePath) {
        List<List<List<String>>> returnList = new ArrayList<>();
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
            POIFSFileSystem pfs = new POIFSFileSystem(in);
            Range range = new HWPFDocument(pfs).getRange();
            TableIterator it = new TableIterator(range);
            //迭代文档中的表格
            while (it.hasNext()) {
                List<List<String>> tableList = new ArrayList<>();
                Table tb = it.next();
                //迭代行，默认从0开始
                for (int i = 0; i < tb.numRows(); i++) {
                    List<String> rowList = new ArrayList<>();
                    TableRow tr = tb.getRow(i);
                    boolean isEmptyRow = true;
                    //迭代列，默认从0开始
                    for (int j = 0; j < tr.numCells(); j++) {
                        TableCell td = tr.getCell(j);//取得单元格
                        //取得单元格的内容
                        StringBuilder cell = new StringBuilder();
                        for (int k = 0; k < td.numParagraphs(); k++) {
                            Paragraph para = td.getParagraph(k);
                            cell.append(para.text().trim());
                        }
                        rowList.add(cell.toString());
                        if (isEmptyRow && StrUtil.isNotBlank(cell.toString())) {
                            isEmptyRow = false;
                        }
                    }
                    if (!isEmptyRow) {//判断是否为空行,不为空行时才加入tableList中
                        tableList.add(rowList);
                    }
                }
                returnList.add(tableList);
            }
        } catch (Exception e) {
            log.warn("读取word表格失败", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return returnList;
    }


    /**
     * 读取2007版WORD的表格数据
     *
     * @param filePath word文件路径
     * @return List三层结构为：<表格List<行List<列List>>>
     */
    public static List<List<List<String>>> readWordTable2007(String filePath) {
        List<List<List<String>>> returnList = new ArrayList<>();
        FileInputStream in = null;
        try {
            in = new FileInputStream(filePath);
            Iterator<XWPFTable> it = new XWPFDocument(in).getTablesIterator();
            //遍历所有表格
            while (it.hasNext()) {
                List<List<String>> tableList = new ArrayList<>();
                XWPFTable table = it.next();
                List<XWPFTableRow> rows = table.getRows();
                //读取每一行数据
                for (XWPFTableRow row : rows) {
                    boolean isEmptyRow = true;
                    List<String> rowList = new ArrayList<>();
                    List<XWPFTableCell> cells = row.getTableCells();
                    //读取每一列数据
                    for (XWPFTableCell cell : cells) {
                        String text = cell.getText().trim();
                        rowList.add(text);
                        if (isEmptyRow && StrUtil.isNotBlank(text)) {
                            isEmptyRow = false;
                        }
                    }
                    if (!isEmptyRow) {//判断是否为空行,不为空行时才加入tableList中
                        tableList.add(rowList);
                    }
                }
                returnList.add(tableList);
            }
        } catch (Exception e) {
            log.warn("读取word表格失败", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return returnList;
    }

    /**
     * 提取Doc/Docx中的图片
     * @param filePath
     * @param fileName
     * 命名方式为： 源文件名称+i+.jpg
     */

    public static List<String> getImgFromDoc(String filePath,String fileName){
        List<String> fileNameList = new ArrayList<>();
        if(fileName.endsWith(".doc")){
            fileNameList.addAll(getImgConvertDoc(filePath,fileName));
        }else {
            fileNameList.addAll(getImgConvertDocx(filePath,fileName));
        }
        return fileNameList;
    }
    /**
     * 提取Doc中的图片
     * @param filePath
     * @param fileName
     * 命名方式为： 源文件名称+i+.jpg
     */
    public static List<String> getImgConvertDoc(String filePath,String fileName){
        List<String> fileNameList = new ArrayList<>();
        try {
            log.info("****************Doc开始****************");
            FileInputStream in=new FileInputStream(new File(filePath+File.separator+fileName));
            HWPFDocument doc=new HWPFDocument(in);
            int length=doc.characterLength();
            PicturesTable pTable=doc.getPicturesTable();
            for (int i=0;i<length;i++){
                Range range=new Range(i, i+1,doc);
                CharacterRun cr=range.getCharacterRun(0);
                if(pTable.hasPicture(cr)){
                    Picture pic=pTable.extractPicture(cr, false);
                    String afileName=pic.suggestFullFileName();
                    String fileAbsolute = filePath+File.separator+fileName.split("\\.")[0]+"_"+afileName;
                    log.info("生成的文件全名称为：{}",fileAbsolute);
                    OutputStream out=new FileOutputStream(new File(fileAbsolute));
                    pic.writeImageContent(out);
                    fileNameList.add(fileAbsolute);

                }
            }
        }catch (FileNotFoundException fe){
            log.error("doc文本中的图片提取发生FileNotFoundException，请查阅···"+fe.getMessage());
        }catch (IOException ie){
            log.error("doc文本中的图片提取发生IOException，请查阅···"+ie.getMessage());
        }
        log.info("****************Doc结束****************");
        return fileNameList;

    }
    /**
     * 提取Docx中的图片
     * @param filePath
     * @param fileName
     * 命名方式为： 源文件名称+i+.jpg
     */
    public static List<String> getImgConvertDocx(String filePath,String fileName){
        List<String> fileNameList = new ArrayList<>();
        log.info("****************Docx开始****************");
        File file = new File(filePath+File.separator+fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor xwpfWordExtractor = new XWPFWordExtractor(document);
            String text = xwpfWordExtractor.getText();
            List<XWPFPictureData> picList = document.getAllPictures();
            for (int i = 0;i<picList.size();i++) {
                XWPFPictureData xwpfPictureData = picList.get(i);
                byte[] bytev = xwpfPictureData.getData();
                String fileAbsolute = filePath+File.separator+fileName.split("\\.")[0]+"_"+i+".jpg";
                log.info("生成的文件全名称为：{}",fileAbsolute);
                FileOutputStream fos = new FileOutputStream(fileAbsolute);
                fos.write(bytev);
            }
            fis.close();
        }catch (FileNotFoundException fe){
            log.error("docx文本中的图片提取发生FileNotFoundException，请查阅···"+fe.getMessage());
        }catch (IOException ie){
            log.error("docx文本中的图片提取发生IOException，请查阅···"+ie.getMessage());
        }
        log.info("****************Docx结束****************");
        return fileNameList;
    }
    public static void main(String[] args) {
        String filePath = "C:\\Users\\Space\\Desktop";
        String fileName ="新建 Microsoft Word 文档.docx";
        getImgFromDoc(filePath,fileName);
        //getImgDocx(path);
        /*List<List<List<String>>> tableList = readWordTable("E:\\ChromeDownload\\P020160920512946160717.doc");
        for (List<List<String>> rowList : tableList) {
            System.out.println(rowList.size());
            for (List<String> cellList : rowList) {
                for (String cell : cellList) {
                    System.out.print(cell);
                    System.out.print("\t");
                }
                System.out.print("\n");
            }
            System.out.print("\n");
        }*/
    }
}
