package com.mr.modules.api.site.instance.boissite;

import com.mr.modules.api.model.FinanceMonitorPunish;
import com.mr.modules.api.site.SiteTaskExtend;
import com.mr.modules.api.site.SiteTaskExtendSub;
import com.mr.modules.api.site.instance.boissite.util.ParseJiLin;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Component("bois_jilin")
@Scope("prototype")
public class SiteTaskImpl_BOIS_JiLin extends SiteTaskExtendSub {
    /**
     * 获取：全量、增量
     * 通过发布时间：yyyy-mm-dd格式进行增量处理
     * 注：请求参数：publishDate
     */
    @Override
    protected String execute() throws Throwable {
//        String url = "http://anhui.circ.gov.cn/web/site11/tab3388/module8940/page1.htm";
                List<String> urlList = extractPageUrlList();
                for(String urlResult : urlList){
                    log.info("urlResult:"+urlResult);
                    Map map = extractContent(getData(urlResult));
                    try{
                        getObj(map,urlResult);
                    }catch (Exception e){
                        writeBizErrorLog(urlResult,"请检查此条url："+"\n"+e.getMessage());
                        continue;
                    }
                }
                return null;
            }
            /**
             * 获取：单笔
             * 注：请求参数传入：url
             */
            @Override
            protected String executeOne() throws Throwable {
                if(oneFinanceMonitorPunish.getUrl()!=null){
                    log.info("oneUrl:"+oneFinanceMonitorPunish.getUrl());
                    Map map = extractContent(getData(oneFinanceMonitorPunish.getUrl()));

            try{
                getObj(map,oneFinanceMonitorPunish.getUrl());
            }catch (Exception e){
                writeBizErrorLog(oneFinanceMonitorPunish.getUrl(),"请检查此条url："+"\n"+e.getMessage());
            }
        }
        if(oneFinanceMonitorPunish.getPublishDate()!=null){
            List<String> urlList = extractPageUrlListAdd(oneFinanceMonitorPunish.getPublishDate());
            for(String urlResult : urlList){
                log.info("urlResult:"+urlResult);
                Map map = extractContent(getData(urlResult));
                try{
                    getObj(map,urlResult);
                }catch (Exception e){
                    writeBizErrorLog(urlResult,"请检查此条url："+"\n"+e.getMessage());
                    continue;
                }
            }
        }
        return null;
    }

    /**  xtractPageAll,URl集合
     * @return*/

    public List extractPageUrlList(){
        List<String> urlList = new ArrayList<>();
        //第一个页面，用于获取总页数
        String baseUrl = "http://jilin.circ.gov.cn/web/site23/tab3593/module9911/page1.htm";
        //解析第一个页面，获取这个页面上下文
        String fullTxt = getData(baseUrl);
        //获取页数
        int  pageAll= extractPage(fullTxt);
        ok:for(int i=1;i<=pageAll;i++){
            String url ="http://jilin.circ.gov.cn/web/site23/tab3593/module9911/page"+i+".htm";
            String resultTxt = getData(url);
            Document doc = Jsoup.parse(resultTxt);
            Elements elementsHerf = doc.getElementsByClass("hui14");
            for(Element element : elementsHerf){
                Element elementUrl = element.getElementById("hui1").getElementsByTag("A").get(0);
                String resultUrl = "http://jilin.circ.gov.cn"+elementUrl.attr("href");
                log.info("编号："+i+"==resultUrl:"+resultUrl);
                if(Objects.isNull(financeMonitorPunishMapper.selectByUrl(resultUrl))){
                    urlList.add(resultUrl);
                }else{
                    break ok;
                }
            }
        }
        return urlList;
    }
    /**  xtractPageAll,URl集合
     * @return*/

    public List extractPageUrlListAdd(String date)throws Throwable{
        List<String> urlList = new ArrayList<>();
        //第一个页面，用于获取总页数
        String baseUrl = "http://jilin.circ.gov.cn/web/site23/tab3593/module9911/page1.htm";
        //解析第一个页面，获取这个页面上下文
        String fullTxt = getData(baseUrl);
        //获取页数
        int  pageAll= extractPage(fullTxt);
        ok:for(int i=1;i<=pageAll;i++){
            String url ="http://jilin.circ.gov.cn/web/site23/tab3593/module9911/page"+i+".htm";
            String resultTxt = getData(url);
            Document doc = Jsoup.parse(resultTxt);
            Elements elementsHerf = doc.getElementsByClass("hui14");
            for(Element element : elementsHerf){
                //发布时间
                Element element_td = element.nextElementSibling();
                String extract_Date = "20" + element_td.text().replace("(","").replace(")","");
                if(new SimpleDateFormat("yyyy-MM-dd").parse(extract_Date).compareTo(new SimpleDateFormat("yyyy-MM-dd").parse(date))>=0){
                    Element elementUrl = element.getElementById("hui1").getElementsByTag("A").get(0);
                    String resultUrl = "http://jilin.circ.gov.cn"+elementUrl.attr("href");
                    log.info("编号："+i+"==resultUrl:"+resultUrl);
                    if(Objects.isNull(financeMonitorPunishMapper.selectByUrl(resultUrl))){
                        urlList.add(resultUrl);
                    }else{
                        break ok;
                    }
                }

            }
        }
        return urlList;
    }

    /** 获取保监会处罚列表所有页数
     * @param fullTxt
     * @return*/

    public int extractPage(String fullTxt){
        int pageAll = 1;
        Document doc = Jsoup.parse(fullTxt);
        Elements td = doc.getElementsByClass("Normal");
        //记录元素的数量
        int serialNo = td.size();
        pageAll = Integer.valueOf(td.get(serialNo-1).text().split("/")[1]);
        log.info("-------------********---------------");
        log.info("处罚列表清单总页数为："+pageAll);
        log.info("-------------********---------------");
        return  pageAll;
    }

    public Map extractContent(String fullTxt) {
        //发布机构
        String publishOrg = "中国保监会吉林保监局行政处";
        //发布时间
        String publishDate = "";
        //TODO 处罚机关（由于有些页面没有，所以暂且给予默认值）
        String punishOrg = "中国保监会吉林监管局";
        //TODO 处罚时间
        String punishDate = "";
        //TODO 处罚文号
        String punishNo = "";
        //TODO 受处罚机构
        String punishToOrg = "";
        //TODO 受处罚机构地址
        String punishToOrgAddress = "";
        //TODO 法定代表人或主要负责人
        String punishToOrgHolder = "";
        //TODO 受处罚当时人名称（自然人）
        StringBuffer priPerson = new StringBuffer();
        //TODO 受处罚当时人证件号码（自然人）
        StringBuffer priPersonCert = new StringBuffer();
        //TODO 受处罚当时人职位（自然人）
        StringBuffer priJob = new StringBuffer();
        //TODO 受处罚当时人地址（自然人）
        StringBuffer priAddress = new StringBuffer();
        //TODO 判断处罚的是法人，还是自然人
        String priBusiType = "";
        String stringDetail = "";
        //数据来源  TODO 来源（全国中小企业股转系统、地方证监局、保监会、上交所、深交所、证监会）
        String source = "保监局";
        //主题 TODO 主题（全国中小企业股转系统-监管公告、行政处罚决定、公司监管、债券监管、交易监管、上市公司处罚与处分记录、中介机构处罚与处分记录
        String object = "行政处罚决定";
        String titleStr = "";

        Map resMap = new ParseJiLin().parseInfo(fullTxt);
        publishDate = (String) resMap.get("publishDate");
        punishDate = (String) resMap.get("punishDate");
        punishNo = (String) resMap.get("punishNo");
        punishToOrg = (String) resMap.get("punishToOrg");
        punishToOrgAddress = (String) resMap.get("punishToOrgAddress");
        punishToOrgHolder = (String) resMap.get("punishToOrgHolder");
        priPerson = (StringBuffer) resMap.get("priPerson");
        priPersonCert = (StringBuffer) resMap.get("priPersonCert");
        priJob = (StringBuffer) resMap.get("priJob");
        priAddress = (StringBuffer) resMap.get("priAddress");
        stringDetail = (String) resMap.get("stringDetail");
        titleStr = (String) resMap.get("titleStr");

        /*log.info("发布主题：" + titleStr);
        log.info("发布机构：" + publishOrg);
        log.info("发布时间：" + publishDate);
        log.info("处罚机关：" + punishOrg);
        log.info("处罚时间：" + punishDate);
        log.info("处罚文号：" + punishNo);
        log.info("受处罚机构：" + punishToOrg);
        log.info("受处罚机构地址：" + punishToOrgAddress);
        log.info("受处罚机构负责人：" + punishToOrgHolder);
        log.info("受处罚人：" + priPerson);
        log.info("受处罚人证件：" + priPersonCert);
        log.info("受处罚人职位：" + priJob);
        log.info("受处罚人地址：" + priAddress);
        log.info("来源："+source);
        log.info("主题："+object);
        log.info("正文：" + stringDetail);*/

        Map<String,String> map = new HashMap<String,String>();
        map.put("titleStr",titleStr);
        map.put("publishOrg",publishOrg);
        map.put("publishDate",publishDate);
        map.put("punishOrg",punishOrg);
        map.put("punishDate",punishDate);
        map.put("punishNo",punishNo);
        map.put("punishToOrg",punishToOrg);
        map.put("punishToOrgAddress",punishToOrgAddress);
        map.put("punishToOrgHolder",punishToOrgHolder);
        map.put("priPerson",priPerson.toString());
        map.put("priPersonCert",priPersonCert.toString());
        map.put("priJob",priJob.toString());
        map.put("priAddress",priAddress.toString());
        map.put("source",source);
        map.put("object",object);
        map.put("stringDetail",stringDetail);

        return map;
    }
    /**
     * 获取Obj,并入库
     * */
    public FinanceMonitorPunish getObj(Map<String,String> mapInfo, String href){

        FinanceMonitorPunish financeMonitorPunish = new FinanceMonitorPunish();
        financeMonitorPunish.setPunishNo(mapInfo.get("punishNo"));//处罚文号
        financeMonitorPunish.setPunishTitle(mapInfo.get("titleStr"));//标题
        financeMonitorPunish.setPublisher(mapInfo.get("publishOrg"));//发布机构
        financeMonitorPunish.setPublishDate(mapInfo.get("publishDate"));//发布时间
        financeMonitorPunish.setPunishInstitution(mapInfo.get("punishOrg"));//处罚机关
        financeMonitorPunish.setPunishDate(mapInfo.get("punishDate"));//处罚时间
        financeMonitorPunish.setPartyInstitution(delFinallyString(mapInfo.get("punishToOrg"),"，").replace("（"," ").replace("）"," "));//当事人（公司）=处罚对象
        financeMonitorPunish.setCompanyFullName(delFinallyString(mapInfo.get("punishToOrg"),"，").replace("（"," ").replace("）"," "));//公司全称
        financeMonitorPunish.setDomicile(delFinallyString(mapInfo.get("punishToOrgAddress"),"，"));//机构住址
        financeMonitorPunish.setLegalRepresentative(delFinallyString(mapInfo.get("punishToOrgHolder"),"，"));//机构负责人
        financeMonitorPunish.setPartyPerson(delFinallyString(mapInfo.get("priPerson"),"，"));//受处罚人
        financeMonitorPunish.setPartyPersonId(delFinallyString(mapInfo.get("priPersonCert"),"，"));//受处罚人证件号码
        financeMonitorPunish.setPartyPersonTitle(delFinallyString(mapInfo.get("priJob"),"，"));//职务
        financeMonitorPunish.setPartyPersonDomi(delFinallyString(mapInfo.get("priAddress"),"，"));//自然人住址
        financeMonitorPunish.setDetails(mapInfo.get("stringDetail"));//详情
        financeMonitorPunish.setUrl(href);
        financeMonitorPunish.setSource(mapInfo.get("source"));
        financeMonitorPunish.setObject(mapInfo.get("object"));

        //保存入库
        saveOne(financeMonitorPunish,false);

        return financeMonitorPunish;
    }
}
