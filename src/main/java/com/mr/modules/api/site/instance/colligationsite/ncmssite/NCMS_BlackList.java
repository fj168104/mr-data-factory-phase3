package com.mr.modules.api.site.instance.colligationsite.ncmssite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.modules.api.mapper.DiscreditBlacklistMapper;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.DiscreditBlacklist;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 站点：全国建筑市场监管公共服务平台
 * url：http://jzsc.mohurd.gov.cn/asite/credit/record/blackList
 * 主题：黑名单记录
 * 属性：黑名单记录主体、认定部门、列入黑名单日期、移除黑名单日期、黑名单类型、黑名单认定依据、统一社会信用代码、企业法人代表、企业登记注册类型、企业注册地址、企业经营地址、企业资质资格、注册人员、工程项目、变更记录、不良行为、黑名单记录
 * 提取：被处罚人姓名/企业名 公布日期、执法单位,文号
 *
 */
@Slf4j
@Component("ncms_blacklist")
@Scope("prototype")
public class NCMS_BlackList extends SiteTaskExtend_CollgationSite {
    @Autowired
    DiscreditBlacklistMapper discreditBlacklistMapper;
    private String source = "全国建筑市场监管公共服务平台";
    private String subject = "黑名单记录";
    @Override
    protected String execute() throws Throwable {

        log.info("开始处理【全国建筑市场监管公共服务平台-黑名单记录】");
        WebClient webClient = createWebClient(null,null);
        webClient.getOptions().setJavaScriptEnabled(true);
        String mainUrl = "http://jzsc.mohurd.gov.cn/asite/credit/record/blackList";
        HtmlPage mainPage = webClient.getPage(mainUrl);
        int position = 1;
        List divList = mainPage.getByXPath("//a[@class='nxt']");
        int total = Integer.parseInt(((HtmlAnchor)divList.get(0)).getAttribute("dt"));
        HtmlPage currentPage = mainPage;
        String entName = "";
        String judgeNo = "";
        String judgeAuth = "";
        String judgeDate = "";
    //    String title = currentPage.getTitleText();
        String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"ncms_blackList"+File.separator+ MD5Util.encode(mainUrl);
        insert2ScrapyData(mainUrl,filePath,"","");
        for(int i=0;i<total;i++){
            List tbList = currentPage.getByXPath("//tbody[@class='cursorDefault']");
            HtmlTableBody tableBody = (HtmlTableBody)tbList.get(0);
        //    String htmlContent = Jsoup.parse(tableBody.asXml()).html();

            List trList = tableBody.getElementsByTagName("tr");
            for(int k=0;k<trList.size();k++){
                HtmlTableRow tableRow = (HtmlTableRow)trList.get(k);
                List tdList = tableRow.getElementsByTagName("td");
                entName = ((HtmlTableCell)tdList.get(0)).getElementsByTagName("a").get(1).getTextContent().trim();
                judgeNo = ((HtmlTableCell)tdList.get(1)).getTextContent();
                judgeNo = judgeNo.replace("(","（").replace(")","）");
                judgeNo = judgeNo.substring(judgeNo.indexOf("（")+1,judgeNo.indexOf("）"));
                judgeAuth = ((HtmlTableCell)tdList.get(2)).getTextContent();
                judgeDate = ((HtmlTableCell)tdList.get(3)).getTextContent();

                DiscreditBlacklist discreditBlacklist = new DiscreditBlacklist();
                discreditBlacklist.setSource(source);
                discreditBlacklist.setSubject(subject);
                discreditBlacklist.setUniqueKey(mainUrl+"@"+entName+"@"+k+"@"+judgeDate);
                discreditBlacklist.setUrl(mainUrl);
                discreditBlacklist.setObjectType("01");
                discreditBlacklist.setEnterpriseName(entName);
                discreditBlacklist.setJudgeAuth(judgeAuth);
                discreditBlacklist.setJudgeDate(judgeDate);
                discreditBlacklist.setJudgeNo(judgeNo);

                //数据入库
                if(discreditBlacklistMapper.selectByUrl(mainUrl,entName,null,judgeNo,judgeAuth).size()==0){
                    discreditBlacklistMapper.insert(discreditBlacklist);
                }
            }

            List divList2 = currentPage.getByXPath("//div[@class='quotes']");
            HtmlDivision division = (HtmlDivision)divList2.get(0);
            List aTags = division.getElementsByTagName("a");
            for(int p=0;p<aTags.size();p++){
                HtmlAnchor anchor = (HtmlAnchor) aTags.get(p);
                if(anchor.getAttribute("class").equalsIgnoreCase("active")){
                    position = p;
                    break;
                }
            }
            currentPage = ((HtmlAnchor) aTags.get(position+1)).click();
        }
        webClient.close();
        log.info("结束处理【全国建筑市场监管公共服务平台-黑名单记录】");
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
