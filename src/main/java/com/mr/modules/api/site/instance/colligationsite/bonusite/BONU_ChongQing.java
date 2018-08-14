package com.mr.modules.api.site.instance.colligationsite.bonusite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.swing.event.DocumentEvent;
import java.io.File;
import java.util.List;

/***
 * 重庆统计局
 * 主题：统计上严重失信企业信息
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 * */
@Slf4j
@Component("bonu_chongQing")
@Scope("prototype")
public class BONU_ChongQing extends SiteTaskExtend_CollgationSite {
    @Override
    protected String execute() throws Throwable {
        log.info("开始处理重庆统计局-失信企业信息");
        String reqUrl = "http://www.cqtj.gov.cn/tjsxgs/sxqyxx/201712/t20171211_446491.htm";
        String str = new String(new RestTemplate().getForObject(reqUrl, byte[].class), "UTF-8");
        str = str.replace("地 址：","地址：");
        Document document = Jsoup.parse(str);
        Elements pTags = document.getElementsByTag("P");

        String publishDate = "2017-12-11";
        String source = "重庆市统计局";
        Elements divs = document.getElementsByClass("fl article");
        Element divContent =  divs.get(0);
        String textContent = divContent.text();
        Element contentElement = document.getElementById("content");
        String html = Jsoup.parse(contentElement.html()).html();
        String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"bonu_chongQing"+File.separator+ MD5Util.encode(reqUrl);
        //下载文件
        String title = "2017年统计失信企业公示信息";
        //下载文件
        FileUtil.writeString(str,filePath+File.separator+title+".html","UTF-8");
        //数据入scrapy_data
        //String url,String hashKey,String html,String text

        insert2ScrapyData(reqUrl,filePath,html,textContent);
        String entName = "";
        String address = "";
        String frName = "";
        String according = "";
        String result = "";
        for(int i=0;i<pTags.size();i++){
            String text = pTags.get(i).text();
            if(text.contains("企业名称：")){
                entName = text.split("：")[1];
            }
            if(text.contains("地址：")){
                address = text.split("：")[1];
            }
            if(text.contains("法定代表人：")){
                frName = text.split("：")[1];
            }
            if(text.contains("统计违法行为：")){
                according = text.split("：")[1];
            }
            if(text.contains("依法处理情况：")){
                result = text.split("：")[1];
                System.out.println(entName+"---"+address+"---"+frName+"---"+according+"---"+result);
                AdminPunish adminPunish = new AdminPunish();
                adminPunish.setSource(source);
                adminPunish.setSubject("统计上严重失信企业信息");
                adminPunish.setUniqueKey(reqUrl+"@"+entName+"@"+i+"@"+publishDate);
                adminPunish.setUrl(reqUrl);
                adminPunish.setObjectType("01");
                adminPunish.setEnterpriseName(entName);
                adminPunish.setPunishAccording(according);
                adminPunish.setPunishResult(result);
                adminPunish.setPersonName(frName);
                adminPunish.setPublishDate(publishDate);
                adminPunish.setJudgeAuth("重庆市统计局");
                //数据入库
                if(adminPunishMapper.selectByUrl(reqUrl,entName,null,null,"重庆市统计局").size()==0){
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }

        log.info("结束处理重庆统计局-失信企业信息");
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
        scrapyData.setSource("重庆市统计局");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
}
