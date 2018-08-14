package com.mr.modules.api.site.instance.colligationsite.mofsite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 站点：财政部-采购公示网站
 * url：http://www.ccgp.gov.cn/search/cr/
 * 主题：政府采购严重违法失信行为记录名单
 * 属性：序号、企业名称、统一社会信用代码（或组织机构代码）、企业地址、严重违法失信行为的具体情形、处罚结果、处罚依据、处罚日期、公布日期、执法单位、
 * 提取：企业名称 公布日期、执法单位
 * 注：输入企业名称，执法单位，时间 进行精确查询
 */
@Slf4j
@Component("mof_cgyzwfsxmd")
@Scope("prototype")
public class MOF_CGYZWFSXMD extends SiteTaskExtend_CollgationSite {

    private String source = "财政部-采购公示网站";
    @Override
    protected String execute() throws Throwable {

        log.info("开始处理【政府采购严重违法失信行为记录名单】");
        WebClient webClient = createWebClient(null,null);
        webClient.getOptions().setJavaScriptEnabled(true);
        String mainUrl = "http://www.ccgp.gov.cn/search/cr/";
        HtmlPage mainPage= webClient.getPage(mainUrl);
        HtmlInlineFrame iframe = (HtmlInlineFrame)mainPage.getElementsByTagName("iframe").get(0);
        String baseUrl = iframe.getAttribute("src");
        HtmlPage listPage = webClient.getPage(baseUrl);
        DomElement span = listPage.getElementById("totalPag");
        int count = Integer.parseInt(span.getTextContent());
        HtmlPage currentPage = listPage;
        for(int i=0;i<count;i++){
            HtmlTable tableInfo = (HtmlTable)currentPage.getElementById("tableInfo");
            String htmlContent = Jsoup.parse(tableInfo.asXml()).html();
            String textContent = tableInfo.getTextContent();
            String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"mof_cgyzwfsxmd"+File.separator+ MD5Util.encode(baseUrl);
            String title = currentPage.getTitleText()+i;
            //下载文件
            saveFile(currentPage,title+".html",filePath);
            baseUrl = baseUrl+i;
            //数据入scrapy_data
            //String url,String hashKey,String html,String text
            insert2ScrapyData(baseUrl+i,filePath,htmlContent,textContent);
            String entName = "";
            String code1 = "";
            String judgeDate = "";
            String publishDate = "";
            String judgeAuth = "";
            List rows = tableInfo.getRows();
            for(int k=1;k<rows.size();k++){
                HtmlTableRow row = (HtmlTableRow)rows.get(k);
                List<HtmlTableCell> cellList = row.getCells();
                entName = cellList.get(1).getTextContent();
                code1 = cellList.get(2).getTextContent();
                judgeDate = cellList.get(7).getTextContent();
                publishDate = cellList.get(8).getTextContent();
                judgeAuth = cellList.get(9).getTextContent();

                AdminPunish adminPunish = new AdminPunish();
                adminPunish.setSource(source);
                adminPunish.setSubject("政府采购严重违法失信行为记录名单");
                adminPunish.setUniqueKey(baseUrl+"@"+entName+"@"+k+"@"+publishDate);
                adminPunish.setUrl(baseUrl);
                adminPunish.setEnterpriseCode1(code1);
                adminPunish.setObjectType("01");
                adminPunish.setEnterpriseName(entName);
                adminPunish.setPublishDate(publishDate);
                adminPunish.setJudgeAuth(judgeAuth);
                adminPunish.setJudgeDate(judgeDate);

                //数据入库
                if(adminPunishMapper.selectByUrl(baseUrl,entName,null,null,judgeAuth).size()==0){
                    adminPunishMapper.insert(adminPunish);
                }

            }
            List divList = currentPage.getByXPath("//div[@class='frontpages']");
            HtmlDivision division = (HtmlDivision)divList.get(0);
            if(i<count-1){
                List aTags = division.getByXPath("//a[@onclick='turnPage(2);']");
                HtmlAnchor anchor = (HtmlAnchor)aTags.get(0);
                currentPage = anchor.click();
            }
        }
        webClient.close();
        log.info("结束处理【政府采购严重违法失信行为记录名单】");
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
        scrapyData.setSource(source);
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
