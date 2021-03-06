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
 * 主题：拉萨海关走私违规行政处罚
 * url:http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613446/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Component("haikuan_lasa_zswg")
@Scope("prototype")
public class HaiKuan_LaSa_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan{
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "拉萨海关走私违规行政处罚";
        String area = "lasa";//区域为：拉萨
        String baseUrl = "http://lasa.customs.gov.cn";
        String url = "http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613446/index.html";
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
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl"));
        adminPunish.setPublishDate(map.get("publishDate"));
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("拉萨海关走私违规行政处罚");
        adminPunish.setSource("拉萨海关");
        String text = map.get("text");

        text = text.replace("：营业执照","营业执照：");
        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("：([\\s])+","：");
        text = text.replaceAll("([\\s])+","，");
        text = text.replace("中国居民身份证号码：","中国居民身份证号码");
        text = text.replace("中国居民身份证号码","中国居民身份证号码：");
        text = text.replace("代表人：","代表人");
        text = text.replace("代表人：","代表人：：");
        text = text.replace("当事人姓名/名称：","当事人：");
        text = text.replaceAll("[，]+","，");
        text = text.replace("，","，");
        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&str.contains("当事人：")&&!strArr[0].contains("发布主题")&&(adminPunish.getEnterpriseName()==null||"".equals(adminPunish.getPersonName()))){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&str.contains("当事人：")&&!strArr[0].contains("发布主题")&&(adminPunish.getPersonName()==null||"".equals(adminPunish.getPersonName()))){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("营业执照")||strArr[0].contains("企业代码"))){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    adminPunish.setPersonName(strArr[1]);
                }
                if(strArr.length>=2&&(strArr[0].contains("身份证号码")||strArr[0].contains("身份证号"))){
                    adminPunish.setPersonId(strArr[1]);
                }
                if(strArr[0].contains("发布主题")){
                    adminPunish.setJudgeNo(strArr[1].replaceAll(".*行政处罚决定书",""));
                }
                if(strArr[0].contains("发布主题")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("行政处罚决定书.*",""));
                }
            }
            if(str.contains("知字")&&str.contains("号")&&adminPunish.getJudgeNo().equals("")){
                adminPunish.setJudgeNo(str);
            }
        }

        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }
}
