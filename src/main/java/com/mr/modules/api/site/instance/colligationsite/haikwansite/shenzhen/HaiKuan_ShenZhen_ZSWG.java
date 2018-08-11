package com.mr.modules.api.site.instance.colligationsite.haikwansite.shenzhen;

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
 * 主题：深圳海关走私违规行政处罚
 * url:http://shenzhen.customs.gov.cn/shenzhen_customs/511686/511713/511715/511717/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shenzhen_zswg")
public class HaiKuan_ShenZhen_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "深圳海关走私违规行政处罚";
        String area = "shenzhen";//区域为：深圳
        String baseUrl = "http://shenzhen.customs.gov.cn";
        String url = "http://shenzhen.customs.gov.cn/shenzhen_customs/511686/511713/511715/511717/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        List<Map<String,String>> listMap = webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        for(Map map : listMap){
            if("".equals(map.get("attachmentName"))){
                extractWebData(map.get("sourceUrl").toString(),map.get("publishDate").toString(),map.get("text").toString());
            }
        }
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
    //提取结构化数据
    public void extractWebData(String sourceUrl,String publishDate,String text){
        //实体标识 计数
        int entityCount = 0;
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("深圳海关走私违规行政处罚");
        adminPunish.setSource("深圳海关");

        text = text.replace("罚决定书 ","罚决定书");
        text = text.replace("（行政处罚决定书）","行政处罚决定书");
        text = text.replace("： 营业执照\\/","营业执照：");
        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("([\\s])+：([\\s])+","：");
        text = text.replace("。","，");
        text = text.replace("(","（");
        text = text.replace(")","）");
        text = text.replace("字 [","字[");
        text = text.replace("] 第 ","]第");
        text = text.replace("第 ","]第");
        text = text.replace(" 号","号");
        text = text.replaceAll("当[\\s]+事[\\s]+人","当事人");

        text = text.replaceAll("([\\s])+","，");
        text = text.replaceAll("[，]+","，");

        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&str.contains("当事人：")&&!strArr[0].contains("发布主题")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&str.contains("当事人：")&&!strArr[0].contains("发布主题")&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("社会信用代码")||strArr[0].contains("营业执照")||strArr[0].contains("企业代码"))){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    adminPunish.setPersonName(strArr[1]);
                }
                if("".equals(adminPunish.getJudgeNo())&&str.contains("发布主题")&&(str.contains("知字")||str.contains("知罚字"))&&str.contains("号")){
                    adminPunish.setJudgeNo((strArr[1].replaceAll(".*处罚决定书","")));

                }
                if("".equals(adminPunish.getEnterpriseName())&&str.contains("发布主题")&&str.contains("关于")&&str.contains("行政处罚决定书")){
                    adminPunish.setEnterpriseName(strArr[1].replaceAll(".*关于","").replaceAll("行政处罚决定书.*",""));
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("关于.*",""));
                }
                if("".equals(adminPunish.getEnterpriseName())&&str.contains("发布主题")&&str.contains("海关行政处罚决定书")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll(".*行政处罚决定书",""));
                }
            }
            if(!str.contains("：")&&str.endsWith("公司")&&adminPunish.getEnterpriseName().equals("")){
                adminPunish.setEnterpriseName(str);
            }
            if(!str.contains("：")&&(str.contains("知字")||str.contains("知罚字"))&&str.contains("号")){
                adminPunish.setJudgeNo(str);
            }


        }
        if(adminPunish.getEnterpriseName().equals("")&&!adminPunish.getPersonName().equals("")){
            adminPunish.setObjectType("01");
        }
        if(!adminPunish.getEnterpriseName().equals("")){
            adminPunish.setObjectType("02");
        }
        adminPunish.setUniqueKey(MD5Util.encode(sourceUrl+adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }
}
