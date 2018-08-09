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
        List<Map<String,String>> listMap = webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        for(Map map : listMap){
            extractDocData(map.get("sourceUrl").toString(),map.get("publishDate").toString(),map.get("text").toString());
        }
        return null;
    }
    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    //提取结构化数据
    public void extractDocData(String sourceUrl,String publishDate,String text){
        //实体标识 计数
        int entityCount = 0;
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("拉萨海关知识产权行政处罚");
        adminPunish.setSource("拉萨海关");
        adminPunish.setPunishReason(text);
        text = text.replaceAll(" ","");
        text = text.replaceAll("\\n","，");
        text = text.replaceAll("。","，");
        String[] textArr = text.split("，");
        for(String str : textArr){
            log.info("-----------------"+str);
        }
        adminPunish.setUniqueKey(MD5Util.encode(sourceUrl+adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        /*saveAdminPunishOne(adminPunish,false);*/

    }

}
