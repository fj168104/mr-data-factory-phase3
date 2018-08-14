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
 * 四川统计局
 * 主题：统计上严重失信企业信息
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 * */
@Slf4j
@Component("bonu_siChuan")
@Scope("prototype")
public class BONU_SiChuan extends SiteTaskExtend_CollgationSite {
    @Override
    protected String execute() throws Throwable {
        log.info("开始处理四川统计局-失信企业信息");
        WebClient webClient = createWebClient(null,null);
        String mainUrl = "http://www.sc.stats.gov.cn/ztlm/tjssxqygs/sxqyxx/";
        HtmlPage mainPage = webClient.getPage(mainUrl);
        HtmlDivision division = (HtmlDivision)mainPage.getElementById("p_dis");
        List ulList = division.getElementsByTagName("ul");
        HtmlUnorderedList ul = (HtmlUnorderedList)ulList.get(0);
        List liList = ul.getElementsByTagName("li");
        for(int i=0;i<liList.size();i++){
            HtmlListItem li = (HtmlListItem)liList.get(i);
            String href = li.getElementsByTagName("a").get(0).getAttribute("href");
            href = href.substring(href.indexOf(".")+2);
            String publishDate = li.getElementsByTagName("span").get(0).getTextContent();
            String reqUrl = mainUrl+href;
            HtmlPage page = webClient.getPage(reqUrl);
            String title = page.getTitleText();
            HtmlDivision htmlDivision = (HtmlDivision)page.getElementById("p_disWiap");
            String htmlContent = Jsoup.parse(htmlDivision.asXml()).html();
            List tabList = page.getByXPath("//table[@class='MsoNormalTable']");
            HtmlTable table = (HtmlTable)tabList.get(0);
            String textContent = table.getTextContent();
            String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"bonu_siChuan"+File.separator+ MD5Util.encode(reqUrl);
            String nameTail = publishDate.replace("-","");
            //下载文件
            saveFile(page,title+nameTail+".html",filePath);

            //数据入scrapy_data
            //String url,String hashKey,String html,String text
            insert2ScrapyData(reqUrl,filePath,htmlContent,textContent);
            List rows = table.getRows();
            for(int k=3;k<rows.size();k++){
                HtmlTableRow row = (HtmlTableRow)rows.get(k);
                List<HtmlTableCell> cellList = row.getCells();
                String entName = cellList.get(0).asText();
                String address = cellList.get(1).asText();
                String frName = cellList.get(2).asText();
                String reason = cellList.get(3).asText();
                String result = cellList.get(4).asText();
                String judgeAuth = cellList.get(5).asText();

                AdminPunish adminPunish = new AdminPunish();
                adminPunish.setSource("四川省统计局");
                adminPunish.setSubject("统计上严重失信企业信息");
                adminPunish.setUniqueKey(reqUrl+"@"+entName+"@"+i+"@"+publishDate);
                adminPunish.setUrl(reqUrl);
                adminPunish.setObjectType("01");
                adminPunish.setEnterpriseName(entName);
                adminPunish.setPublishDate(publishDate);
                adminPunish.setJudgeAuth(judgeAuth);
                adminPunish.setPersonName(frName);
                adminPunish.setPunishResult(result);
                adminPunish.setPunishReason(reason);
                
                //数据入库
                if(adminPunishMapper.selectByUrl(reqUrl,entName,null,null,judgeAuth).size()==0){
                    adminPunishMapper.insert(adminPunish);
                }

            }
        }


        webClient.close();
        log.info("结束处理四川统计局-失信企业信息");
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
        scrapyData.setSource("四川省统计局");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
