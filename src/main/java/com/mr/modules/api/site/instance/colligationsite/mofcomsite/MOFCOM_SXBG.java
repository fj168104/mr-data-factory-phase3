package com.mr.modules.api.site.instance.colligationsite.mofcomsite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.common.util.ExcelUtil;
import com.mr.common.util.JsonUtil;
import com.mr.common.util.cutImage.ImageCutFromOpenCVUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.ProductionQuality;
import com.mr.modules.api.model.Proxypool;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.ExecutorConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.SystemProperties;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

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
    OCRUtil ocrUtil;
    @Autowired
    ImageCutFromOpenCVUtil imageCutFromOpenCVUtil;
    @Autowired
    SiteParams siteParams;
    @Autowired
    ExecutorConfig executorConfig ;
    @Override
    protected String execute() throws Throwable {
        //判断是增量还是全量
        String increaseFlag = siteParams.map.get("increaseFlag");
        webContext(increaseFlag);
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
    String fields = "url,source,enterprise_name,oper_result,oper_org publish_date";
    //唯一标识 注：一般为，title/JubgeNo+enterpriseName+publishdate/punishdate

    //总页数
    int pageAllSize = 1;
    public void webContext(String increaseFlag) {
        List<Proxypool> proxypoolList = proxypoolMapper.selectProxyPool();
        proxypoolList.clear();
        String baseUrl = "http://www.ipraction.gov.cn";
        String urlNext = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/";
        //退出标识
        boolean breakFlag = false;
        //获取总页码
        pageAllSize = achievePageAll(urlNext,"","");
        //翻页
        for(int n=1;n<pageAllSize;n++){//并发处理
            int m = n;
            log.info("**********************************第 {} 页*******************************",m);
            String urln = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/?"+m;
            try {
                breakFlag =htmlParse(baseUrl,urln,proxypoolList,increaseFlag);
                if(breakFlag==true){
                    break;
                }
            } catch (Throwable throwable) {
                log.error("请查阅错误信息···"+throwable.getMessage());
            }
            /*executorConfig.asyncServiceExecutor().execute(new Runnable() {

                String urln = "http://www.ipraction.gov.cn/article/xxgk/shxy/sxbg/?"+m;

                @Override
                public void run() {
                    log.info("**********************************第 {} 页*******************************",m);
                    try {
                        htmlParse(baseUrl,urln,proxypoolList);
                    } catch (Throwable throwable) {
                        log.error("请查阅错误信息···"+throwable.getMessage());
                    }
                }
            });*/

        }



    }
    //@Async(value="asyncServiceExecutor")
    public boolean htmlParse(String baseUrl ,String resultUrl,List<Proxypool> proxypoolList,String increaseFlag)throws Throwable{
        String ip = "";
        String port = "";

        //结束循环标识
        boolean breakNextPageFlag = false;
        //repeats 从试次数
        boolean repeatFlag = true;
        int repeatTime = 0;
        while(repeatTime<10&&repeatFlag){
            WebClient webClient = createWebClient(ip,port);
            try {
                HtmlPage htmlPage = null;
                try {
                    htmlPage = webClient.getPage(resultUrl);
                } catch (Exception e) {
                    repeatFlag =true;
                    repeatTime = repeatTime+1;
                    log.error("发生IO处理异常，请检查···"+e.getMessage());
                    log.info("程序重试中···重试次数为10,第{}次···重试",repeatTime);
                    Thread.sleep(10000);
                    if(proxypoolList.size()>0){
                        port = proxypoolList.get(0).getIpport();
                        ip = proxypoolList.get(0).getIpaddress();

                        proxypoolList.remove(0);
                    }
                }

                /**
                 * 获取清单列表
                 */
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
                        WebClient webClientDetail = createWebClient(ip,port);
                        HtmlPage htmlPageDetail = null;
                        try {
                            htmlPageDetail = webClientDetail.getPage(urlDetail);
                        } catch (Exception e) {
                            repeatFlag =true;
                            repeatTime = repeatTime+1;
                            log.error("发生IO处理异常，请检查···"+e.getMessage());
                            log.info("程序重试中···重试次数为5,第{}次···重试",repeatTime);
                            Thread.sleep(10000);
                            if(proxypoolList.size()>0){
                                port = proxypoolList.get(0).getIpport();
                                ip = proxypoolList.get(0).getIpaddress();

                                proxypoolList.remove(0);
                            }
                        }
                        try {
                            saveFile(htmlPageDetail,urlTitle+".html",filePath);
                        } catch (Exception e) {
                            repeatFlag =true;
                            log.error("源文件html下载有异常·····"+e.getMessage());
                            continue;
                        }
                        //获取目标HTML 的对应标签模块
                        DomElement imageSrc =  htmlPageDetail.getElementById("zoom");
                        //获取正文模块,拼接上主题与发布时间
                        String textHtml = "<p>发布主题："+urlTitle+"</p>"+
                                "<p>发布时间："+publishDate+"</p>"+
                                imageSrc.asXml();
                        String html = Jsoup.parse(textHtml).html();
                        String text = "　　发布主题："+urlTitle+"　　\n发布时间："+publishDate+"\n"+imageSrc.asText();
                        scrapyData.setHtml(html);

                        scrapyData.setAttachmentType("");
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
                                    //附件后缀名
                                    String stuff = strFile[maxIndex];
                                    String flieName = urlTitle+"."+stuff;
                                    scrapyData.setAttachmentType(stuff);
                                    saveFile(page,flieName,filePath);
                                    /**
                                     * 附件解析处理
                                     */
                                    if(stuff.contains("jpg")||stuff.contains("jpeg")||stuff.contains("png")||stuff.contains("tif")||stuff.contains("bmp")||stuff.contains("gif")){
                                        //表格图片切分处理
                                        log.info("表格图片切分处理，图片附件处理中···"+flieName);
                                        text = text + imageCutFromOpenCVUtil.imgTextExtract(filePath+File.separator,flieName,OCRUtil.DOWNLOAD_DIR+File.separator+"temp"+File.separator);
                                    }else if(stuff.contains("doc")){
                                        text = text+ocrUtil.getTextFromDoc(filePath+File.separator+flieName);
                                    }else if(stuff.contains("xls")){
                                        text = text+ ExcelUtil.textExtractXls(filePath+File.separator+flieName);
                                    }else {
                                        log.info("其他非处理附件···"+flieName);
                                    }
                                    scrapyData.setText(text);
                                } catch (Exception e) {
                                    repeatFlag =true;
                                    log.error("图片附件下载有异常·····"+e.getMessage());
                                    continue;
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
                                    //附件后缀名
                                    String stuff = strFile[maxIndex];
                                    String flieName = urlTitle+"."+stuff;
                                    scrapyData.setAttachmentType(stuff);
                                    saveFile(page,flieName,filePath);

                                    /**
                                     * 附件解析处理
                                     */
                                    if(stuff.contains("jpg")||stuff.contains("jpeg")||stuff.contains("png")||stuff.contains("tif")||stuff.contains("bmp")||stuff.contains("gif")){
                                        //表格图片切分处理
                                        log.info("表格图片切分处理，图片附件处理中···"+flieName);
                                        text = text + imageCutFromOpenCVUtil.imgTextExtract(filePath+File.separator,flieName,OCRUtil.DOWNLOAD_DIR+File.separator+"temp"+File.separator);
                                    }else if(stuff.contains("doc")){
                                        text = text+ocrUtil.getTextFromDoc(filePath+File.separator+flieName);
                                    }else if(stuff.contains("xls")){
                                        text = text+ ExcelUtil.textExtractXls(filePath+File.separator+flieName);
                                    }else {
                                        log.info("其他非处理附件···"+flieName);
                                    }

                                } catch (Exception e) {
                                    repeatFlag =true;
                                    log.error("非图片附件下载有异常·····"+e.getMessage());
                                    continue;
                                }finally {
                                    webClientDetail.close();
                                }

                            }else {//TODO 其他情况
                                log.info("此页面{}无附件",urlDetail);
                            }
                            //用于存储处理详情结构化需要的属性
                            Map<String,String> mapAttr = new HashMap<>();
                            mapAttr.put("text",text);
                            mapAttr.put("html",html);
                            mapAttr.put("publishDate",publishDate);
                            mapAttr.put("sourceUrl",urlDetail);
                            mapAttr.put("title",urlTitle);
                            mapAttr.put("source",source);
                            extractWebNerDetail(mapAttr);
                        }
                        //入库
                        boolean isFlag = saveScrapyDataOne(scrapyData,false);
                        //如果记录已经在库中存在，就退出遍历
                        if(isFlag && "add".equals(increaseFlag)){
                            breakNextPageFlag = true;
                        }
                    }
                }
                repeatFlag =false;
            } catch (Exception e) {
                repeatTime = repeatTime+1;
                log.error("发生IO处理异常，请检查···"+e.getMessage());
                log.info("程序重试中···重试次数为5,第{}次···重试",repeatTime);
                Thread.sleep(10000);
                repeatFlag =true;

                if(proxypoolList.size()>0){
                    port = proxypoolList.get(0).getIpport();
                    ip = proxypoolList.get(0).getIpaddress();

                    proxypoolList.remove(0);
                }


            }finally {
                webClient.close();
            }
        }
        return  breakNextPageFlag;
    }

    /**
     * 通过NLP Ner 提取文本中的实体对象
     * @param mapAttr
     * 备注：实体中包括：处罚机关，处罚对象（公司，个人）
     */
    public  void extractWebNerDetail( Map<String,String> mapAttr) {
        Map<String,Object> mapNer = new HashMap<>();
        mapNer.put("text",mapAttr.get("text").replace("公司","公司，").replace("厂","厂，"));
        try {
            JsonNode jsonNode = JsonUtil.getJson(new NLP_Ner_API().nerAPI(mapNer));
            ArrayNode person = (ArrayNode)jsonNode.get("person");
            ArrayNode organization = (ArrayNode)jsonNode.get("organization");
            ArrayNode location = (ArrayNode)jsonNode.get("location");
            ArrayNode facility = (ArrayNode)jsonNode.get("facility");
            ArrayNode gpe = (ArrayNode)jsonNode.get("gpe");

            log.info("person："+person);
            log.info("organization："+organization);
            log.info("location："+location);
            log.info("facility："+facility);
            log.info("gpe："+gpe);
            //获取检查机关
            String operOrg = "";
            int strOrgLength = 0;
            for(JsonNode  strOrganization : organization){
                String strOrg = strOrganization.asText();
                if(strOrg.contains("食品药品")||strOrg.contains("局")||strOrg.contains("食药")||strOrg.contains("监督")){
                    log.info("**************************{}*********************************{}","正在提取检查机关对象",strOrg);
                    if(strOrgLength<strOrg.length()){
                        operOrg = strOrg;
                    }
                    strOrgLength = strOrg.length();
                }
            }
            /**
             * 提取企业对象
             */
            for( JsonNode  strOrganization : organization){

                String strOrg = strOrganization.asText();
                if(!strOrg.contains("监督")&&!strOrg.contains("食品药品")&&!strOrg.contains("局")&&!strOrg.contains("食药")&&strOrg.length()>5){
                    log.info("**************************{}*********************************{}","正在提取企业对象",strOrg);
                    ProductionQuality productionQuality = new ProductionQuality();
                    productionQuality.setCreatedAt(new Date());
                    productionQuality.setUpdatedAt(new Date());
                    productionQuality.setEnterpriseName(strOrg);
                    productionQuality.setUrl(mapAttr.get("sourceUrl"));
                    productionQuality.setOperResult(mapAttr.get("text"));
                    productionQuality.setOperOrg(operOrg);
                    productionQuality.setSource(mapAttr.get("source"));
                    productionQuality.setPublishDate(mapAttr.get("publishDate"));
                    saveProductionQualityOne(productionQuality,false);
                }
            }
            /**
             * TODO 提取个人对象,这个提取有问题，事物名称（人名称，物体名称）
             */
            /*for( JsonNode  strPerson : person){
                log.info("**************************{}*********************************","正在提取自然人对象");
                ProductionQuality productionQuality = new ProductionQuality();
                String currPerson = strPerson.asText();
                productionQuality.setCreatedAt(new Date());
                productionQuality.setUpdatedAt(new Date());
                productionQuality.setEnterpriseName(currPerson);
                productionQuality.setUrl(mapAttr.get("sourceUrl"));
                productionQuality.setOperResult("text");
                productionQuality.setOperOrg(operOrg);
                productionQuality.setSource("source");
                productionQuality.setPublishDate(mapAttr.get("publishDate"));

                saveProductionQualityOne(productionQuality,false);
            }*/

        } catch (Exception e) {
            log.info("实体对象提取过程中发生异常，请检查···"+e.getMessage());
        }
    }


    /**
     * 获取总页数数
     * @param ip
     * @param port
     * @return
     */
    public int achievePageAll(String resultUrl,String ip, String port) {
        int pageSize = 1;
        WebClient wc =  null;
        if ("".equals(ip) || "".equals(port)||ip==null||port==null) {
            wc = new WebClient(BrowserVersion.FIREFOX_52);
            log.info("通过本地ip进行处理···");
        } else {
            //获取代理对象
            wc = new WebClient(BrowserVersion.FIREFOX_52, ip,Integer.valueOf(port));
            log.info("通过代理进行处理···");
        }

        //设置浏览器版本
        //是否使用不安全的SSL
        wc.getOptions().setUseInsecureSSL(true);
        //启用JS解释器，默认为true
        wc.getOptions().setJavaScriptEnabled(true);
        //禁用CSS TODO HTMLUNIT 本来就没有界面所以静止 false为不启用
        wc.getOptions().setCssEnabled(false);
        //js运行错误时，是否抛出异常 false:为不启用
        wc.getOptions().setThrowExceptionOnScriptError(false);
        //状态码错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //是否允许使用ActiveX
        wc.getOptions().setActiveXNative(false);
        //等待js时间,在执行页面的时候设置
        //wc.waitForBackgroundJavaScript(10000);
        //设置Ajax异步处理控制器即启用Ajax支持
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //设置超时时间
        wc.getOptions().setTimeout(10000);
        //不跟踪抓取
        wc.getOptions().setDoNotTrackEnabled(false);
        //启动客户端重定向
        wc.getOptions().setRedirectEnabled(true);
        //
        wc.getCookieManager().clearCookies();
        //
        wc.setRefreshHandler(new ImmediateRefreshHandler());
        //设置js处理时间为5000（5秒）,在执行页面的时候设置
        // wc.setJavaScriptTimeout(5000);

        HtmlPage htmlPage = null;
        try {
            htmlPage = wc.getPage(resultUrl);
        } catch (Exception e) {
            log.error("发生IO处理异常，请检查···"+e.getMessage());
        }
        /**
         * 获取总页数
         */
        List<HtmlSpan> htmlSpanPage =  htmlPage.getByXPath("//body//section//div//section//article//div//div[@class='messageListpage mt20p']//span");
        pageSize = Integer.valueOf(htmlSpanPage.get(2).asText());
        log.info("************************总共{}页***************************",htmlSpanPage.get(2).asText());
        return pageSize;
    }

}
