package com.mr.modules.api.site.instance.colligationsite.haikwansite.haerbin;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：哈尔滨海关知识产权行政处罚
 * url:http://harbin.customs.gov.cn/harbin_customs/467898/467921/467923/467924/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_haerbin_zscq")
public class HaiKuan_HaErBin_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "哈尔滨海关知识产权行政处罚";
        String area = "harbin";//区域为：哈尔滨
        String baseUrl = "http://harbin.customs.gov.cn";
        String url = "http://harbin.customs.gov.cn/harbin_customs/467898/467921/467923/467924/index.html";
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

    //提取Web结构化数据
    @Override
    public void extractWebData(Map<String,String> map){
        String text = map.get("text");
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl").toString());
        adminPunish.setPublishDate(map.get("publishDate").toString());
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("哈尔滨海关知识产权行政处罚");
        adminPunish.setSource("哈尔滨海关");

        adminPunish.setPunishReason(text.replaceAll("[\\s]{2,}"," "));

        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("[\\s]{1,}：[\\s]{1,}","：");
        text = text.replace("。","，");
        text = text.replace("(","（");
        text = text.replace(")","）");
        text = text.replaceAll("\\][\\s]{1,}","]");

        text = text.replaceAll("当[\\s]{1,}事[\\s]{1,}人","当事人");
        text = text.replace("当事人名称：","当事人：");
        text = text.replace("姓名：","当事人：");
        text = text.replace("当事人姓名/名称：","当事人：");
        text = text.replace("：营业执照","：");
        text = text.replace("编号：","");

        text = text.replaceAll("[，]+","，");
        text = text.replace(";","，");
        text = text.replace("；","，");
        text = text.replace("：，","：");
        text = text.replace("，：","：");
        text = text.replace(":","：");
        text = text.replace("〔","[").replace("〕","]");
        text = text.replace("﹝","[").replace("﹞","]");
        text = text.replace("【","[").replace("】","]");

        //[\u4e00-\u9fa5] TODO 匹配中文 提取文号编号   关缉违字
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+[\\s]{0,}[关,洲][\\s]{0,}[查,郴,机,缉,违,罚,公,处,行,简,易]{0,}[\\s]{0,}[查,郴,机,缉,违,罚,公,处,行,简,易]{0,}[\\s]{0,}[查,郴,机,缉,违,罚,公,处,行,简,易]{0,}[\\s]{0,}[查,郴,机,缉,违,罚,公,处,行,简,易]{0,}[\\s]{0,}[查,郴,机,缉,违,罚,公,处,行,简,易]{0,}[\\s]{0,}[查,郴,机,缉,违,罚,公处,行,简,易]{0,}[\\s]{0,}[字][\\s]{0,}\\[[\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}\\][\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[\\s]{0,}[-]{0,}[0-9]{0,}[\\s]{0,}[号]");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            adminPunish.setJudgeNo(matcher.group().replaceAll("[\\s]{1,}",""));
        }

        text = text.replace("证件号码： 企业编码","营业执照：");
        text = text.replace("证件号码：营业执照，","营业执照：");
        text = text.replace("证件名称、证件号码：","营业执照：");
        text = text.replace("；","，");
        text = text.replace("自然人姓名：","当事人：");
        text = text.replace("被处罚人：","当事人：");
        text = text.replace("被处罚当事人名称：","当事人：");
        text = text.replace("被处罚法人：","当事人：");
        text = text.replace("被处罚单位名称：","当事人：");
        text = text.replace("自然人的姓名：","当事人：");
        text = text.replace("统一社会代码，","统一社会代码：");

        text = text.replaceAll("[：]+[\\s]{1,}","：");
        text = text.replaceAll("[\\s]{1,}","，");
        text = text.replaceAll("[，]{1,}","，");



        String[] textArr = text.split("，");

        adminPunish.setJudgeAuth("中华人民共和国哈尔滨海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&!strArr[0].contains("发布主题")&&str.contains("当事人：")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&!strArr[0].contains("发布主题")&&(str.contains("当事人："))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("社会代码")||strArr[0].contains("社会信用代码")||strArr[0].contains("营业执照"))&&"".equals(adminPunish.getEnterpriseCode1())){
                    adminPunish.setEnterpriseCode1(strArr[1].replaceAll("（.*",""));
                }
                if(strArr.length>=2&&(strArr[0].contains("代表人")||strArr[0].contains("法人代表"))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("身份证号码")&&"".equals(adminPunish.getPersonId())){
                    adminPunish.setPersonId(strArr[1]);
                }
                if(str.contains("发布主题")&&str.contains("海关关于")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("关于.*",""));
                }
            }
        }
        if(adminPunish.getEnterpriseName().equals("")&&!adminPunish.getPersonName().equals("")){
            adminPunish.setObjectType("01");
        }
        if(!adminPunish.getEnterpriseName().equals("")){
            adminPunish.setObjectType("02");
        }

        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }
}
