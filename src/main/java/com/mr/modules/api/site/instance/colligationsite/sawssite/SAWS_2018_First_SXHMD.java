package com.mr.modules.api.site.instance.colligationsite.sawssite;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.common.collect.Lists;
import com.mr.common.OCRUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import com.mr.modules.api.xls.importfile.FileImportExecutor;
import com.mr.modules.api.xls.importfile.domain.MapResult;
import com.mr.modules.api.xls.importfile.domain.common.Configuration;
import com.mr.modules.api.xls.importfile.domain.common.ImportCell;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 站点：国家安全生产监督管理总局网站
 * url：http://old.chinasafety.gov.cn/zwdt/gwgg/201803/t20180302_176730.shtml
 * 主题：2018年第一批安全生产失信联合惩戒“黑名单”单位及其人员名单
 * 属性：单位名称、注册地址、统一社会信用代码、主要负责人、身份证号、失信行为简况、信息报送机关、纳入理由、发布日期
 * 提取：TODO 单位名称 发布日期  信息报送机关
 */
@Slf4j
@Component("swas_2018_first_sxhmd")
@Scope("prototype")
public class SAWS_2018_First_SXHMD extends SiteTaskExtend_CollgationSite {
    @Override
    protected String execute() throws Throwable {

        log.info("开始处理国家安全生产监督管理总局网站-2018年度第一批失信黑名单");
        String reqUrl = "http://old.chinasafety.gov.cn/zwdt/gwgg/201803/t20180302_176730.shtml";
        String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"swas_2018_first_sxhmd"+File.separator+ MD5Util.encode(reqUrl);
        WebClient webClient = createWebClient(null,null);
        HtmlPage htmlPage = webClient.getPage(reqUrl);

        DomElement nrBox = htmlPage.getElementById("nrBox");
        String textContent = nrBox.getTextContent();
      //  log.info("文本内容：{}",textContent);
        String htmlText = Jsoup.parse(nrBox.asXml()).html();
      //  log.info("html正文：{}",htmlText);
        String title = "国家安全生产监督管理总局公告(2017年第16号)2017年第三批安全生产失信联合惩戒“黑名单”单位及其人员名单";
        String wenHao = "2018年第6号";
        String publishDate = "2018年3月1日";
        String htmlFileName = title+".html";
        //下载html文件
        saveFile(htmlPage,htmlFileName,filePath);
        //下载附件
        List list = htmlPage.getByXPath("//*[@id=\"nrBox\"]/div/p[4]/span/a");
        HtmlAnchor aTag = (HtmlAnchor)list.get(0);
        String xlsFileName = aTag.getAttribute("href").split("/")[1];
        Page xlsPage = aTag.click();
        saveFile(xlsPage,xlsFileName,filePath);

        String[] colNames = {"序号","单位名称","注册地址","统一社会信用代码","主要负责人","身份证号","失信行为简况","信息报送机关","纳入理由"};
        List<Map<String, Object>> DataList = importFromXls(filePath+File.separator+xlsFileName,colNames);
        int i = 0;
        for(Map<String, Object> map : DataList){
            i++;
            String entName = (String)map.get("单位名称");
            String enterpriseCode1 = (String)map.get("统一社会信用代码");
            String personName = (String)map.get("主要负责人");
            String personId = (String)map.get("身份证号");
            String punishAccording = (String)map.get("失信行为简况");
            String judgeAuth = (String)map.get("信息报送机关");
            String punishReason = (String)map.get("纳入理由");
            String punishType = "失信黑名单";
            AdminPunish adminPunish = new AdminPunish();
            adminPunish.setSource("国家安全生产监督管理总局网站");
            adminPunish.setSubject("2018年第一批安全生产失信联合惩戒“黑名单”单位及其人员名单");
            adminPunish.setUniqueKey(reqUrl+"@"+entName+"@"+i+"@"+publishDate);
            adminPunish.setUrl(reqUrl);
            adminPunish.setObjectType("01");
            adminPunish.setEnterpriseName(entName);
            adminPunish.setEnterpriseCode1(enterpriseCode1);
            /*adminPunish.setPersonName(personName);
            adminPunish.setPersonId(personId);*/
            adminPunish.setPunishAccording(punishAccording);
            adminPunish.setPunishReason(punishReason);
            adminPunish.setPunishType(punishType);
            adminPunish.setJudgeNo(wenHao);
            adminPunish.setJudgeDate(publishDate);
            adminPunish.setJudgeAuth(judgeAuth);
            //数据入库
            if(adminPunishMapper.selectByUrl(reqUrl,entName,personName,wenHao,judgeAuth).size()==0){
                adminPunishMapper.insert(adminPunish);
            }
        }

        //数据入库
        insert2ScrapyData(reqUrl,filePath,htmlText,textContent);

        webClient.close();
        log.info("结束处理国家安全生产监督管理总局网站-2018年度第一批失信黑名单");
        return "";
    }


    /**
     * 将数据insert到scrapy_data表
     * */
    public void insert2ScrapyData(String url,String hashKey,String html,String text){
        ScrapyData scrapyData = new ScrapyData();
        scrapyData.setUrl(url);
        scrapyData.setHashKey(hashKey);
        scrapyData.setHtml(html);
        scrapyData.setText(text);
        scrapyData.setAttachmentType("xls");
        scrapyData.setSource("国家安全生产监督管理总局网站");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }



    public List<Map<String, Object>> importFromXls(String FilePath, String[] columeNames) throws Exception {
        File importFile = new File(FilePath);
        Configuration configuration = new Configuration();

        configuration.setStartRowNo(5);
        List<ImportCell> importCells = Lists.newArrayList();
        for (int i = 0; i < columeNames.length; i++) {
            importCells.add(new ImportCell(i, columeNames[i]));
        }
        configuration.setImportCells(importCells);
        configuration.setImportFileType(Configuration.ImportFileType.EXCEL);

        MapResult mapResult = (MapResult) FileImportExecutor.importFile(configuration, importFile, importFile.getName());
        List<Map<String, Object>> maps = mapResult.getResult();

        return maps;
    }
}
