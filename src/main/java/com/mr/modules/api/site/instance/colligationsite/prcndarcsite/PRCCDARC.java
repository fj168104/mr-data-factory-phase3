package com.mr.modules.api.site.instance.colligationsite.prcndarcsite;


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.common.OCRUtil;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 站点：中华人民共和国家发展改革委员会网站
 * url：http://www.ndrc.gov.cn/xzcf/
 * 属性：行政处罚决定书文号、处罚单位、当事人、处罚事由、处罚结果、处罚日期
 * 主题：价格违法与垄断案件处罚公告
 * 提取：TODO 行政处罚决定书文号、处罚单位、当事人、处罚日期
 */
@Slf4j
@Component("prccdarc")
@Scope("prototype")
public class PRCCDARC extends SiteTaskExtend_CollgationSite {


    @Override
    protected String execute() throws Throwable {
     //   log.info("开始爬取发改委-价格违法与垄断案件处罚公告");
        String url = "http://www.ndrc.gov.cn/xzcf/index.html";
        List<Map> urlList = getAllUrl(url);
        ExecutorService executorService = Executors.newFixedThreadPool(6);

        for(Map map : urlList){

            executorService.execute(new Runnable() {

                WebClient webClient = null;
                @Override
                public void run() {
                    try {
                        String reqUrl = (String)map.get("reqUrl");
                        String title = (String)map.get("title");
                        String publishDate = (String)map.get("publishDate");
                        webClient = createWebClient(null,null);
                        HtmlPage htmlPage = webClient.getPage(reqUrl);
                    //    String title = htmlPage.getTitleText();
                        String filePath = OCRUtil.DOWNLOAD_DIR + File.separator  +"prccdarc"+File.separator+MD5Util.encode(reqUrl);
                        String fileName = title+".html";
                        //保存文件
                        saveFile(htmlPage,fileName,filePath);

                        String wenHao = title.substring(title.lastIndexOf("书")+1,title.length()).trim();
                        wenHao = wenHao.replace("〔", "[").replace("〕", "]");
                        /*log.info("标题： {}",title);
                        log.info("文号：{}",wenHao);*/
                        DomElement zoom = htmlPage.getElementById("zoom");
                        String textContent = zoom.getTextContent();
                     //   log.info("文本内容：{}",textContent);
                        String htmlText = Jsoup.parse(zoom.asXml()).html();
                     //   log.info("正文：{}",htmlText);
                        DomNodeList<HtmlElement> pElements = zoom.getElementsByTagName("P");
                        String partyPerson = "";
                        String judgeDate = "";
                        for(HtmlElement p : pElements){
                            String pInfo = p.getTextContent().replace(":","：");
                            if(pInfo.contains("当事人：")){
                                partyPerson = pInfo.substring(pInfo.indexOf("：")+1);
                            }else if(pInfo.endsWith("日") && pInfo.contains("年") && pInfo.contains("月")){
                                pInfo = pInfo.substring(pInfo.indexOf("年")-4);
                                judgeDate = pInfo;
                            }
                        }
                        /*log.info("处罚对象：{}",partyPerson);
                        log.info("执行日期：{}",judgeDate);*/

                        //数据入库
                        // String url,String hashKey,String html,String text
                        insert2ScrapyData(reqUrl,filePath,htmlText,textContent);
                        //String url,String entName,String judgeNo,String judgeDate,String publishDate
                        insert2AdminPunish(reqUrl,partyPerson,wenHao,judgeDate,publishDate);
                    } catch (Throwable throwable) {
                        log.error("爬取发改委-价格违法与垄断案件处罚公告发生异常");
                        throwable.printStackTrace();
                    }finally {
                        if(webClient!=null){
                            webClient.close();
                        }
                    }
                }
            });
        }
        executorService.shutdown();
     //   log.info("结束爬取发改委-价格违法与垄断案件处罚公告");
        return null;
    }





    /**
     * 获取总页数
     * */
    public int getPageNum(String pageInfo) throws IOException {
        int pageAll = 1;
        Document document = Jsoup.parse(pageInfo);
        Elements li = document.getElementsByClass("L");
        Elements aTags = li.get(0).getElementsByTag("a");
        String aCount = aTags.get(aTags.size()-3).text();
        pageAll = Integer.parseInt(aCount);
        log.info("发改委-价格违法与垄断案件处罚公告总页数为：{}",pageAll);
        return  pageAll;
    }


    /**
     * 获取所有url
     * */
    public List<Map> getAllUrl(String url) throws IOException{
        List<Map> list = new ArrayList();
        String pageInfo = getData(url);
        //获取页数
        int pageNum = getPageNum(pageInfo);
        for(int i=0;i<pageNum;i++){
            String pageUrl = url;
            if(i>0){
                pageUrl = "http://www.ndrc.gov.cn/xzcf/index_"+i+".html";
            }
            pageInfo = getData(pageUrl);
            Document document = Jsoup.parse(pageInfo);
            Elements uls = document.getElementsByClass("list_02 clearfix");
            Elements lis = uls.get(0).getElementsByClass("li");
            for(Element liElement : lis){

                Map<String,String> map = new HashMap();
                Element aTag = liElement.getElementsByTag("a").get(0);
                Element fontTag = liElement.getElementsByTag("font").get(0);
                String title = aTag.text();
                String publishDate = fontTag.text();
                String reqUrl = aTag.attr("href");
                //    urlList.add("http://www.ndrc.gov.cn/xzcf/"+reqUrl.substring(reqUrl.indexOf(".")+2));
                map.put("reqUrl","http://www.ndrc.gov.cn/xzcf/"+reqUrl.substring(reqUrl.indexOf(".")+2));
                map.put("title",title);
                map.put("publishDate",publishDate);
                list.add(map);
            }
        }



        return  list;
    }

    /**
     *
     * 获取页面信息
     * */
    public String getData(String url) {
        String pageInfo = "";
        WebClient webClient = null;
        try {
            webClient = createWebClient(null,null);
            webClient.getOptions().setJavaScriptEnabled(true);
            HtmlPage htmlPage = webClient.getPage(url);
            pageInfo = htmlPage.asXml();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            if(webClient!=null){
                webClient.close();
            }
        }
        return pageInfo;
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
        scrapyData.setSource("发改委");
        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        if(scrapyDataMapper.selectCountByUrl(url)==0){
            scrapyDataMapper.insert(scrapyData);
        }
    }
    /**
     * 将数据insert到admin_punish表
     * */
    public void insert2AdminPunish(String url,String entName,String judgeNo,String judgeDate,String publishDate){
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setSource("发改委");
        adminPunish.setSubject("价格违法与垄断案件处罚公告");
        adminPunish.setUniqueKey(url+"@"+entName+"@"+judgeDate);
        adminPunish.setUrl(url);
        adminPunish.setObjectType("01"); //主体类型: 01-企业 02-个人
        adminPunish.setEnterpriseName(entName);
        adminPunish.setJudgeNo(judgeNo);
        adminPunish.setJudgeDate(judgeDate);
        String judgeAuth = "国家发展改革委办公厅";
        adminPunish.setJudgeAuth(judgeAuth);
        adminPunish.setPublishDate(publishDate);
        if(adminPunishMapper.selectByUrl(url,entName,null,judgeNo,judgeAuth).size()==0){
            adminPunishMapper.insert(adminPunish);
        }

    }
}
