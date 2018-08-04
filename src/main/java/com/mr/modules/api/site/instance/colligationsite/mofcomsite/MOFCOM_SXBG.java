package com.mr.modules.api.site.instance.colligationsite.mofcomsite;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.ExecutorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;

import javax.xml.ws.Action;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 站点：国家商务部网站
 * url：http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/
 * 主题：失信曝光
 * 属性：企业名称 检查产品  检查结果 检查机关  发布日期
 * 提取：TODO 唯一标识（unique_key）、来源、地址、标题、公司名称 发布日期  检查机关,文号
 * 注：目前没有存储标题的属性
 */
@Slf4j
@Component("mofcom_sxbg")
@Scope("prototype")
public class MOFCOM_SXBG extends SiteTaskExtend_CollgationSite{
    @Autowired
    ExecutorConfig executorConfig ;
    @Override
    protected String execute() throws Throwable {
        webContext();
       return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    //源文件存储的跟路径
    String fileBasePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"mofcomsite"+File.separator;
    //数据来源
    String source = "国家商务部网站";
    //要提取的字段
    String fields = "source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title";
    //唯一标识 注：一般为，title/JubgeNo+enterpriseName+publishdate/punishdate
    String unique_key = "";
    public void webContext(){
        String baseUrl = "http://www.ipraction.gov.cn";
        String urlNext = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/?2";
        //String urln = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/?"+n;
        //第一页
        log.info("**********************************第 {} 页*******************************",1);
        try {
            htmlParse(baseUrl,urlNext);
        } catch (Throwable throwable) {
            log.error("请查阅错误信息···"+throwable.getMessage());
        }
        //第2页  到 第351页
        for(int n=90;n<352;n++){//并发处理
            int m = n;
            log.info("**********************************第 {} 页*******************************",n);
            executorConfig.asyncServiceExecutor().execute(new Runnable() {
                String urln = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/?"+m;
                @Override
                public void run() {

                    try {
                        htmlParse(baseUrl,urln);
                    } catch (Throwable throwable) {
                        log.error("请查阅错误信息···"+throwable.getMessage());
                    }
                }
            });

        }



    }
    @Async(value="asyncServiceExecutor")
    public void htmlParse(String baseUrl ,String resultUrl)throws Throwable{
        log.info("******************************************************当前线程为："+Thread.currentThread().getName());
        WebClient webClient = createWebClient("","");

        //repeats 从试次数
        boolean repeatFlag = true;
        int repeatTime = 0;
        while(repeatTime<5&&repeatFlag){
            try {
                HtmlPage htmlPage = null;
                try {
                     htmlPage = webClient.getPage(resultUrl);
                } catch (Exception e) {
                    repeatTime = repeatTime+1;
                    log.error("发生IO处理异常，请检查···"+e.getMessage());
                    log.info("程序重试中···重试次数为5,第{}次···重试",repeatTime);
                    Thread.sleep(6000);
                    repeatFlag =true;
                    if(repeatTime==5){
                        break;
                    }

                }

                List<HtmlElement> htmlElementList = htmlPage.getByXPath("//body//section[@class='blank']//div[@class='column_01']//section[@class='clearfix mt20p messageCon']//article[@class='mainL fl']//div[@class='newsList']//ul[@class='newsList01']//li");
                for(HtmlElement htmlElement :htmlElementList){
                    if(!htmlElement.getAttribute("class").equals("listline")){
                        //创建对象
                        ScrapyData scrapyData = new ScrapyData();
                        HtmlElement htmlElementLi = htmlElement;
                        HtmlElement htmlElementA = htmlElementLi.getElementsByTagName("a").get(0);
                        //目标地址Url
                        String urlDetail = baseUrl+htmlElementA.getAttribute("href");

                        //目标地址 拼上Url MD5加密作为文件存储路径
                        String filePath = fileBasePath+MD5Util.encode(urlDetail);
                        scrapyData.setUrl(urlDetail);
                        scrapyData.setSource(source);
                        scrapyData.setHashKey(filePath);
                        scrapyData.setCreatedAt(new Date());
                        //目标地址主题
                        String urlTitle =  htmlElementA.getAttribute("title");
                        //文章发布时间
                        String publishDate = htmlElementLi.getElementsByTagName("span").get(0).asText();

                        //创建新的浏览器，访问详情界面信息
                        WebClient webClientDetail = createWebClient("","");
                        HtmlPage htmlPageDetail = null;
                        try {
                            htmlPageDetail = webClientDetail.getPage(urlDetail);
                        } catch (Exception e) {
                            repeatTime = repeatTime+1;
                            log.error("发生IO处理异常，请检查···"+e.getMessage());
                            log.info("程序重试中···重试次数为5,第{}次···重试",repeatTime);
                            Thread.sleep(6000);
                            repeatFlag =true;
                            if(repeatTime==5){
                                break;
                            }

                        }
                        try {
                            saveFile(htmlPageDetail,urlTitle+".html",filePath);
                        } catch (Exception e) {
                            log.error("源文件html下载有异常·····"+e.getMessage());
                        }
                        //获取目标HTML 的对应标签模块
                        DomElement imageSrc =  htmlPageDetail.getElementById("zoom");
                        //获取正文模块,拼接上主题与发布时间
                        String textHtml = "<p>发布主题："+urlTitle+"</p>"+
                                "<p>发布时间："+publishDate+"</p>"+
                                imageSrc.asXml();
                        scrapyData.setHtml(Jsoup.parse(textHtml).html());
                        scrapyData.setText("　　发布主题："+urlTitle+"　　\n发布时间："+publishDate+"\n"+imageSrc.asText());
                        scrapyData.setFields(fields);
                        if(imageSrc!=null){

                            DomNodeList<HtmlElement> imageSrcImg =  imageSrc.getElementsByTagName("img");
                            DomNodeList<HtmlElement> imageSrcA =  imageSrc.getElementsByTagName("a");
                            if(imageSrcImg.size()>0&&imageSrcA.size()<1){//图片标签
                                HtmlElement imageSrcUrlDD =  imageSrcImg.get(0);
                                String imageSrcUrl = imageSrcUrlDD.getAttribute("src");
                                String file = imageSrcUrlDD.getAttribute("title");
                                Page page = webClientDetail.getPage(imageSrcUrl);
                                try {
                                    String[] strFile = file.split("\\.");
                                    int maxIndex = strFile.length-1;
                                    String flieName = urlTitle+"."+strFile[maxIndex];
                                    scrapyData.setAttachmentType(strFile[maxIndex]);
                                    saveFile(page,flieName,filePath);
                                } catch (Exception e) {
                                    log.error("图片附件下载有异常·····"+e.getMessage());
                                }finally {
                                    webClientDetail.close();
                                }

                            }else if (imageSrcA.size()>0){//非图片标签
                                HtmlAnchor imageSrcUrlAA =  (HtmlAnchor) imageSrcA.get(0);
                                String file = imageSrcUrlAA.asText();
                                Page page = imageSrcUrlAA.click();
                                try {
                                    String[] strFile = file.split("\\.");
                                    int maxIndex = strFile.length-1;
                                    String flieName = urlTitle+"."+strFile[maxIndex];
                                    scrapyData.setAttachmentType(strFile[maxIndex]);
                                    saveFile(page,flieName,filePath);
                                } catch (Exception e) {
                                    log.error("非图片附件下载有异常·····"+e.getMessage());
                                }finally {
                                    webClientDetail.close();
                                }
                            }else {//TODO 其他情况
                                log.info("此页面{}无附件",urlDetail);
                            }
                        }
                        //入库
                        boolean isFlag = saveScrapyDataOne(scrapyData,false);
                        //如果记录已经在库中存在，就推测遍历
                        if(isFlag){
                            break;
                        }
                    }
                }
                repeatFlag =false;
            } catch (Exception e) {
                repeatTime = repeatTime+1;
                log.error("发生IO处理异常，请检查···"+e.getMessage());
                log.info("程序重试中···重试次数为5,第{}次···重试",repeatTime);
                Thread.sleep(6000);
                repeatFlag =true;
                if(repeatTime==5){
                    break;
                }

            }finally {
                webClient.close();
            }
        }

    }
}
