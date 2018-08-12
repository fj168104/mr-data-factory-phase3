package com.mr.modules.api.site.instance.colligationsite.haikwansite.lanzhou;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.common.OCRUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：兰州海关知识产权行政处罚
 * url:http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/index.html
 * ----http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/5a44e505-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_lanzhou_zscq")
public class HaiKuan_LanZhou_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan{
    // 通过关键字keyWord=add 来标识增量处理
    @Autowired
    OCRUtil ocrUtil;
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "兰州海关知识产权行政处罚";
        String area = "lanzhou";
        String baseUrl = "http://lanzhou.customs.gov.cn";
        String url = "http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        //搜集页面基本信息
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);

        // 实现runnable借口，创建多线程并启动
        /*new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }) {
        }.start();*/
        //提取结构化数据
        return null;
    }
    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
    //提取结构化数据
    @Override
    public void extractDocData(Map<String,String> map){

        String text = "";
        try {
            text =ocrUtil.getTextFromDocAutoFilePath(map.get("filePath"),map.get("attachmentName"));
        } catch (Exception e) {
            log.error("从附件doc中提取附件出现异常，请检查···"+e.getMessage());
        }
        //实体标识 计数
        int entityCount = 0;
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(map.get("sourceUrl"));
        adminPunish.setPublishDate(map.get("publishDate"));
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("兰州海关知识产权行政处罚");
        adminPunish.setSource("兰州海关");
        adminPunish.setPunishReason(text);
        text = text.replaceAll(" ","");
        text = text.replaceAll("\\n","，");
        text = text.replaceAll("[，]+","，");

        String[] textArr = text.split("，");
        for(String str : textArr){
            if(str.contains("罚字")){
                adminPunish.setJudgeNo(str);
            }
            if(str.contains("当事人")){
                String entityName = str.replace("当事人","");
                if(entityName.length()<6){
                    adminPunish.setObjectType("01");
                    adminPunish.setPersonName(entityName);
                    entityCount++;
                }
                if(entityName.length()>6&&entityCount<1){
                    adminPunish.setObjectType("02");
                    adminPunish.setEnterpriseName(entityName);
                }

            }
            if(str.contains("身份证号")){
                adminPunish.setPersonId(str.replace("身份证号",""));
            }
        }
        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }


}
