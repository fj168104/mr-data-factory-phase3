package com.mr.modules.api.site.instance.boissite;

import com.mr.modules.api.model.FinanceMonitorPunish;
import com.mr.modules.api.site.SiteTaskExtend;
import com.mr.modules.api.site.SiteTaskExtendSub;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *@ auther :zjxu
 *@ dateTime : 201803
 * 厦门保监局处罚 提取所需要的信息
 * 序号、处罚文号、机构当事人名称、机构当事人住所、机构负责人姓名、
 * 当事人集合（当事人姓名、当事人身份证号、当事人职务、当事人住址）、发布机构、发布日期、行政处罚详情、处罚机关、处罚日期
 */
@Slf4j
@Component("bois_xiamen")
@Scope("prototype")
public class SiteTaskImpl_BOIS_XiaMen extends SiteTaskExtendSub {
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
        // http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page1.htm
        String baseUrl = "http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page1.htm";
        //解析第一个页面，获取这个页面上下文
        String fullTxt = getData(baseUrl);
        //获取页数
        int  pageAll= extractPage(fullTxt);
        ok:for(int i=1;i<=pageAll;i++){
            String url ="http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page"+i+".htm";
            String resultTxt = getData(url);
            Document doc = Jsoup.parse(resultTxt);
            Elements elementsHerf = doc.getElementsByClass("hui14");
            for(Element element : elementsHerf){
                Element elementUrl = element.getElementById("hui1").getElementsByTag("A").get(0);
                String resultUrl = "http://xiamen.circ.gov.cn"+elementUrl.attr("href");
                log.info("编号："+i+"==resultUrl:"+resultUrl);
                if(Objects.isNull(financeMonitorPunishMapper.selectByUrl(resultUrl))){
                    urlList.add(resultUrl);
                }else {
                    break ok;
                }
            }
        }
        return urlList;
    }

    /**  xtractPageAll,增量集合
     * @return*/

    public List extractPageUrlListAdd(String date)throws Throwable{
        List<String> urlList = new ArrayList<>();
        //第一个页面，用于获取总页数
        // http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page1.htm
        String baseUrl = "http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page1.htm";
        //解析第一个页面，获取这个页面上下文
        String fullTxt = getData(baseUrl);
        //获取页数
        int  pageAll= extractPage(fullTxt);
        ok:for(int i=1;i<=pageAll;i++){
            String url ="http://xiamen.circ.gov.cn/web/site36/tab3415/module8941/page"+i+".htm";
            String resultTxt = getData(url);
            Document doc = Jsoup.parse(resultTxt);
            Elements elementsHerf = doc.getElementsByClass("hui14");
            for(Element element : elementsHerf){
                //发布时间
                Element element_td = element.nextElementSibling();
                String extract_Date = "20" + element_td.text().replace("(","").replace(")","");
                if(new SimpleDateFormat("yyyy-MM-dd").parse(extract_Date).compareTo(new SimpleDateFormat("yyyy-MM-dd").parse(date))>=0){
                    Element elementUrl = element.getElementById("hui1").getElementsByTag("A").get(0);
                    String resultUrl = "http://xiamen.circ.gov.cn"+elementUrl.attr("href");
                    log.info("编号："+i+"==resultUrl:"+resultUrl);
                    if(Objects.isNull(financeMonitorPunishMapper.selectByUrl(resultUrl))){
                        urlList.add(resultUrl);
                    }else {
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
        String publishOrg = "中国保监会厦门保监局行政处";
        //发布时间
        String publishDate = "";
        //TODO 处罚机关
        String punishOrg ="厦门保监局";
        //TODO 处罚时间
        String punishDate = "";
        //TODO 处罚文号
        String  punishNo = "";
        //TODO 受处罚机构
        String punishToOrg = "";
        //TODO 受处罚机构地址
        String punishToOrgAddress = "";
        //TODO 法定代表人或主要负责人
        String punishToOrgHolder = "";
        //TODO 受处罚当时人名称（自然人）
        StringBuffer priPerson =  new StringBuffer();
        //TODO 受处罚当时人证件号码（自然人）
        StringBuffer priPersonCert = new StringBuffer();
        //TODO 受处罚当时人职位（自然人）
        StringBuffer priJob = new StringBuffer();
        //TODO 受处罚当时人地址（自然人）
        StringBuffer priAddress = new StringBuffer();
        //TODO 判断处罚的是法人，还是自然人
        String priBusiType = "";
        //数据来源  TODO 来源（全国中小企业股转系统、地方证监局、保监会、上交所、深交所、证监会）
        String source = "保监局";
        //主题 TODO 主题（全国中小企业股转系统-监管公告、行政处罚决定、公司监管、债券监管、交易监管、上市公司处罚与处分记录、中介机构处罚与处分记录
        String object = "行政处罚决定";
        String stringDetail ="";
        Document doc = Jsoup.parse(fullTxt.replaceAll("、","，")
                .replace("(","（")
                .replace(")","）")
                .replace(":","：")
                .replace("&nbsp;","")
                .replace(" ","")
                .replace("姓名：","当事人：")
                .replaceAll("当(.*)事(.*)人：","当事人：")
                .replace("受处罚人：","当事人：")
                .replace("受处理人：","当事人：")
                .replace("受处罚人名称：","当事人：")
                .replace("营业地址：","地址：")
                .replaceAll("住(.*)所：","地址：")
                .replaceAll("住(.*)址：","地址：")
                .replaceAll("地(.*)址：","地址：")
                .replaceAll("职(.*)务：","职务：")
                .replaceAll("时(.*)任：","职务：")
                .replaceAll("负(.*)责(.*)人：","负责人：")
                .replaceAll("主要负责人：","负责人：")
                .replaceAll("法定代表人：","负责人：")
                .replaceAll("主要负责人姓名：","负责人：")
                .replaceAll("身份证号码：","身份证号：")

        );
        //TODO 全文
        Element elementsTxt = doc.getElementById("tab_content");
        Elements elementsTD = elementsTxt.getElementsByTag("TD");
        Elements elementsSpan = elementsTxt.getElementsByClass("xilanwb");
        Elements elementsP = elementsTxt.getElementsByTag("P");
        Elements elementsA = elementsTxt.getElementsByTag("A");
        Elements elementsPre = elementsTxt.getElementsByTag("Pre");

        //TODO 正文
        stringDetail =elementsP.text();
//        log.info("stringDetail:"+stringDetail);
        if(elementsPre.first()!=null && elementsPre.first().text().length()>0){
            stringDetail = elementsPre.first().text()+" "+ stringDetail;
        }

        /*TODO 通用型*/
        //TODO 提取主题
        Element elementsTitle = elementsTD.first();
        String titleStr = elementsTitle.text();
        //TODO 获取包含发布时间的元素
        Element elementsPublishDate = elementsTD.get(1);
        String publishDateStr = elementsPublishDate.text();
        publishDate = publishDateStr.substring(publishDateStr.indexOf("发布时间：")+5,publishDateStr.indexOf("分享到："));
        //TODO 正文中没有文号

        if(elementsSpan.get(0).text().contains("厦保监罚")){
            punishNo=elementsP.get(0).text().replaceAll("　","").trim();
        }else if(titleStr.contains("厦保监") && titleStr.contains("号")){
            /*String[] punishNoStr = titleStr.split("（");
            if(punishNoStr.length==2){
                punishNo ="（"+punishNoStr[1].replaceAll("　","").trim();
            }*/
            punishNo = titleStr.substring(titleStr.indexOf("厦保监"),titleStr.lastIndexOf("号")+1);

        }

        String spantext = textTransfer(elementsSpan.text()).trim();
        if(spantext.lastIndexOf("日")>spantext.lastIndexOf("月") && spantext.lastIndexOf("月")> spantext.lastIndexOf("年")){
            punishDate =spantext.substring(spantext.lastIndexOf("年")-4,spantext.lastIndexOf("日")+1);
        }

        /*TODO 特殊型 只适合没有标明当事人的处罚文案，需要加限制条件*/
        if(stringDetail.indexOf("当事人：")>-1){
            //TODO 默认值
            punishOrg = "厦门保监局";
            List<String> listStr = new ArrayList();
            //TODO 判断是否为法人
            for(Element elementP : elementsP){
                String elementPStr =  elementP.text().replaceAll("　","").trim();
                if(elementP.text().indexOf("：")>-1&&elementP.text().split("：").length>1){
                    listStr.add(elementP.text().replaceAll("　","").trim());
                }
                /*if(elementPStr.indexOf("年")>-1 && elementPStr.indexOf("月")>-1&&elementPStr.indexOf("日")>-1){
                    punishDate = elementPStr.substring(elementPStr.lastIndexOf("年")-4,elementPStr.lastIndexOf("日")+1);
                    //      punishDate = elementPStr.replaceAll(" ","").trim();
                }*/
                if(elementP.text().indexOf("厦保监罚〔")>-1){
                    punishNo = elementP.text().replaceAll(" ","").trim();
                }
            }
            //如果P标签中没有事件，事件在A标签中，需要获取A标签中的时间
           /* for(Element elementA : elementsA){
                String elementAStr =  elementA.text().replaceAll("　","").trim();
                if(elementAStr.indexOf("年")>-1 && elementAStr.indexOf("月")>-1&&elementAStr.indexOf("日")>-1){
                    punishDate = elementAStr.replaceAll(" ","").trim();
                }
            }*/

            //TODO 需要判断是法人还是自然人
            boolean busiPersonFlag = false;
            log.info("listStr:-------"+listStr.toString());
            for(int i=0;i<listStr.size();i++ ){
                String[] currentPersonStr  = listStr.get(i).split("：");

                if(i==0&&currentPersonStr[1].length()>3&&currentPersonStr[0].equals("当事人")&& !currentPersonStr[1].contains("年龄")){
                    busiPersonFlag =true;
                    punishToOrg = currentPersonStr[1];
                }
                // TODO 法人
                if(i==1&&busiPersonFlag==true&&currentPersonStr[0].trim().equals("地址")){
                    punishToOrgAddress = currentPersonStr[1];
                }
                if(i==2&&busiPersonFlag==true&&currentPersonStr[0].trim().equals("地址")){
                    punishToOrgAddress = currentPersonStr[1];
                }
                if(busiPersonFlag==true&&currentPersonStr[0].trim().equals("负责人")){
                    punishToOrgHolder = currentPersonStr[1];
                }
                if(i>1&&busiPersonFlag==true&&currentPersonStr[0].trim().equals("当事人")){
                    priPerson.append(currentPersonStr[1]).append("，");
                }
                if(i>2&&busiPersonFlag==true&&currentPersonStr[0].trim().equals("地址")){
                    priAddress.append(currentPersonStr[1]).append("，");
                }
                if(busiPersonFlag==true&&currentPersonStr[0].trim().equals("身份证号")){
                    priPersonCert.append(currentPersonStr[1]).append("，");
                }
                if(busiPersonFlag==true&&currentPersonStr[0].trim().equals("职务")){
                    priJob.append(currentPersonStr[1]).append("，");
                }
                //TODO 自然人
                if(busiPersonFlag==false&&currentPersonStr[0].trim().equals("当事人")){
                    if(currentPersonStr[1].contains("年龄")){
                        String name = currentPersonStr[1].substring(0,currentPersonStr[1].indexOf("年龄"));
                        priPerson.append(name).append("，");
                    }else{
                        priPerson.append(currentPersonStr[1]).append("，");
                    }
                }
                if(busiPersonFlag==false&&currentPersonStr[0].trim().equals("地址")){

                    priAddress.append(currentPersonStr[1]).append("，");
                }
                if(busiPersonFlag==false&&currentPersonStr[0].trim().equals("身份证号")){
                    if(currentPersonStr[1].contains("地址") && currentPersonStr.length>1){
                        String certNo = currentPersonStr[1].substring(0,currentPersonStr[1].indexOf("地址"));
                        priPersonCert.append(certNo.trim()).append("，");
                        priAddress.append(currentPersonStr[2].trim()).append("，");
                    }else{
                        priPersonCert.append(currentPersonStr[1].trim()).append("，");
                    }

                }
                if(busiPersonFlag==false&&currentPersonStr[0].trim().equals("职务")){
                    priJob.append(currentPersonStr[1]).append("，");
                }
            }
        }else if(stringDetail.contains("告知书")){
          //      log.info("------stringDetail----------"+stringDetail);
                String name = textTransfer(stringDetail.substring(stringDetail.indexOf("告知书")+3,stringDetail.indexOf("：")));
                if(name.length()>5){
                    punishToOrg = name;
                }else{
                    priPerson.append(name).append("，");
                }
        }else{
            String name = textTransfer(stringDetail.substring(0,stringDetail.indexOf("：")));
            if(name.length()>5){
                punishToOrg = name;
            }else{
                priPerson.append(name).append("，");
            }
        }
        punishNo = punishNo.replace("（","").replace("）","");

        if(punishToOrg.contains("以下简称")){
            punishToOrg = punishToOrg.substring(0,punishToOrg.indexOf("以下简称")-1);
        }
        /*log.info("发布主题："+titleStr);
        log.info("发布机构："+publishOrg);
        log.info("发布时间："+publishDate);
        log.info("处罚机关："+punishOrg);
        log.info("处罚时间："+punishDate);
        log.info("处罚文号："+punishNo);
        log.info("受处罚机构："+punishToOrg);
        log.info("受处罚机构地址："+punishToOrgAddress);
        log.info("受处罚机构负责人："+punishToOrgHolder);
        log.info("受处罚人："+priPerson);
        log.info("受处罚人证件："+priPersonCert);
        log.info("受处罚人职位："+priJob);
        log.info("受处罚人地址："+priAddress);
        log.info("正文："+stringDetail);*/

        Map<String,String> map = new HashMap<String,String>();
        map.put("titleStr",textTransfer(titleStr));
        map.put("publishOrg",textTransfer(publishOrg));
        map.put("publishDate",textTransfer(publishDate));
        map.put("punishOrg",textTransfer(punishOrg));
        map.put("punishNo",textTransfer(punishNo));
        map.put("punishToOrg",textTransfer(punishToOrg));
        map.put("companyFullName",textTransfer(punishToOrg));
        map.put("punishToOrgAddress",textTransfer(punishToOrgAddress));
        map.put("punishToOrgHolder",textTransfer(punishToOrgHolder));
        map.put("priPerson",textTransfer(priPerson.toString()));
        map.put("priPersonCert",textTransfer(priPersonCert.toString()));
        map.put("priJob",textTransfer(priJob.toString()));
        map.put("priAddress",textTransfer(priAddress.toString()));
        map.put("source",source);
        map.put("object",object);
        map.put("stringDetail",textTransfer(stringDetail));

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

    private String textTransfer(String text){
        return text.replace((char) 12288, ' ').trim(); //去掉全角空格
    }
}
