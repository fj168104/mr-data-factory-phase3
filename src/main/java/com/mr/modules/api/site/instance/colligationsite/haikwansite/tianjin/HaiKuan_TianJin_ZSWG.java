package com.mr.modules.api.site.instance.colligationsite.haikwansite.tianjin;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：天津海关走私违规行政处罚
 * url:http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427913/b6fd3207-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_tianjin_zswg")
public class HaiKuan_TianJin_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {

    private String source = "天津海关";
    private String subject = "天津海关走私违规行政处罚";
    private String judgeAuth = "天津海关";

    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";

        String area = "tianjin";//区域为：天津
        String baseUrl = "http://tianjin.customs.gov.cn";
        String url = "http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427920/2fa9bddc-1.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    public void extractImgData(Map<String, String> map) {
        log.info("img parse>>>");
        String sourceUrl = map.get("sourceUrl");
        String filePath = map.get("filePath");
        String publishDate = map.get("publishDate");
        String attachmentName = map.get("attachmentName");
        String titleText = map.get("text");
        String bodyText = "";
        try {
            bodyText = BaiduOCRUtil.getTextStrFromImageFile(filePath + File.separator + attachmentName);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String text = titleText + " " + bodyText;
        extractData(sourceUrl, publishDate, text);
    }

    private void extractData(String sourceUrl, String publishDate, String text) {
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject(subject);
        adminPunish.setSource(source);

        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国天津海关");

        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("([\\s])+：([\\s])+","：");
        text = text.replace("。","，");
        text = text.replace(",","，");
        text = text.replace("(","（");
        text = text.replace(")","）");
        text = text.replace("全业代码","企业代码");
        text = text.replace("字 [","字[");
        text = text.replace("] 第 ","]第");
        text = text.replace("第 ","]第");
        text = text.replace(" 号","号");
        text = text.replace("当事人名称","当事人");
        text = text.replace("当事人姓名/名称","当事人");
        text = text.replaceAll("地[\\s]+址","地址");
        text = text.replaceAll("当[\\s]+事[\\s]+人","当事人");
        text = text.replaceAll("([\\s])+","，");
        text = text.replaceAll("[，]+","，");
        text = text.replace(";","，");
        text = text.replace("：，","：");
        text = text.replace("，：","：");
        text = text.replace(":","：");
        text = text.replace("，事人","，当事人");
        text = text.replace("，当事、人：","，当事人：");
        text = text.replace("，当导人：","，当事人：");
        text = text.replace("，当手人：","，当事人：");
        text = text.replace("当事，","当事人：");
        text = text.replace("当事人，","当事人：");
        text = text.replace("当人：","当事人：");
        text = text.replace("当半人，","当事人：");
        text = text.replace("当申人：","当事人：");
        text = text.replace("当事入：","当事人：");
        text = text.replace("当写人：","当事人：");
        text = text.replace("称：","当事人：");
        text = text.replace("法定代表人为","法定代表人：");

        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国天津海关");
        String lastStr = "";
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&& strArr[0].contains("当事人")&& StrUtil.isEmpty(adminPunish.getEnterpriseName())){
                    if(strArr[1].contains("公司")){
                        adminPunish.setEnterpriseName(strArr[1].substring(0, strArr[1].indexOf("公司") + 2));
                    }else {
                        adminPunish.setEnterpriseName(strArr[1]);
                    }

                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&& strArr[0].contains("当事人")&&StrUtil.isEmpty(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&(strArr[0].contains("证件号码")||strArr[0].contains("信用代码")||strArr[0].contains("营业执照"))){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    if(StrUtil.isEmpty(adminPunish.getEnterpriseName())){
                        adminPunish.setEnterpriseName(lastStr);
                        adminPunish.setObjectType("01");
                    }
                    adminPunish.setPersonName(strArr[1].replace("海关编码","")
                            .replace("企业代码","")
                            .replace("罚款人民币","")
                            .replace("罚款人民","")
                            .replace("海关注册编码","")
                            .replace("（","")
                            .replace("）",""));
                }
                if (strArr.length >= 2 && StrUtil.isEmpty(adminPunish.getJudgeNo()) && strArr[0].contains("发布主题")){
                    if(strArr[1].contains("号")){
                        adminPunish.setJudgeNo(strArr[1].substring(0, strArr[1].indexOf("号") + 1));
                    }
                }
            }

            if (StrUtil.isEmpty(adminPunish.getEnterpriseName())
                    && !str.contains("：")
                    && (str.trim().startsWith("当事人") && str.trim().endsWith("公司"))) {
                adminPunish.setEnterpriseName(str.replaceAll("\\s*", "").replace("当事人", ""));
                adminPunish.setObjectType("01");
            }

            if(str.contains("公司")){
                lastStr = str;
            }

        }

        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }

}
