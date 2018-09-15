package com.mr.modules.api.site.instance.colligationsite.ncmssite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.mr.common.OCRUtil;
import com.mr.common.util.ExcelUtil;
import com.mr.common.util.JsonUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.mofcomsite.NLP_Ner_API;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 站点：全国建筑市场监管公共服务平台
 * url：http://jzsc.mohurd.gov.cn/asite/jsbpp/jsp/news_list.jsp
 * 主题：部级处罚通报
 * 属性：索引号、主体信息、发文单位、生成日期、文件名称、有效期、文号、主题词、被处罚人姓名/企业名、法定代表人、地址、违规事实、相关条令、处罚内容、处罚日期、处罚单位、处罚单位联系方式、发布日期
 * 提取：被处罚人姓名/企业名 公布日期、执法单位,文号
 *
 */
@Slf4j
@Component("ncms_bjcftb")
@Scope("prototype")
public class NCMS_BJCFTB extends SiteTaskExtend_CollgationSite{
    @Autowired
    OCRUtil ocrUtil;
    @Autowired
    SiteParams siteParams;
    //目标地址
     String baseUrl = "http://www.mohurd.gov.cn";
     String url = "http://jzsc.mohurd.gov.cn/asite/jsbpp/jsp/news_list.jsp?data-callback=cftbRefreshMoreLink&data-contentid=bjcftabcontent&class=formsubmit&data-url=news_list.jsp&item_code=jsbpp_news_cftb";

    @Override
    protected String execute() throws Throwable {
        //判断是增量还是全量
        String increaseFlag = siteParams.map.get("increaseFlag");
        try {
            webContext(increaseFlag);
        }catch (Exception e){
            log.error("全国建筑市场监管公共服务平台提取异常，亲查看···"+e.getMessage());
        }

        return null;
    }

    public  void webContext(String increaseFlag){
        //翻页标识
        boolean nextPageFlag = true;
        //翻页序号
        int pageIndex = 2;
        try {
            //第一页
            HtmlPage htmlPage = createWebListClient("","").getPage(url);
            nextPageDetailList(htmlPage);

            List divList = htmlPage.getByXPath("//a[@class='nxt']");
            int total = Integer.parseInt(((HtmlAnchor)divList.get(0)).getAttribute("dt"));
            //翻页

            while(nextPageFlag){
                htmlPage = nextPage(htmlPage,pageIndex);
                //判断数据是否已经存储在库中
                boolean insertFlag = nextPageDetailList(htmlPage);
                pageIndex ++;
                //退出遍历页面
                if(pageIndex>total||(increaseFlag.equals("add")&&insertFlag==true)){
                    break;
                }
            }
        } catch (Throwable throwable) {
            log.error("提取数据异常···请检查！"+throwable.getMessage());
        }

    }


    /**
     * 进行下一页操作
     * @param htmlPage
     * @param pageIndex
     * @return
     * @throws Throwable
     */
    public  HtmlPage nextPage(HtmlPage htmlPage,int pageIndex) throws  Throwable{
        log.info("***************第 {} 页**************",pageIndex);
        //翻页
            List<HtmlElement> htmlElementNextPageList =  htmlPage.getByXPath("//div[@class='quotes']//a");
            if(htmlElementNextPageList.size()>0){
                htmlPage = htmlElementNextPageList.get(pageIndex).click();
                String hrefValue = "javascript:jQuery(\"a[sf=pagebar]\").each(function() {\n" +
                        "        _pangbar_(this)\n" +
                        "    });";
                ScriptResult s = htmlPage.executeJavaScript(hrefValue);//执行js方法
                htmlPage=(HtmlPage) s.getNewPage();//获得执行后的新page对象
            }


        return htmlPage;

    }
    /**
     * 获取页面详情列表
     * @param htmlPage
     */
    public  boolean nextPageDetailList(HtmlPage htmlPage) throws  Throwable{
        //判断数据是否已经存储在库中
        boolean insertFlag = false;
        //获取类别清单
        List<HtmlElement> htmlElementLiList =  htmlPage.getByXPath("//ul[@class='news_group_list']//li");
        for(HtmlElement htmlElementLi : htmlElementLiList){
            List<HtmlElement> htmlElementDiv = htmlElementLi.getElementsByTagName("div");
            if(htmlElementDiv.size()==2){
                //获取详情链接
                //获取文号
                String detailUrl = htmlElementDiv.get(0).getElementsByTagName("a").get(0).getAttribute("data-exturl");
                String detailTitle = htmlElementDiv.get(0).getElementsByTagName("a").get(0).getAttribute("title");
                //获取发布时间
                String publishDate = htmlElementDiv.get(1).asText().split(" ")[0];
                log.info("获取详情链接:{}********获取文号:{}********获取发布时间:{}",detailUrl,detailTitle,publishDate);
                //解析详情，提取结构化信息
                insertFlag = detailPage(detailUrl,publishDate,detailTitle);
            }
        }
        return insertFlag;
    }
    /**
     * 获取详情
     * @param urlDetail
     * @param publishDate
     * @param title
     * @return
     * @throws Throwable
     */
    public  boolean detailPage(String  urlDetail,String publishDate,String title) throws  Throwable{
        //判断数据是否已经存储在库中
        boolean insertFlag = false;
        Map<String,String> mapScrapy = new HashMap<>();

        HtmlPage htmlPage = createWebDetailClient("","").getPage(urlDetail);
        // TODO 保存源页面

        List<HtmlElement> trList = htmlPage.getByXPath("/html/body/table/tbody/tr[2]/td/table[2]");
        //获取附件信息
        //附件类型
        String strAttType = "";
        String strAttUrl = "";
        List<HtmlElement> trListA = htmlPage.getByXPath("//td[class='attlink']//a");
        if(trListA.size()>0){
            strAttType = trListA.get(0).getAttribute("href").split("\\.")[1];
            //附件地址
            strAttUrl = baseUrl + trListA.get(0).getAttribute("href");
        }
        //获取正文，存储为HTML
        String htmlText = "";
        //获取正文，存储为Text
        String text = "";
        if(trList.size()==1){
            String htmlSub = "<p>发布主题："+title+"</p>\n"+"<p>发布时间："+publishDate+"</p>\n"+trList.get(0).asXml();
            Document htmlDoc = Jsoup.parse(htmlSub);
            htmlText = htmlDoc.html();
            text = htmlDoc.text();
            String hashKey = OCRUtil.DOWNLOAD_DIR+ File.separator+"ncmssite"+File.separator+ MD5Util.encode(urlDetail);
            //TODO 如果附件存在，需要下载附件

            // TODO 存储在scrapyData表中
            //source	数据来源
            mapScrapy.put("source","全国建筑市场监管公共服务平台-部级");
            //url	地址
            mapScrapy.put("sourceUrl",urlDetail);
            //hash_key	url的md5结果（如有附件，则保存在此目录中）
            mapScrapy.put("hashKey", hashKey);
            //attachment_type	附件类型（pdf,doc,xls,jpg,tiff...）
            mapScrapy.put("attachmentType",strAttType);
            //html	正文html
            mapScrapy.put("html",htmlText);
            //text	正文text，提取到的正文
            mapScrapy.put("text",text);
            //fields	提取到的关键数据
            mapScrapy.put("fields","source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
            insertFlag = scrapyDataInsert(mapScrapy);

            if(!strAttType.equals("")){
                Page page = createWebClient("","").getPage(strAttUrl);
                saveFile(page,title+"."+strAttType,hashKey);
                if(strAttType.contains("doc")){
                    text = text + ocrUtil.getTextFromDoc(hashKey+File.separator+title+"."+strAttType);
                }
                if(strAttType.contains("xls")){
                    text = text + ExcelUtil.textExtractXls(hashKey+File.separator+title+"."+strAttType);
                }
            }
            try {
                Map<String,Object> nlpTextMap = new HashMap<>();
                nlpTextMap.put("text",text.replaceAll("联[\\s]{0,}系[\\s]{0,}人：.*联系电话","").replace("公司","公司，"));
                JsonNode jsonNode = JsonUtil.getJson(new NLP_Ner_API().nerAPI(nlpTextMap));
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
                // TODO 结构化入库Admin_punish表中
                //文号
                String judgeNo = "";
                Pattern pattern = Pattern.compile("[\\s]{0,}[注,撤,建,督,罚,字,市,行]{1,}[〔]{0,}[\\[]{0,}[0-9]{1,}[\\]]{0,}[〕]{0,}[0-9]{1,}[号][\\s]{0,}");
                Matcher matcher = pattern.matcher(text);
                if(matcher.find()){
                    judgeNo =matcher.group().replaceAll("[\\s]{1,}","");
                }
                //处罚机关
                String judgeAuth = "";
                int judgeAuthLength = 0;
                for(JsonNode enterpriseName : organization ){
                    if(!enterpriseName.textValue().contains("公司")&&(enterpriseName.textValue().contains("厅")||enterpriseName.textValue().contains("委员会")||enterpriseName.textValue().contains("监督")||enterpriseName.textValue().contains("局")||enterpriseName.textValue().contains("部")||enterpriseName.textValue().contains("管理"))){
                        if(enterpriseName.textValue().length()>judgeAuthLength){
                            judgeAuth =  enterpriseName.textValue();
                            judgeAuthLength =enterpriseName.textValue().length();
                         }
                    }
                }

                for(JsonNode personName : person ){
                    Map<String,String> mapPunish = new HashMap<>();
                    if(personName.textValue().length()>1){
                        mapPunish.put("source","全国建筑市场监管公共服务平台");
                        mapPunish.put("subject","部级处罚");
                        mapPunish.put("sourceUrl",urlDetail);
                        mapPunish.put("enterpriseName",personName.textValue());
                        mapPunish.put("publishDate",publishDate);
                        mapPunish.put("judgeNo",judgeNo);
                        mapPunish.put("judgeAuth",judgeAuth);
                        mapPunish.put("punishReason",text);
                        insertFlag = adminPunishInsert(mapPunish);
                    }
                }
                for(JsonNode enterpriseName : organization ){
                    if((enterpriseName.textValue().length()>3&&!enterpriseName.textValue().contains("厅")&&!enterpriseName.textValue().contains("委员会")&&!enterpriseName.textValue().contains("监督")&&!enterpriseName.textValue().contains("局")&&!enterpriseName.textValue().contains("部")&&!enterpriseName.textValue().contains("管理"))||enterpriseName.textValue().contains("公司")){
                        Map<String,String> mapPunish = new HashMap<>();
                        mapPunish.put("source","全国建筑市场监管公共服务平台");
                        mapPunish.put("subject","部级处罚");
                        mapPunish.put("sourceUrl",urlDetail);
                        mapPunish.put("enterpriseName",enterpriseName.textValue());
                        mapPunish.put("publishDate",publishDate);
                        mapPunish.put("judgeNo",judgeNo);
                        mapPunish.put("judgeAuth",judgeAuth);
                        mapPunish.put("punishReason",text);
                        insertFlag = adminPunishInsert(mapPunish);
                    }
                }

            } catch (Exception e) {
                log.info("实体对象提取过程中发生异常，请检查···"+e.getMessage());
            }


        }
        return  insertFlag;

    }

    public static WebClient createWebListClient(String ip, String port) throws Throwable{
        WebClient wc =  null;
        if ("".equals(ip) || "".equals(port)||ip==null||port==null) {
            wc = new WebClient(BrowserVersion.CHROME);
            log.info("通过本地ip进行处理···");
        } else {
            //获取代理对象
            wc = new WebClient(BrowserVersion.CHROME, ip,Integer.valueOf(port));
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
        //等待js时间
        //wc.waitForBackgroundJavaScript(10000);
        //设置Ajax异步处理控制器即启用Ajax支持
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //设置超时时间
        //wc.getOptions().setTimeout(20000);
        //不跟踪抓取
        wc.getOptions().setDoNotTrackEnabled(false);
        //启动客户端重定向
        wc.getOptions().setRedirectEnabled(true);
        //
        wc.getCookieManager().clearCookies();
        //
        wc.setRefreshHandler(new ImmediateRefreshHandler());
        return wc;
    }


    public static WebClient createWebDetailClient(String ip, String port) throws Throwable{
        WebClient wc =  null;
        if ("".equals(ip) || "".equals(port)||ip==null||port==null) {
            wc = new WebClient(BrowserVersion.CHROME);
            log.info("通过本地ip进行处理···");
        } else {
            //获取代理对象
            wc = new WebClient(BrowserVersion.CHROME, ip,Integer.valueOf(port));
            log.info("通过代理进行处理···");
        }

        //设置浏览器版本
        //是否使用不安全的SSL
        wc.getOptions().setUseInsecureSSL(true);
        //启用JS解释器，默认为true
        wc.getOptions().setJavaScriptEnabled(false);
        //禁用CSS TODO HTMLUNIT 本来就没有界面所以静止 false为不启用
        wc.getOptions().setCssEnabled(false);
        //js运行错误时，是否抛出异常 false:为不启用
        wc.getOptions().setThrowExceptionOnScriptError(false);
        //状态码错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //是否允许使用ActiveX
        wc.getOptions().setActiveXNative(false);
        //等待js时间
        //wc.waitForBackgroundJavaScript(10000);
        //设置Ajax异步处理控制器即启用Ajax支持
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //设置超时时间
        //wc.getOptions().setTimeout(20000);
        //不跟踪抓取
        wc.getOptions().setDoNotTrackEnabled(false);
        //启动客户端重定向
        wc.getOptions().setRedirectEnabled(true);
        //
        wc.getCookieManager().clearCookies();
        //
        wc.setRefreshHandler(new ImmediateRefreshHandler());
        return wc;
    }

}
