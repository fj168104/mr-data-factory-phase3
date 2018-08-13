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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 贵州统计局
 * 主题：统计上严重失信企业信息
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 * */
@Slf4j
@Component("bonu_guiZhou")
@Scope("prototype")
public class BONU_GuiZhou extends SiteTaskExtend_CollgationSite {
    @Override
    protected String execute() throws Throwable {
        log.info("开始处理贵州统计局-失信企业信息");
        WebClient webClient = createWebClient(null,null);
        Map pageInfoMap1 = new HashMap();
        pageInfoMap1.put("url","http://www.gzstjj.gov.cn/rdzt/tjssxqygs/sxqyxx/201806/t20180607_3290357.html");
        pageInfoMap1.put("publishDate","2018-06-07");
        Map pageInfoMap2 = new HashMap();
        pageInfoMap2.put("url","http://www.gzstjj.gov.cn/rdzt/tjssxqygs/sxqyxx/201708/t20170825_2798633.html");
        pageInfoMap2.put("publishDate","2017-08-25");
        Map pageInfoMap3 = new HashMap();
        pageInfoMap3.put("url","http://www.gzstjj.gov.cn/rdzt/tjssxqygs/sxqyxx/201708/t20170825_2798630.html");
        pageInfoMap3.put("publishDate","2017-08-25");
        Map pageInfoMap4 = new HashMap();
        pageInfoMap4.put("url","http://www.gzstjj.gov.cn/rdzt/tjssxqygs/sxqyxx/201708/t20170825_2798632.html");
        pageInfoMap4.put("publishDate","2017-08-25");

        List<Map> mapList = new ArrayList();
        mapList.add(pageInfoMap1);
        mapList.add(pageInfoMap2);
        mapList.add(pageInfoMap3);
        mapList.add(pageInfoMap4);
        int i=0;
        for(Map map : mapList){
            String reqUrl = (String)map.get("url");
            String publishDate = (String)map.get("publishDate");
            HtmlPage page = webClient.getPage(reqUrl);
            String title = page.getTitleText();
            List contentDivList = page.getByXPath("//div[@style='font-size:14px;width:920px;']");
            HtmlDivision div = (HtmlDivision)contentDivList.get(0);
            String htmlContent = Jsoup.parse(div.asXml()).html();
            List tabList = div.getElementsByTagName("table");
            HtmlTable table = (HtmlTable)tabList.get(0);
            String textContent = table.getTextContent();
            String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"bonu_guiZhou"+File.separator+ MD5Util.encode(reqUrl);
            String nameTail = publishDate.replace("-","");
            //下载文件
            saveFile(page,title+nameTail+".html",filePath);

            //数据入scrapy_data
            //String url,String hashKey,String html,String text
            insert2ScrapyData(reqUrl,filePath,htmlContent,textContent);

            DomNodeList domNodeList = table.getElementsByTagName("tr");
            String entName = "";
            String code1 = "";
            String judgeAuth = "";
            String frName = "";
            for(int j=0;j<domNodeList.size();j++){
                HtmlTableRow tableRow = (HtmlTableRow)domNodeList.get(j);
                List<HtmlTableCell> cellList = tableRow.getCells();
                if(cellList.get(0).asText().equalsIgnoreCase("行政相对人（企业）名称") || cellList.get(0).asText().equalsIgnoreCase("单位名称")){
                    entName = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("行政相对人统一社会信用代码") || cellList.get(0).asText().equalsIgnoreCase("统一社会信用代码")){
                    code1 = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("承办机关") || cellList.get(0).asText().equalsIgnoreCase("认定机关")){
                    judgeAuth = cellList.get(1).asText();
                }else if(cellList.get(0).asText().equalsIgnoreCase("法定代表人")){
                    frName = cellList.get(1).asText();
                }
            }

            AdminPunish adminPunish = new AdminPunish();
            adminPunish.setSource("贵州省统计局");
            adminPunish.setSubject("统计上严重失信企业信息");
            adminPunish.setUniqueKey(reqUrl+"@"+entName+"@"+(i++)+"@"+publishDate);
            adminPunish.setUrl(reqUrl);
            adminPunish.setEnterpriseCode1(code1);
            adminPunish.setObjectType("01");
            adminPunish.setEnterpriseName(entName);
            adminPunish.setPublishDate(publishDate);
            adminPunish.setJudgeAuth(judgeAuth);
            adminPunish.setPersonName(frName);


            //数据入库
            if(adminPunishMapper.selectByUrl(reqUrl,entName,frName,null,judgeAuth).size()==0){
                adminPunishMapper.insert(adminPunish);
            }
        }


        webClient.close();
        log.info("结束处理贵州统计局-失信企业信息");
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
        scrapyData.setSource("贵州省统计局");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
