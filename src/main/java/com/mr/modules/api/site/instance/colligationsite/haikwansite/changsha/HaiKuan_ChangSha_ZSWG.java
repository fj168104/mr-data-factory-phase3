package com.mr.modules.api.site.instance.colligationsite.haikwansite.changsha;

import com.mr.common.OCRUtil;
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
 * 主题：长沙海关走私违规行政处罚
 * url:http://changsha.customs.gov.cn/changsha_customs/508922/508939/508941/508943/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_changsha_zswg")
public class HaiKuan_ChangSha_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    OCRUtil ocrUtil;
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "长沙海关走私违规行政处罚";
        String area = "changsha";//区域为：长沙
        String baseUrl = "http://changsha.customs.gov.cn";
        String url = "http://changsha.customs.gov.cn/changsha_customs/508922/508939/508941/508943/index.html";
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
        adminPunish.setSubject("长沙海关走私违规行政处罚");
        adminPunish.setSource("长沙海关");

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
        text = text.replace("湘 关 机 缉违字","湘关机缉违字");
        text = text.replace("统一社会信用代码","，统一社会信用代码");
        text = text.replace("统一社会信用代码为","，统一社会信用代码：");
        text = text.replace("海关注册编码","，海关注册编码");
        text = text.replace("衡阳电科 电源有限公司","衡阳电科电源有限公司");
        text = text.replace("长沙瑞良电子 有限公司","长沙瑞良电子有限公司");
        text = text.replace("安石国际贸易 有限公司","安石国际贸易有限公司");

        text = text.replace("91430100 675573106J","91430100675573106J");
        text = text.replace("9 14301 1159547460XH","9143011159547460XH");


        text = text.replaceAll("[，]+","，");
        text = text.replace(";","，");
        text = text.replace("；","，");
        text = text.replace("：，","：");
        text = text.replace("，：","：");
        text = text.replace(":","：");
        text = text.replace("〔","[").replace("〕","]");
        text = text.replace("﹝","[").replace("﹞","]");

        //[\u4e00-\u9fa5] TODO 匹配中文 提取文号编号   关缉违字
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+[\\s]{0,}[关,洲][\\s]{0,}[查,郴,机,缉,违,罚,公,处]{1,}[\\s]{0,}[字][\\s]{0,}\\[[\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}\\][\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[\\s]{0,}[-]{0,}[0-9]{0,}[\\s]{0,}[号]");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            adminPunish.setJudgeNo(matcher.group().replaceAll("[\\s]{1,}",""));
        }

        text = text.replace("证件号码：营业执照，","营业执照：");
        text = text.replaceAll("[：]+[\\s]{1,}","：");
        text = text.replaceAll("[\\s]{1,}","，");
        text = text.replaceAll("[，]{1,}","，");



        String[] textArr = text.split("，");

        adminPunish.setJudgeAuth("中华人民共和国长沙海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&!strArr[0].contains("发布主题")&&str.contains("当事人：")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&!strArr[0].contains("发布主题")&&(str.contains("当事人：")||str.contains("姓名："))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("社会信用代码")||strArr[0].contains("营业执照"))&&"".equals(adminPunish.getEnterpriseCode1())){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&(strArr[0].contains("代表人")||strArr[0].contains("法人代表"))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("身份证号码")&&"".equals(adminPunish.getPersonId())){
                    adminPunish.setPersonId(strArr[1]);
                }

                if(adminPunish.getEnterpriseName().equals("")&&str.contains("发布主题")&&str.contains("公司")&&str.contains("海关关于")){
                    adminPunish.setEnterpriseName(strArr[1].replaceAll(".*关于","").replaceAll("公司.*","公司"));
                }
                if(str.contains("发布主题")&&strArr[1].contains("海关关于")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("关于.*",""));
                }
            }

            if(str.startsWith("当事人")&&str.endsWith("公司")&&adminPunish.getEnterpriseName().equals("")&&!str.contains("：")){
                adminPunish.setEnterpriseName(str.replace("当事人",""));
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

    //提取Doc结构化数据
    @Override
    public void extractDocData(Map<String,String> map){

        String text = "";
        try {
            text = ocrUtil.getTextFromDocAutoFilePath(map.get("filePath"),map.get("attachmentName"));
        } catch (Exception e) {
            log.error("解析Doc文档异常，请检查···"+e.getMessage());
        }
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl").toString());
        adminPunish.setPublishDate(map.get("publishDate").toString());
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("长沙海关走私违规行政处罚");
        adminPunish.setSource("长沙海关");

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
        text = text.replace("湘 关 机 缉违字","湘关机缉违字");
        text = text.replace("统一社会信用代码","，统一社会信用代码");
        text = text.replace("统一社会信用代码为","，统一社会信用代码：");
        text = text.replace("海关注册编码","，海关注册编码");
        text = text.replace("衡阳电科 电源有限公司","衡阳电科电源有限公司");

        text = text.replace("91430100 675573106J","91430100675573106J");
        text = text.replace("9 14301 1159547460XH","9143011159547460XH");


        text = text.replaceAll("[，]+","，");
        text = text.replace(";","，");
        text = text.replace("；","，");
        text = text.replace("：，","：");
        text = text.replace("，：","：");
        text = text.replace(":","：");
        text = text.replace("〔","[").replace("〕","]");
        text = text.replace("﹝","[").replace("﹞","]");

        //[\u4e00-\u9fa5] TODO 匹配中文 提取文号编号   关缉违字
        Pattern pattern = Pattern.compile("[\\u4e00-\\u9fa5]+[\\s]{0,}[关,洲][\\s]{0,}[查,郴,机,缉,违,罚,公,处]{1,}[\\s]{0,}[字][\\s]{0,}\\[[\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}[0-9][\\s]{0,}\\][\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[0-9]{0,}[\\s]{0,}[\\s]{0,}[-]{0,}[0-9]{0,}[\\s]{0,}[号]");
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()){
            adminPunish.setJudgeNo(matcher.group().replaceAll("[\\s]{1,}",""));
        }
        text = text.replace("证件号码：营业执照，","营业执照：");
        text = text.replaceAll("[：]+[\\s]{1,}","：");
        text = text.replaceAll("[\\s]{1,}","，");
        text = text.replaceAll("[，]{1,}","，");



        String[] textArr = text.split("，");

        adminPunish.setJudgeAuth("中华人民共和国长沙海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&!strArr[0].contains("发布主题")&&str.contains("当事人：")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&!strArr[0].contains("发布主题")&&(str.contains("当事人：")||str.contains("姓名："))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("社会信用代码")||strArr[0].contains("营业执照"))&&"".equals(adminPunish.getEnterpriseCode1())){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&(strArr[0].contains("代表人")||strArr[0].contains("法人代表"))&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("身份证号码")&&"".equals(adminPunish.getPersonId())){
                    adminPunish.setPersonId(strArr[1]);
                }

                if(adminPunish.getEnterpriseName().equals("")&&str.contains("发布主题")&&str.contains("公司")&&str.contains("海关关于")){
                    adminPunish.setEnterpriseName(strArr[1].replaceAll(".*关于","").replaceAll("公司.*","公司"));
                }
                if(str.contains("发布主题")&&strArr[1].contains("海关关于")){
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("关于.*",""));
                }
            }

            if(str.startsWith("当事人")&&str.endsWith("公司")&&adminPunish.getEnterpriseName().equals("")&&!str.contains("：")){
                adminPunish.setEnterpriseName(str.replace("当事人",""));
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
