package com.mr.modules.api.site.instance.creditchinasite.mainsite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.mapper.AdminPunishMapper;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CreditChina;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * @auther
 * 1.信用中国主站
 * 2.url:http://www.creditchina.gov.cn/xinxigongshi/xinxishuanggongshi/
 * 3.需求：选择“行政处罚”+“全国”
 * 4.提取内容：处罚主体、决定书文号、处罚名称、法定代表人、处罚类别、处罚结果、处罚事由、处罚机关、处罚决定日期、处罚期限、数据更新日期
 * 5.todo 主站只允许访问5页
 */
@Slf4j
@Component("creditchinamainsite0006")
@Scope("prototype")
public class CreditChinaMainSite0006 extends SiteTaskExtend_CreditChina{
    @Autowired
    AdminPunishMapper adminPunishMapper;
    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    @Override
    protected String execute() throws Throwable {
        String keyWord = SiteParams.map.get("keyWord");
        if(keyWord==null){
            keyWord="";
        }
        SiteParams.map.clear();
        extractContext(keyWord);
        return null;
    }
    /**
     * 获取网页内容
     */
    public void extractContext(String keyWord ) throws Throwable{
        result(5,keyWord);;
    }


    /**
     * 获取api接口相应结果清单
     * @param keywordy       关键字
     * @param sizePage  遍历次数
     * @param webClient  HTMLUnit 客户端
     * @return
     */
    public  List<String> publicityInfoSearch(String keywordy ,int sizePage, WebClient webClient) {
        List<String> nameList = new ArrayList<>();
        try {
            //浏览器的中文需要转码  如：URLEncoder.encode("绩溪县汇通汽车运输有限公司", "utf-8");
            String name = URLEncoder.encode(keywordy, "utf-8");

            //获取处罚的列表清单的名称
            String url = "https://www.creditchina.gov.cn/api/publicity_info_search?keyword="+name+"&dataType=0&areaCode=&page="+sizePage+"&pageSize=10";
            WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);
            Map<String, String> additionalHeaders = new HashMap<String, String>();
            additionalHeaders.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36");
            additionalHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
            additionalHeaders.put("Accept", "application/json;charset=UTF-8, text/javascript, */*; q=0.01");
            request.setAdditionalHeaders(additionalHeaders);
            // 获取某网站页面
            Page page = webClient.getPage(request);
            // System.out.println(Page.getWebResponse().getContentAsString());
            ObjectMapper mapper = new ObjectMapper();
            WebResponse response = page.getWebResponse();
            if (response.getContentType().equals("application/json")) {
                String jsonString = response.getContentAsString();
                //log.info("----------------sizePage:"+sizePage+"\n"+jsonString);
                Map mapJson = mapper.readValue(jsonString, Map.class);
                List<Map> listResults = (List)mapJson.get("results");
                for(int i =0;i<listResults.size();i++){
                    Map mapResult = listResults.get(i);

                    String nameResult = (String)mapResult.get("name");
                    nameList.add(nameResult);
                    //log.info("-------dddddd-nameResult--------\n"+nameResult);
                }

                return nameList;
            }else{
                return null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    /**
     * 获取处罚的详情形象
     * @param researchName  要查询的名称
     * @param webClient HTMLUnit客户端
     * @return
     */
    public  List<Map> pubPenaltyName(String researchName, WebClient webClient) {
        List<Map> nameList = new ArrayList<>();
        try {
            //浏览器的中文需要转码  如：URLEncoder.encode("绩溪县汇通汽车运输有限公司", "utf-8");
            String name = URLEncoder.encode(researchName, "utf-8");
            String url = "https://www.creditchina.gov.cn/api/pub_penalty_name?name="+name+"&page=1&pageSize=10";
            WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);
            Map<String, String> additionalHeaders = new HashMap<String, String>();
            additionalHeaders.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36");
            additionalHeaders.put("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
            additionalHeaders.put("Accept", "application/json;charset=UTF-8, text/javascript, */*; q=0.01");
            request.setAdditionalHeaders(additionalHeaders);
            // 获取某网站页面
            Page page = webClient.getPage(request);
            // System.out.println(Page.getWebResponse().getContentAsString());
            ObjectMapper mapper = new ObjectMapper();
            WebResponse response = page.getWebResponse();
            if (response.getContentType().equals("application/json")) {
                String jsonString = response.getContentAsString();
                //log.info("----------------\n"+jsonString);
                Map mapJson = mapper.readValue(jsonString, Map.class);
                Map listResult = (Map)mapJson.get("result");
                List<Map> listResults = (List)listResult.get("results");
                for(int j =0;j<listResults.size();j++){
                    Map mapResult = listResults.get(j);
                    //存储数据源地址
                    mapResult.put("sourceUrl",url);
                    mapResult.put("source","信用中国");
                    mapResult.put("subject","全国行政处罚");
                    mapResult.put("enterpriseName",mapResult.remove("cfXdrMc"));
                    mapResult.put("personName",mapResult.remove("cfFr"));
                    mapResult.put("punishType",mapResult.remove("cfCflb1"));
                    mapResult.put("punishReason",mapResult.remove("cfSy"));
                    mapResult.put("punishAccording",mapResult.remove("cfYj"));
                    mapResult.put("punishResult",mapResult.remove("cfJg"));
                    mapResult.put("judgeNo",mapResult.remove("cfWsh"));
                    mapResult.put("judgeDate",mapResult.remove("cfJdrq"));
                    mapResult.put("judgeAuth",mapResult.remove("cfXzjg"));
                    mapResult.put("publishDate",mapResult.remove("cfSjc"));
                    nameList.add(mapResult);
                }

                return nameList;
            }else{
                return null;
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }
    public void result(int pageSize,String keywordy)throws Throwable{

        List<String> nameList = new ArrayList<>();
        List<Map> punishDetail = new ArrayList<>();
        //1.获取处罚清单
        if(("".equals(keywordy)||keywordy==null)&&pageSize<=0){
            for(int i=1;i<=pageSize;i++){
                nameList.addAll(publicityInfoSearch(keywordy,i,createWebClient("",""))) ;
            }
            log.info("关键字为空或者为null并且页数小于等于零的情况···");
        }
        if(("".equals(keywordy)||keywordy==null)&& pageSize>0){
            for(int i=1;i<=pageSize;i++){
                nameList.addAll(publicityInfoSearch(keywordy,i,createWebClient("",""))) ;
            }

            log.info("关键字为空或者为null并且页数大于零的情况···");
        }
        if(!"".equals(keywordy)&&keywordy!=null){
            nameList =  publicityInfoSearch(keywordy,1,createWebClient("",""));
            log.info("关键字不为空并且不为null并且页数小于零的情况···");
        }

        //2.获取处罚的详情
        //log.info("-----------------------\n"+nameList);
        for(int i=0;i<nameList.size();i++){
            punishDetail.addAll(pubPenaltyName(nameList.get(i),createWebClient("","")));
        }
        //入库
        for(Map map : punishDetail){
            adminPunishInsert(map);
        }
        //log.info("-----------------------\n"+punishDetail);

    }

}
