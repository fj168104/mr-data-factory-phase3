package com.mr.modules.api.site.instance.colligationsite.haikwansite.lasa;

import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：拉萨海关知识产权行政处罚
 * url:http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613445/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_lasa_zscq")
public class HaiKuan_LaSa_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan{
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "拉萨海关知识产权行政处罚";
        String area = "lasa";//区域为：拉萨
        String baseUrl = "http://lasa.customs.gov.cn";
        String url = "http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613445/index.html";
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

    //提取结构化数据
    @Override
    public void extractWebData(Map<String,String> map){
        //实体标识 计数
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl"));
        adminPunish.setPublishDate(map.get("publishDate"));
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("拉萨海关知识产权行政处罚");
        adminPunish.setSource("拉萨海关");
        String text = map.get("text");

        text = text.replace("：营业执照","营业执照：");
        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("：([\\s])+","：");
        text = text.replace("字 [","字[");
        text = text.replace("] 第 ","]第");
        text = text.replace("第 ","]第");
        text = text.replace(" 号","号");
        text = text.replaceAll("([\\s])+","，");
        text = text.replace("。","，");
        text = text.replaceAll("[，]+","，");
        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中国人民共和国拉萨海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&strArr[0].contains("当事人")&&!strArr[0].contains("发布主题")&&(adminPunish.getEnterpriseName()==null||"".equals(adminPunish.getPersonName()))){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&strArr[0].contains("当事人")&&!strArr[0].contains("发布主题")&&(adminPunish.getPersonName()==null||"".equals(adminPunish.getPersonName()))){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&strArr[0].contains("证件")){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    adminPunish.setPersonName(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("发布主题")&&strArr[1].contains("行政处罚决定书")&&strArr[1].contains("海关关于")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("海关关于.*","海关"));
                }
                if(strArr.length>=2&&strArr[0].contains("发布主题")&&strArr[1].contains("行政处罚决定书")&&!strArr[1].contains("海关关于")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("行政处罚决定书.*",""));
                }
            }
            if(str.contains("知字")&&str.contains("号")){
                adminPunish.setJudgeNo(str);
            }


        }
        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }

}
