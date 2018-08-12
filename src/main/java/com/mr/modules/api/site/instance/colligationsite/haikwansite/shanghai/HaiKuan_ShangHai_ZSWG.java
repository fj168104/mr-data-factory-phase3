package com.mr.modules.api.site.instance.colligationsite.haikwansite.shanghai;

import com.mr.common.OCRUtil;
import com.mr.common.util.AIOCRUtil;
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
 * 主题：上海海关走私违规行政处罚
 * url:http://shanghai.customs.gov.cn/shanghai_customs/423405/423510/423512/423514/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shanghai_zswg")
public class HaiKuan_ShangHai_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    OCRUtil ocrUtil;
    @Autowired
    AIOCRUtil aiocrUtil;
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "上海海关走私违规行政处罚";
        String area = "shanghai";//区域为：上海
        String baseUrl = "http://shanghai.customs.gov.cn";
        String url = "http://shanghai.customs.gov.cn/shanghai_customs/423405/423510/423512/423514/index.html";
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
    //提取附件为PDF的文本内容，调用ocr识别
    public String extractWebDOCXLSData(String filePath,String fileName){
        String filePathName = filePath+ File.separator+fileName;
        ocrUtil.pdf2image(filePathName);
        String text = AIOCRUtil.getTextFromImageFile(filePathName.replace(".pdf",".png"));
        return text;
    }
    //提取结构化数据
    @Override
    public void extractWebData(Map<String,String> map){
        //实体标识 计数
        String text = map.get("text");
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl").toString());
        adminPunish.setPublishDate(map.get("publishDate").toString());
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("上海海关走私违规行政处罚");
        adminPunish.setSource("上海海关");


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
        text = text.replace("当事人姓名/名称：","当事人： ");
        text = text.replaceAll("当[\\s]+事[\\s]+人","当事人");
        text = text.replaceAll("([\\s])+","，");
        text = text.replaceAll("[，]+","，");
        text = text.replace("当事人：，","当事人：");

        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国上海海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&&strArr[0].contains("当事人")&&!strArr[0].contains("发布主题")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1].replaceAll("（([0-9a-zA-Z]+)+）",""));
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&&strArr[0].contains("当事人")&&!strArr[0].contains("发布主题")&&"".equals(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1].replaceAll("（([0-9a-zA-Z]+)+）",""));
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&(strArr[0].contains("社会信用代码")||strArr[0].contains("营业执照")||strArr[0].contains("企业代码"))){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    adminPunish.setPersonName(strArr[1]);
                }
            }
            if(!str.contains("：")&&(str.contains("知字")||str.contains("罚字"))&&str.contains("号")){
                adminPunish.setJudgeNo(str);
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
