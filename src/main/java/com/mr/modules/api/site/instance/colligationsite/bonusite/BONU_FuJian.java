package com.mr.modules.api.site.instance.colligationsite.bonusite;

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

/***
 * 福建国统局
 * 主题：统计上严重失信企业信息
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 * */
@Slf4j
@Component("bonu_fujian")
@Scope("prototype")
public class BONU_FuJian extends SiteTaskExtend_CollgationSite {

    @Override
    protected String execute() throws Throwable {
        log.info("开始处理福建统计局-失信企业信息");
        WebClient webClient = createWebClient(null,null);
        String mainUrl = "http://tjj.fujian.gov.cn/sxqy/sxqyxx/";
        HtmlPage mainPage = webClient.getPage(mainUrl);
        List divList = mainPage.getByXPath("/html/body/div[3]/div[2]/div[2]");
        //    System.out.println(divList.get(0));
        HtmlDivision division = (HtmlDivision)divList.get(0);

        // HtmlListItem
        List liList = division.getElementsByTagName("li");
        for(int i=0;i<liList.size();i++){
            HtmlListItem htmlListItem = (HtmlListItem)liList.get(i);
            HtmlElement aTag = htmlListItem.getElementsByTagName("a").get(0);
            HtmlElement spanTag = htmlListItem.getElementsByTagName("span").get(0);
            String href = aTag.getAttribute("href");
            href= href.substring(href.indexOf(".")+2);
            String reqUrl = mainUrl+href;
            String title = aTag.getTextContent();
            String publishDate = spanTag.getTextContent();
            HtmlPage page = webClient.getPage(reqUrl);
            HtmlDivision contentDiv = (HtmlDivision)page.getElementById("doc_content");

            String htmlContent = Jsoup.parse(contentDiv.asXml()).html();
            String textContent = contentDiv.getTextContent();

            String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"bonu_fuJian"+File.separator+ MD5Util.encode(reqUrl);
            //下载文件
            saveFile(page,title+".html",filePath);

            //数据入scrapy_data
            //String url,String hashKey,String html,String text
            insert2ScrapyData(reqUrl,filePath,htmlContent,textContent);

            HtmlTable element = (HtmlTable)contentDiv.getElementsByTagName("table").get(0);
            DomNodeList domNodeList = element.getElementsByTagName("tr");
            String judgeNo = "";
            String entName = "";
            String code1 = "";
            String judgeDate = "";
            String judgeAuth = "";
            for(int j=0;j<domNodeList.size();j++){
                HtmlTableRow tableRow = (HtmlTableRow)domNodeList.get(j);
                List<HtmlTableCell> cellList = tableRow.getCells();
                if(cellList.get(0).asText().equalsIgnoreCase("行政处罚决定书文号")){
                    judgeNo = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("行政相对人名称")){
                    entName = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("行政相对人代码-1（统一社会信用代码）")){
                    code1 = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("处罚决定日期")){
                    judgeDate = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("处罚机关")){
                    judgeAuth = cellList.get(1).asText();
                }
            }
            AdminPunish adminPunish = new AdminPunish();
            adminPunish.setSource("福建省统计局");
            adminPunish.setSubject("统计上严重失信企业信息");
            adminPunish.setUniqueKey(reqUrl+"@"+entName+"@"+i+"@"+publishDate);
            adminPunish.setUrl(reqUrl);
            adminPunish.setObjectType("01");
            adminPunish.setEnterpriseName(entName);
            adminPunish.setEnterpriseCode1(code1);
            adminPunish.setJudgeNo(judgeNo);
            adminPunish.setPublishDate(publishDate);
            adminPunish.setJudgeAuth(judgeAuth);
            adminPunish.setJudgeDate(judgeDate);
            //数据入库
            if(adminPunishMapper.selectByUrl(reqUrl,entName,null,judgeNo,judgeAuth).size()==0){
                adminPunishMapper.insert(adminPunish);
            }
        }

        webClient.close();
        log.info("结束处理福建统计局-失信企业信息");
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
        scrapyData.setSource("福建省统计局");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
