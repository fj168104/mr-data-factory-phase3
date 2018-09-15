package com.mr.modules.api.site.instance.colligationsite.haikwansite.qingdao;

import com.mr.common.OCRUtil;
import com.mr.common.util.BaiduOCRUtil;
import com.mr.common.util.WordUtil;
import com.mr.framework.ocr.OcrUtils;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.FilenameFilterUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：青岛海关知识产权行政处罚
 * url:http://qingdao.customs.gov.cn/qingdao_customs/406484/406544/406553/406554/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_qingdao_zscq")
public class HaiKuan_QingDao_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    private String source = "青岛海关";
    private String subject = "青岛海关知识产权行政处罚";
    private String judgeAuth = "青岛海关";
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
    //    String source = "青岛海关知识产权行政处罚";
        String area = "qingdao";//区域为：青岛
        String baseUrl = "http://qingdao.customs.gov.cn";
        String url = "http://qingdao.customs.gov.cn/qingdao_customs/406484/406544/406553/406554/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    /**
     * 提取网页中附件为：doc文本
     * @map Map用户存储，filePath(附件所在路径)，attachmentName(附件名称),publishDate,text(附件文本)，详情网页地址：sourceUrl
     */
    public void extractDocData(Map<String,String> map){
        parseText(getInfo(map),map);
    }

    public void extractPdfData(Map<String,String> map){
        parseText(getInfo(map),map);
    }

    public void extractImgData(Map<String,String> map){
        parseText(getInfo(map),map);
    }

    public void parseText(String resultStr,Map<String,String> map){
        String publishDate = map.get("publishDate");
        String url = map.get("sourceUrl");
        String name = getName(resultStr,map.get("title"));

        String uniquekey = map.get("sourceUrl")+"@"+name+"@"+publishDate;
        String objectType = "01";
        String punishReason = resultStr;
        AdminPunish adminPunish = new AdminPunish();
        if(name.length()<5){
            objectType = "02";
            adminPunish.setPersonName(name);
        }else{
            adminPunish.setEnterpriseName(name);
        }
        String judgeNo = getJudgeNo(resultStr);
        adminPunish.setSource(source);
        adminPunish.setSubject(subject);
        adminPunish.setUniqueKey(uniquekey);
        adminPunish.setUrl(url);
        adminPunish.setObjectType(objectType);
        adminPunish.setJudgeAuth(judgeAuth);
        adminPunish.setPunishReason(punishReason);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setJudgeNo(judgeNo);

        //数据入库
        if(adminPunishMapper.selectByUrl(url,null,null,null,judgeAuth).size()==0){
            adminPunishMapper.insert(adminPunish);
        }
    }
    private String getName(String str,String title){
        String nameInfo = "";
        if(title.contains("关于") && title.contains("公司")){
            nameInfo = title.substring(title.indexOf("关于")+2,title.indexOf("公司")+2);
            return nameInfo;
        }
        if(str.contains("琭海乐天进出有公司")){
            nameInfo = "珠海乐天进出口有限公司";
            return nameInfo;
        }
        String regEx="当事人：(.*?)地址：";
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regEx);// 匹配的模式
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        try{
            if(list.size()>0){
                StringBuffer name = new StringBuffer();
                for(String string : list){
                    string = string.replace(",","，");
                    if(string.contains("，"))
                        string = string.substring(0,string.indexOf("，")).trim();
                    name = name.append(string).append("，");
                }
                nameInfo = name.toString();
                nameInfo = nameInfo.substring(0,nameInfo.lastIndexOf("，"));
                if(list.size()==1){
                    if(nameInfo.contains("公司")){
                        nameInfo = nameInfo.substring(0,nameInfo.indexOf("公司")+2);
                    }else if(nameInfo.contains("电话")){
                        nameInfo = nameInfo.substring(0,nameInfo.indexOf("电话"));
                    }
                }
            }else{
                str = str.substring(str.indexOf("决定书")+3,str.indexOf("，"));
                if(!str.contains("字") && !str.contains("号")){
                    nameInfo = str;
                }else{
                    if(str.contains("公司")){
                        nameInfo = str.substring(str.indexOf("号")+1,str.indexOf("公司")+2).replace("当事人：","");
                    }else if(str.contains("20")){
                        if(str.indexOf("当事人：")>str.indexOf("号")){
                            str = str.substring(str.indexOf("当事人："));
                            nameInfo = str.substring(str.indexOf("当事人：")+4,str.indexOf("20"));
                        }

                    }

                }
            }
        }catch (Exception e){
            log.error("获取当事人名称失败，文章为 {}",str);
            log.error(e.getMessage());
        }
        return nameInfo.replace("人:","").replace("人：","").trim();
    }

    private String getJudgeNo(String str){
        String judgeNo = "";
        try{
            str = str.substring(str.indexOf("海关")+2);
            if(str.contains("字") && str.contains("号")){
                judgeNo = str.substring(str.indexOf("关")-1,str.indexOf("号")+1);
            }else{
                judgeNo = str.substring(str.indexOf("关")-1)+"号";
            }

            if(judgeNo.length()>25){//判定ocr识别有误，无法取得准确的值
                return "";
            }
        }catch (Exception e){
            log.error("获取处罚文号失败，文章为 {}",str);
            log.error(e.getMessage());
        }

        return judgeNo;
    }
    private String getInfo(Map<String,String> map){
        String resultStr = "";
        String url = map.get("sourceUrl");
        String attachmentName = map.get("attachmentName");
        String tail = map.get("attachmentName").substring(attachmentName.indexOf("."));
        FilenameFilterUtil filenameFilterUtil = new FilenameFilterUtil(tail);
        String filePath = map.get("filePath");
        File file = new File(filePath);
        List attchmentList = new ArrayList();
        if(file.isDirectory()){
            File[] files = file.listFiles(filenameFilterUtil);
            for(File attchmentFile : files){
                attchmentList.add(attchmentFile.getPath());
            }
        }
        if(tail.toLowerCase().equalsIgnoreCase(".pdf")){
            try {
                resultStr = parsePdfInfo(filePath,attachmentName);
            } catch (Exception e) {
                log.error("pdf解析失败，URL为 {}",url);
            }
        }else if(tail.toLowerCase().contains(".doc")){
            try {
                resultStr = new OCRUtil().getTextFromDocAutoFilePath(filePath,attachmentName);
                resultStr = resultStr.replaceAll ("\r|\n*","").replace(" ","");
                if(resultStr.equalsIgnoreCase("")){//说明doc里包含图片
                    //获取doc里的图片
                    List<String> list = WordUtil.getImgFromDoc(filePath,attachmentName);
                    //解析图片
                    resultStr = BaiduOCRUtil.getTextStrFromImageFileList(list);
                }
            } catch (Exception e) {
                log.error("doc解析失败，URL为 {}",url);
            }
        }else{
            try{
                resultStr = BaiduOCRUtil.getTextStrFromImageFileList(attchmentList);
            }catch (Exception e) {
                log.error("image解析失败，URL为 {}",url);
                log.error(e.getMessage());
            }
        }
        resultStr = resultStr.replace("」","")
                .replace("当事人:","当事人：")
                .replace("号事人:","号当事人: ")
                .replace("当事人;","当事人：")
                .replace("地北:","地址: ")
                .replace("地址:","地址：")
                .replace("地址;","地址：")
                .replace("住所:","地址：")
                .replace("公可","公司")
                .replace("有限公书","有限公司")
                .replace("有限公司法定代表","有限公司,法定代表")
                .replace("有限公法定代表人","有限公司,法定代表")
                .replace("有法定代表","有限公司,法定代表")
                .replace("当事人名称:","当事人：")
                .replace("决定書","决定书")
                .replace("当入:","当事人：")
                .replace("当斗人:","当事人：")
                .replace("有限公司法代素人","有限公司,法定代表人")
                .replace("知這字","知违字")
                .replace(",","，")
        ;
        if(resultStr.contains("号当事") && !resultStr.contains("号当事人：")){
            resultStr = resultStr.replace("号当事","号当事人：");
        }
        if(resultStr.contains("地址") && !resultStr.contains("地址：")){
            resultStr = resultStr.replace("地址","地址：");
        }
        if(resultStr.contains("号") && !resultStr.contains("号当事人：")){
            resultStr = resultStr.replace("号","号当事人：");
        }
        return resultStr;
    }

    public String parsePdfInfo(String filePath,String attachmentName) throws Exception {
        String resultStr = "";
        OcrUtils ocr = new OcrUtils(filePath);
        File textFile = new File(ocr.readPdf(attachmentName));//解析pdf成txt
        resultStr = FileUtils.readFileToString(textFile, "utf-8");//读取txt
        FileUtils.deleteQuietly(textFile);//删除txt
        resultStr = resultStr.replaceAll ("\r|\n*","").replace(" ","");
        if(resultStr.equalsIgnoreCase("")){//说明pdf里包含图片
            List<File> list = pdf2image(filePath + File.separator + attachmentName, false);
            List<String> fileList = new ArrayList();
            for(File file : list){
                fileList.add(file.getPath());
            }
            resultStr = BaiduOCRUtil.getTextStrFromImageFileList(fileList).replace(" ","");
        }

        return resultStr;
    }

    public List<File> pdf2image(String pdfName, boolean needDelete) {
        List<File> pngList = new ArrayList<>();
        File file = new File(pdfName);
        try {
            PDDocument doc = PDDocument.load(file);
            PDFRenderer renderer = new PDFRenderer(doc);
            int pageCount = doc.getNumberOfPages();
            for (int i = 0; i < pageCount; ++i) {
                BufferedImage image = renderer.renderImageWithDPI(i, 96.0F);//读取pdf
                File pngFile = new File(file.getParentFile(), i + ".png");
                ImageIO.write(image, "PNG", pngFile);//写png文件
                pngList.add(pngFile);
            }
            if (needDelete) {
                file.delete();//删除PDF
            }
            doc.close();
        } catch (IOException e) {
            log.warn("convert pdf to image failed...", e);
        }
        return pngList;
    }
}
