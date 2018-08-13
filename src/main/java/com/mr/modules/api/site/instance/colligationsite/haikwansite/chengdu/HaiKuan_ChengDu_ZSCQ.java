package com.mr.modules.api.site.instance.colligationsite.haikwansite.chengdu;

import com.mr.framework.core.util.StrUtil;
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
 * 主题：成都海关知识产权行政处罚
 * url:http://chengdu.customs.gov.cn/chengdu_customs/519405/519431/519433/519434/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_chengdu_zscq")
public class HaiKuan_ChengDu_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "成都海关知识产权行政处罚";
        String area = "chengdu";//区域为：成都
        String baseUrl = "http://chengdu.customs.gov.cn";
        String url = "http://chengdu.customs.gov.cn/chengdu_customs/519405/519431/519433/519434/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    public void extractWebData(Map<String, String> map) {
        extractData(map.get("sourceUrl"), map.get("publishDate"), map.get("text"));
    }

    private void extractData(String sourceUrl, String publishDate, String text) {
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("成都海关知识产权行政处罚");
        adminPunish.setSource("成都海关");

        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国成都海关");

        text = text.replace("　", " ");
        text = text.replace(" ", " ");
        text = text.replace("﹝", "〔");
        text = text.replace("﹞", "〕");
        text = text.replaceAll("([\\s])+：([\\s])+", "：");
        text = text.replace("。", "，");
        text = text.replace("(", "（");
        text = text.replace(")", "）");
        text = text.replace("字 [", "字[");
        text = text.replace("] 第 ", "]第");
        text = text.replace("第 ", "]第");
        text = text.replace(" 号", "号");
        text = text.replace("编号：", "");
        text = text.replace("当事人姓名/名称：", "当事人： ");
        text = text.replaceAll("当[\\s]+事[\\s]+人", "当事人");
        text = text.replaceAll("([\\s])+", "，");
        text = text.replaceAll("[，]+", "，");
        text = text.replace("当事人：，", "当事人：");
        text = text.replace("证件名称、证件号码：", "");

        text = text.replace("作出处罚决定海关，", "作出处罚决定海关：");
        text = text.replace("处罚决定书文号，", "处罚决定书文号：");
        text = text.replace("被处罚企业名，", "被处罚企业名：");
        text = text.replace("法定代表人，", "法定代表人：");
        text = text.replace("统一社会信用代码，", "统一社会信用代码：");
        text = text.replace("案件名称，", "案件名称：");
        text = text.replace("〔，", "〔");
        text = text.replace("，〕，", "〕");
        text = text.replace("，〕", "〕");
        text = text.replace("〕，", "〕");
        text = text.replace("，/，", "/");

        if((text.substring(0, text.indexOf("发布时间")).contains("成都海关未涉及到海关知识产权行政处罚案件")
                || text.substring(0, text.indexOf("发布时间")).contains("测试"))){
            return;
        }

        String[] textArr = text.split("，");

        for (String str : textArr) {

            if (str.contains("：")) {
                String[] strArr = str.split("：");
                //当事人
                if (strArr.length >= 2 && strArr[0].contains("当事人") && !strArr[0].contains("发布主题") && "".equals(adminPunish.getEnterpriseName())) {

                    if(strArr[1].length()>6){
                        adminPunish.setEnterpriseName(strArr[1]
                                .replaceAll("（([0-9a-zA-Z]+)+）", "").replaceAll("（企业代码.*", ""));
                        adminPunish.setObjectType("01");
                    }else{
                        adminPunish.setPersonName(strArr[1].replaceAll("（([0-9a-zA-Z]+)+）", ""));
                        adminPunish.setObjectType("02");
                    }

                }
                //处理 被处罚企业名 为空的情况
                if (strArr.length >= 2 && strArr[0].contains("案件名称") && !strArr[0].contains("发布主题") && "".equals(adminPunish.getEnterpriseName())) {
                    adminPunish.setEnterpriseName(strArr[1].substring(0, strArr[1].indexOf("公司") + 2));
                    adminPunish.setObjectType("01");
                }

                if (strArr.length >= 2 && strArr[0].contains("被处罚企业名") && !strArr[0].contains("发布主题")) {
                    adminPunish.setEnterpriseName(strArr[1]
                            .replaceAll("（([0-9a-zA-Z]+)+）", "").replaceAll("（企业代码.*", ""));
                    adminPunish.setObjectType("01");
                }

                //社会信用代码
                if (strArr.length >= 2 && (strArr[0].contains("社会信用代码") || strArr[0].contains("营业执照") || strArr[0].contains("企业代码"))) {
                    adminPunish.setEnterpriseCode1(strArr[1].replaceAll("\\s*", ""));
                }
                if (strArr.length >= 2 && strArr[0].contains("代表人")) {
                    adminPunish.setPersonName(strArr[1].replaceAll("\\s*", ""));
                }

                //处罚机关
                if (strArr.length >= 2 && strArr[0].contains("作出处罚决定海关")) {
                    adminPunish.setJudgeAuth(strArr[1].replaceAll("\\s*", ""));
                }

                //处罚文号
                if (strArr.length >= 2 && strArr[0].contains("处罚决定书文号")) {
                    adminPunish.setJudgeNo(strArr[1].replaceAll("\\s*", ""));
                }
            }

            if (StrUtil.isBlank(adminPunish.getJudgeNo()) && !str.contains("：") && (str.contains("蓉关知字") && str.contains("号"))) {
                adminPunish.setJudgeNo(str.replaceAll("\\s*", ""));
            }

        }

        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl() + adminPunish.getEnterpriseName() + adminPunish.getPersonName() + adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish, false);
    }
}
