package com.mr.modules.api.site.instance.colligationsite.haikwansite.haerbin;

import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：哈尔滨海关走私违规行政处罚
 * url:http://harbin.customs.gov.cn/harbin_customs/467898/467921/467923/467925/554b8d78-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_haerbin_zswg")
public class HaiKuan_HaErBin_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String source = "哈尔滨海关";
    private String subject = "哈尔滨海关走私违规行政处罚";
    private String judgeAuth = "哈尔滨海关";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
     //   String source = "哈尔滨海关走私违规行政处罚";
        String area = "harbin";//区域为：哈尔滨
        String baseUrl = "http://harbin.customs.gov.cn";
        String url = "http://harbin.customs.gov.cn/harbin_customs/467898/467921/467923/467925/554b8d78-1.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }


    public void extractWebData(Map<String,String> map){
        parseText(map);
    }

    public void parseText(Map<String,String> map){
        String judgeNo = "";
        String entName = "";
        String personName = "";
        String publishDate = map.get("publishDate");
        String url = map.get("sourceUrl");
        String title = map.get("title");
        String objectType = "01";
        String html = map.get("html");
        Document document = Jsoup.parse(html);
        Element div = document.getElementById("easysiteText");
        Elements tables = div.getElementsByTag("table");
        if(tables.size()==0){
            AdminPunish adminPunish = new AdminPunish();

            Elements pTags = div.getElementsByTag("p");
            for(int i=0;i<pTags.size();i++){
                Element p = pTags.get(i);
                String text = p.text().replace(" ","").replace(":","：");
                if(text.contains("决定书文号：")){
                    judgeNo = text.substring(text.indexOf("：")+1).replace("；","").replace("。","").trim();
                }else if(title.contains("决定书") && title.contains("号") && title.contains("字")){
                    judgeNo = title.substring(title.indexOf("决定书")+3,title.indexOf("号")+1).replace("（","").trim();
                }
                if(text.contains("五、被处罚") || text.contains("五、被出罚")){

                    text = text.substring(text.indexOf("：")+1);
                    if(text.contains("、")){
                        text = text.substring(0,text.lastIndexOf("、"));
                    }

                    if(text.contains("，")){
                        text = text.substring(0,text.indexOf("，"));
                    }
                    text = text.replaceAll("；","").replaceAll("。","").trim();
                    if(text.length()>5 && (text.contains("公司")||text.contains("厂"))){
                        entName = text;
                        adminPunish.setEnterpriseName(entName);
                    }else{
                        personName = text;
                        objectType = "02";
                        adminPunish.setPersonName(personName);

                    }
                }else if(text.contains("当事人：")){
                    text = text.replaceAll(",","，");
                    text = text.substring(text.indexOf("：")+1,text.indexOf("，"));
                    if(text.length()>5 && (text.contains("公司")||text.contains("厂"))){
                        entName = text;
                        adminPunish.setEnterpriseName(entName);
                    }else{
                        personName = text;
                        objectType = "02";
                        adminPunish.setPersonName(personName);

                    }
                }


            }
            String uniquekey = map.get("sourceUrl")+"@"+entName+"@"+personName+"@"+publishDate;
            adminPunish.setSource(source);
            adminPunish.setSubject(subject);
            adminPunish.setUniqueKey(uniquekey);
            adminPunish.setUrl(url);
            adminPunish.setObjectType(objectType);
            adminPunish.setJudgeAuth(judgeAuth);
            adminPunish.setPublishDate(publishDate);
            adminPunish.setJudgeNo(judgeNo);

            //数据入库
            if(adminPunishMapper.selectByUrl(url,null,null,null,judgeAuth).size()==0){
                adminPunishMapper.insert(adminPunish);
            }

        }else{
            Elements trs = tables.get(0).getElementsByTag("tr");
            for(int k=1;k<trs.size();k++){
                Elements tds = trs.get(k).getElementsByTag("td");
                entName = tds.get(1).text().substring(0,tds.get(1).text().indexOf("公司")+2);
                judgeNo = tds.get(2).text();

                AdminPunish adminPunish = new AdminPunish();

                String uniquekey = map.get("sourceUrl")+"@"+entName+"@"+personName+"@"+publishDate;
                adminPunish.setSource(source);
                adminPunish.setSubject(subject);
                adminPunish.setUniqueKey(uniquekey);
                adminPunish.setUrl(url);
                adminPunish.setObjectType(objectType);
                adminPunish.setJudgeAuth(judgeAuth);
                adminPunish.setPublishDate(publishDate);
                adminPunish.setJudgeNo(judgeNo);
                adminPunish.setEnterpriseName(entName);
                //数据入库
                if(adminPunishMapper.selectByUrl(url,entName,null,judgeNo,judgeAuth).size()==0){
                    adminPunishMapper.insert(adminPunish);
                }

            }
        }
    }
}
