package com.mr.modules.api.site.instance.colligationsite.haikwansite.shantou;

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
 * 主题：汕头海关知识产权行政处罚
 * url:http://shantou.customs.gov.cn/shantou_customs/596193/596226/596228/596229/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shantou_zscq")
public class HaiKuan_ShanTou_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {

    private String source = "汕头海关";
    private String subject = "汕头海关知识产权行政处罚";

    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String area = "shantou";//区域为：汕头
        String baseUrl = "http://shantou.customs.gov.cn";
        String url = "http://shantou.customs.gov.cn/shantou_customs/596193/596226/596228/596229/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    //提取结构化数据
    public void extractWebData(Map<String,String> map) {

        String sourceUrl =  map.get("sourceUrl");
        String publishDate = map.get("publishDate");
        String text = map.get("text");

        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject(subject);
        adminPunish.setSource(source);


        text = text.replace("： 营业执照\\/","营业执照：");
        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("([\\s])+：([\\s])+","：");
        text = text.replace("。","，");
        text = text.replace("(","（");
        text = text.replace(")","）");
        text = text.replace("字 [","字[");
        text = text.replace(" 号","号");
        text = text.replace("当事人名称","当事人");
        text = text.replaceAll("([\\s])+","，");
        text = text.replaceAll("[，]+","，");
        text = text.replace("：，","：");
        text = text.replace("，年","年");
        text = text.replace("，月","月");
        text = text.replace("，日","日");
        text = text.replace("PAW，PATROL","PAW PATROL");

        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("汕头海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&str.contains("当事人：")&&!strArr[0].contains("发布主题")&&"".equals(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
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

            }

        }

        adminPunish.setUniqueKey(MD5Util.encode(sourceUrl+adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        adminPunish.setObjectType("01");
        saveAdminPunishOne(adminPunish,false);

    }
}
