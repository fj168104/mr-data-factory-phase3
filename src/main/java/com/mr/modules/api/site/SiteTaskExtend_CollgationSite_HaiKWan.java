package com.mr.modules.api.site;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.modules.api.mapper.*;
import com.mr.modules.api.model.*;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

/**
 * @auterh zjxu
 * @DateTIme 2018-08
 * 处理海关相关公共方法
 */
@Slf4j
public class SiteTaskExtend_CollgationSite_HaiKWan extends SiteTaskExtend_CollgationSite{
    int sizePage = 1;
    int nextPagesize = 0;//用于判断递归次数
    /**
     * 提取正文
     */
    public List<Map<String,String>> webContext(String increaseFlag,String baseUrl,String url,String ip,String port,String source,String area) {
        //用户存储，filePath，fileName，attachmentType(附件类型),publishDate
        List<Map<String,String>> listMap = new ArrayList<>();
        //获取页数
        WebClient webClient = null;

        try {
            webClient = createWebClient("", "");
            HtmlPage htmlPage = webClient.getPage(url);
            //获取主题列表
            listMap = parseListPage(increaseFlag,htmlPage, ip, port, baseUrl, source,area);
            //进行目标详情页面解析

            //进行翻页操作

        } catch (IOException ioe) {
            log.error("打开页面，连接页面异常···" + ioe.getMessage());
        } catch (Throwable throwable) {
            log.error("创建模拟浏览器异常···" + throwable.getMessage());
        } finally {
            webClient.close();
        }
        return listMap;
    }
    /**
     * 解析主题清单网页
     * @param htmlPage
     * @param ip
     * @param port
     */
    public  List<Map<String,String>> parseListPage(String increaseFlag,HtmlPage htmlPage, String ip, String port,String baseUrl,String source,String area){
        //原文非附件的文本类容
        String text = "";
        //是否要继续执行翻页,:false需要翻页，true：不翻页
        boolean nextPageFlag = true;
        //用于存储，filePath，fileName，attachmentType(附件类型)，publishDate
        List<Map<String,String>> listMap = new ArrayList<>();
        //获取总页数
        nextPagesize = nextPagesize+1;

        HtmlElement sizePageH = (HtmlElement) htmlPage.getByXPath("//div[@class='easysite-total-page']").get(0);
        sizePage = Integer.parseInt(sizePageH.asText().split("\\/")[1]);

        if(nextPagesize<=sizePage){
            //获取列表
            List<HtmlElement> htmlElements = htmlPage.getByXPath("//ul[@class='conList_ul']");
            if(htmlElements.size()>0){
                for(HtmlElement htmlElementList : htmlElements){
                    List<HtmlElement> htmlElementLiList = htmlElementList.getElementsByTagName("li");
                    for(HtmlElement htmlElementLi : htmlElementLiList){
                        //创建Map，用于存储需要返回的对象属性
                        Map<String ,String > mapAttr = new HashMap<>();
                        //获取详情连接
                        HtmlElement htmlElementA = htmlElementLi.getElementsByTagName("a").get(0);
                        String detailUrl = baseUrl+htmlElementA.getAttribute("href");
                        String titleName = htmlElementA.getAttribute("title");
                        //获取发布时间
                        String publishDate = htmlElementLi.getElementsByTagName("span").get(0).asText();
                        log.info("detailUrl:{}\ntitleName:{}\npublishDate:{}",detailUrl,titleName,publishDate);
                        //创建路径
                        String hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);                   //打开附件
                        //附件类型名称
                        String attachmentName = "";
                        //操作详情界面
                        WebClient webClientDetail = null;
                        try {
                            webClientDetail = createWebClient(ip,port);
                            /**
                             * 清单列表中直接管理附件的情况，html页面
                             */
                            if(!detailUrl.contains("html")){
                                //3.创建存储对象
                                ScrapyData scrapyData = new ScrapyData();
                                String[] attachmentTypeStr = htmlElementA.getAttribute("href").split("\\.");
                                String attachmentType = attachmentTypeStr[attachmentTypeStr.length-1];
                                Page page = htmlElementA.click();
                                //下载附件
                                attachmentName = titleName+"."+attachmentType;
                                saveFile(page,attachmentName,hashKeyFilePath);
                                //准备入库操作
                                scrapyData.setHtml("");
                                scrapyData.setText("");
                                scrapyData.setUrl(detailUrl);
                                scrapyData.setCreatedAt(new Date());
                                scrapyData.setSource(source);
                                scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
                                scrapyData.setAttachmentType(attachmentType);
                                scrapyData.setHashKey(hashKeyFilePath);
                                //入库
                                nextPageFlag = saveScrapyDataOne(scrapyData,false);

                            }else{/*清单列表不是直接附件*/
                                HtmlPage htmlPageDetail = webClientDetail.getPage(detailUrl);
                                Map map = parseDetailPage(htmlPageDetail,detailUrl,titleName,publishDate,source,area);
                                attachmentName  = map.get("attachmentName").toString();
                                nextPageFlag = (boolean)map.get("nextPageFlag");
                                text = map.get("text").toString();
                            }
                            if(nextPageFlag&&increaseFlag.equals("add")){
                                break;
                            }

                        } catch (IOException ioe){
                            log.error("网页处理IO异常···"+ioe.getMessage());
                        }catch (Throwable throwable) {
                            log.error("未分类异常···"+throwable.getMessage());
                        }finally {
                            webClientDetail.close();
                        }
                        mapAttr.put("text",text);
                        mapAttr.put("sourceUrl",detailUrl);
                        mapAttr.put("publishDate",publishDate);
                        mapAttr.put("attachmentName",attachmentName);
                        mapAttr.put("filePath",hashKeyFilePath);
                        listMap.add(mapAttr);
                    }

                }
            }
            //递归操作下一页,nextPageFlag = false 执行翻页
            if("all".equals(increaseFlag)||"".equals(increaseFlag)||("add".equals(increaseFlag)&&nextPageFlag==false)){
                htmlPage = nextPage(htmlPage,ip, port,baseUrl);
                if(htmlPage != null){
                    parseListPage(increaseFlag,htmlPage,ip,port,baseUrl,source,area);
                }
            }
        }
        return listMap;
    }

    /**
     * 获取下一页
     * @param htmlPage
     * @param ip
     * @param port
     */
    public HtmlPage nextPage(HtmlPage htmlPage, String ip, String port,String baseUrl){
        //获取下一页
        List<HtmlElement> htmlElements = htmlPage.getByXPath("//div[@class='easysite-page-wrap']//a[@title='下一页']");
        HtmlElement htmlElementNextButton = htmlElements.get(0);
        HtmlPage nextPage = null;
        if(!htmlElementNextButton.getAttribute("tagname").contains("[NEXTPAGE]")){
            WebClient webClientNextPage = null;
            try {
                webClientNextPage = createWebClient(ip,port);
                nextPage = webClientNextPage.getPage(baseUrl+htmlElementNextButton.getAttribute("tagname"));

            } catch (IOException e) {
                log.error("打开下一页网络异常，请查看···"+e.getMessage());
            }catch (Throwable throwable){
                log.error("打开浏览器异常，清查看···"+ throwable.getMessage());
            }
        }
        return nextPage;
    }

    /**
     * 解析详情页面
     * @param htmlPage
     * @param detailUrl
     * @param titleName
     * @param publishDate
     */
    public Map parseDetailPage(HtmlPage htmlPage,String detailUrl,String titleName,String publishDate,String source,String area){
        //原文非附件的文本
        String text  = "";
        //用于存储，attachmentName（附件名称），nextPageFlag（翻页标识）
        Map<String,Object> map = new HashMap();
        boolean nextPageFlag = true;
        String attachmentName = "";
        List<HtmlElement> htmlElements = htmlPage.getByXPath("//div[@class='easysite-news-text']");
        if(htmlElements.size()>0){
            HtmlElement htmlElement = htmlElements.get(0);
            //1.获取详情子页面
            Document htmlTextDoc = Jsoup.parse("<p>发布主题："+titleName+"</p>"+"<p>发布时间："+publishDate+"</p>"+htmlElement.asXml());
            String htmlText = htmlTextDoc.html();
            text  = htmlTextDoc.text();
            //2.获取附件所在的标签
            List<HtmlElement> htmlElementAList = htmlElement.getElementsByTagName("a");
            List<HtmlElement> htmlElementImgList = htmlElement.getElementsByTagName("img");
            //3.创建存储对象
            ScrapyData scrapyData = new ScrapyData();
            if(htmlElementImgList.size()>0){
                for(HtmlElement htmlElementImg : htmlElementImgList){
                    try {
                        String[] attachmentTypeStr = htmlElementImg.getAttribute("src").split("\\.");
                        String attachmentType =attachmentTypeStr[attachmentTypeStr.length-1];
                        //创建路径
                        String hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
                        //下载元素网页
                        saveFile(htmlPage,titleName+".html",hashKeyFilePath);
                        Page page = htmlElementImg.click();
                        //下载附件
                        attachmentName = titleName+"."+attachmentType;
                        saveFile(page,attachmentName,hashKeyFilePath);
                        //准备入库操作
                        scrapyData.setHtml(htmlText);
                        scrapyData.setText(text);
                        scrapyData.setUrl(detailUrl);
                        scrapyData.setCreatedAt(new Date());
                        scrapyData.setSource(source);
                        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
                        scrapyData.setAttachmentType(attachmentType);
                        scrapyData.setHashKey(hashKeyFilePath);
                        //入库
                        nextPageFlag = saveScrapyDataOne(scrapyData,false);
                    } catch (IOException e) {
                        log.error("下载附件出现异常，请查验···"+e.getMessage());
                    }catch (Exception e){
                        log.error("保存附件出现异常，请检验···"+e.getMessage());
                    }
                }
            }else if(htmlElementAList.size()>0){
                for(HtmlElement htmlElementA : htmlElementAList){
                    try {
                        String[] attachmentTypeStr = htmlElementA.getAttribute("href").split("\\.");
                        String attachmentType =attachmentTypeStr[attachmentTypeStr.length-1];
                        //创建路径
                        String hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
                        //下载元素网页
                        saveFile(htmlPage,titleName+".html",hashKeyFilePath);
                        Page page = htmlElementA.click();
                        //下载附件
                        attachmentName = titleName+"."+attachmentType;
                        saveFile(page,attachmentName,hashKeyFilePath);
                        //准备入库操作
                        scrapyData.setHtml(htmlText);
                        scrapyData.setText(text);
                        scrapyData.setUrl(detailUrl);
                        scrapyData.setCreatedAt(new Date());
                        scrapyData.setSource(source);
                        scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
                        scrapyData.setAttachmentType(attachmentType);
                        scrapyData.setHashKey(hashKeyFilePath);
                        //入库
                        nextPageFlag = saveScrapyDataOne(scrapyData,false);
                    } catch (IOException e) {
                        log.error("下载附件出现异常，请查验···"+e.getMessage());
                    }catch (Exception e){
                        log.error("保存附件出现异常，请检验···"+e.getMessage());
                    }
                }
            }else{
                //创建路径
                String hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
                //下载元素网页
                saveFile(htmlPage,titleName+".html",hashKeyFilePath);
                //准备入库操作
                scrapyData.setHtml(htmlText);
                scrapyData.setText(text);
                scrapyData.setUrl(detailUrl);
                scrapyData.setCreatedAt(new Date());
                scrapyData.setSource(source);
                scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
                scrapyData.setAttachmentType("");
                scrapyData.setHashKey(hashKeyFilePath);
                //入库
                nextPageFlag = saveScrapyDataOne(scrapyData,false);
            }
        }
        map.put("text",text);
        map.put("attachmentName",attachmentName);
        map.put("nextPageFlag",nextPageFlag);
        return map;
    }
}
