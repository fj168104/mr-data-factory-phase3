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
 * 湖北国统局
 * 主题：统计上严重失信企业信息
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 * */
@Slf4j
@Component("bonu_huBei")
@Scope("prototype")
public class BONU_HuBei extends SiteTaskExtend_CollgationSite {
    @Override
    protected String execute() throws Throwable {
        log.info("开始处理湖北统计局-失信企业信息");
        WebClient webClient = createWebClient(null,null);
        String mainUrl = "http://www.stats-hb.gov.cn/info/iList.jsp?site_id=CMSwww&cat_id=10622&cur_page=1";
        HtmlPage mainPage = webClient.getPage(mainUrl);
        List list = mainPage.getByXPath("//div[@class='main fr']");

        HtmlDivision mainDiv = (HtmlDivision) list.get(0);
        HtmlUnorderedList ulList = (HtmlUnorderedList)mainDiv.getElementsByTagName("ul").get(0);
        System.out.println(ulList.getElementsByTagName("li").size());
        List liList = ulList.getElementsByTagName("li");
        String baseUrl = "http://www.stats-hb.gov.cn";
        for(int i=0;i<liList.size();i++){
            HtmlListItem htmlListItem = (HtmlListItem)liList.get(i);
            HtmlElement aTag = htmlListItem.getElementsByTagName("a").get(0);
            String href = aTag.getAttribute("href");
            String title = aTag.getAttribute("title");
            HtmlElement pTag = htmlListItem.getElementsByTagName("p").get(0);
            String publishDate = pTag.getTextContent().split("：")[1];

            String reqUrl = baseUrl+href;

            HtmlPage page = webClient.getPage(reqUrl);
            List Divlist = page.getByXPath("//div[@class='article-box']");
            HtmlDivision contentDiv = (HtmlDivision)Divlist.get(0);
            String htmlContent = Jsoup.parse(contentDiv.asXml()).html();
            String textContent = contentDiv.getTextContent();

            String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"bonu_huBei"+File.separator+ MD5Util.encode(reqUrl);
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
            adminPunish.setSource("湖北省统计局");
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
        log.info("结束处理湖北统计局-失信企业信息");
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
        scrapyData.setSource("湖北省统计局");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
