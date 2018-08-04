package com.mr.modules.api.site.instance.creditchinasite.anhuisite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.modules.api.model.DiscreditBlacklist;
import com.mr.modules.api.site.SiteTaskExtend_CreditChina;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Auther zjxu to 201806
 * 信用中国（安徽）
 * 提取主题：重大税收违法案件信息 (国税)
 * 提取属性：企业名称 统一社会信用代码 组织机构代码
 */
@Slf4j
@Component("creditchinaanhui_zdsswfaj_gs")
@Scope("prototype")
public class CreditChinaAnHui_ZDSSWFAJ_GS extends SiteTaskExtend_CreditChina{
    @Override
    protected String execute() throws Throwable {
        WebContext();
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    String url = "http://www.creditah.gov.cn/remote/1487/";
    String baseUrl = "http://www.creditah.gov.cn";

    public void WebContext()throws Throwable{
        String ip="",  port="";
        WebClient webClient = createWebClient(ip,port);
        try {
            HtmlPage htmlPage = webClient.getPage(url);
            Document documentInit = Jsoup.parse(htmlPage.asXml());
            Element allPage  = documentInit.getElementsByClass("allPage").get(0);
            int pageSize = Integer.valueOf(allPage.ownText());
            if(pageSize>=1){
                for(int i = 1;i<pageSize;i++){
                    String urlResult = url+"index_"+i+".htm";
                    HtmlPage htmlPage1 = webClient.getPage(urlResult);
                    Document document = Jsoup.parse(htmlPage1.asXml());
                    Element element = document.getElementsByClass("bordered").get(0);
                    Element elementTbody = element.getElementsByTag("tbody").get(0);
                    Elements elementsTr = elementTbody.getElementsByTag("tr");
                    for(Element elementTR:elementsTr){
                        Elements elementsTd = elementTR.getElementsByTag("td");
                        
                        if(elementsTd.size()==3){
                            Map map = new HashMap<>();
                            String detailUrl = baseUrl+elementsTd.get(0).getElementsByTag("div").get(0).getElementsByTag("a").get(0).attr("href");
                            if(elementsTd.get(0).text().trim().length()<6){
                                map.put("objectType","02");
                                map.put("personName",elementsTd.get(2).text());
                                map.put("enterpriseName",elementsTd.get(0).text());
                            }else{
                                map.put("objectType","01");
                                map.put("personName",elementsTd.get(2).text());
                                map.put("enterpriseName",elementsTd.get(0).text());
                            }
                            map.put("enterpriseCode4",elementsTd.get(1).text());
                            map.put("sourceUrl",detailUrl);
                            map.put("source","信用中国（安徽）");
                            map.put("subject","重大税收违法案件信息 (国税)");
                            insertDiscreditBlacklist(map);
                        }
                    }
                }
            }
        }catch (IOException e){
            log.error("访问网络有问题，请检查···异常信息如下"+e.getMessage());
        }
    }
}
