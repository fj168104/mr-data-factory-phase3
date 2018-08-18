package com.mr.modules.api.site.instance.colligationsite.haikwansite.beijing;

import com.gargoylesoftware.htmlunit.Page;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.ocr.OcrUtils;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：北京海关知识产权行政处罚
 * url:http://beijing.customs.gov.cn/beijing_customs/434756/434811/434813/434814/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_beijing_zscq")
public class HaiKuan_BeiJing_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "北京海关知识产权行政处罚";
        String area = "beijing";//区域为：北京
        String baseUrl = "http://beijing.customs.gov.cn";
        String url = "http://beijing.customs.gov.cn/beijing_customs/434756/434811/434813/434814/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if (increaseFlag == null) {
            increaseFlag = "";
        }
        webContext(increaseFlag, baseUrl, url, ip, port, source, area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    /**
     * 解析web页面数据
     *
     * @param map
     */
    @Override
    public void extractWebData(Map<String, String> map) {
        String html = map.get("html");
        StringBuilder sText = new StringBuilder();
        try {
            Document doc = Jsoup.parse(html);
            Elements elements = doc.select("div.easysite-news-text").first().getElementsByTag("p");
            for (Element element : elements) {
                sText.append(element.text()).append("\n");
            }
        } catch (Throwable t) {
            log.warn("读取html内容失败", t);
        }
        if (StrUtil.isEmpty(sText.toString())) {
            log.warn("读取html失败，忽略本条记录数据");
            return;
        }
        parseText(map, sText.toString());
    }

    /**
     * 解析PDF数据
     *
     * @param map
     */
    @Override
    public void extractPdfData(Map<String, String> map) {
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");

        //读取PDF文件成text
        String text = null;
        try {
            OcrUtils ocr = new OcrUtils(filePath);
            File textFile = new File(ocr.readPdf(attachmentName));//解析pdf成txt
            text = FileUtils.readFileToString(textFile, "utf-8");//读取txt
            FileUtils.deleteQuietly(textFile);//删除txt
        } catch (Throwable t) {
            log.warn("读取PDF文件失败", t);
        }
        if (StrUtil.isEmpty(text)) {
            log.warn("读取PDF文件失败，忽略本条记录数据");
            return;
        }
        parseText(map, text);
    }

    /**
     * 解析文本内容
     *
     * @param map
     * @param text
     */
    private void parseText(Map<String, String> map, String text) {
        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("北京海关");// 数据来源
        adminPunish.setSubject("海关知识产权行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        //解析text
        try {
            adminPunish.setPunishReason(text);
            String content = text;
            content = content.replace("　", " ");//替换全角空格
            content = content.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名/名称)?[\\s]{0,3}", "当事人");//替换当事人标识
            content = content.replaceAll("发[\\s]{0,3}件[\\s]{0,3}人[\\s]{0,3}", "当事人");
            content = content.replaceAll("法[\\s]{0,3}人[\\s]{0,3}代[\\s]{0,3}表", "法定代表人");
            content = content.replace("法定代表人为", "法定代表人：");
            content = content.replace(":", "：");//替换英文冒号为中文冒号
            content = content.replace(",", "，");//替换英文逗号
            content = content.replace("\r", "\n");//替换回车换行符

            for (String str : content.split("\n")) {
                if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && !str.contains("：") && (str.contains("知字")) && str.contains("号")) {
                    str = str.replace("〔", "[");
                    str = str.replace("〕", "]");
                    str = str.replace("【", "[");
                    str = str.replace("】", "]");
                    str = str.replace("［", "[");
                    str = str.replace("］", "]");
                    adminPunish.setJudgeNo(str.replace(" ", ""));//处罚决定书文号
                }
                if (StrUtil.isEmpty(adminPunish.getEnterpriseName()) && StrUtil.isEmpty(adminPunish.getPersonName()) && str.contains("当事人：")) {
                    String name = str.substring(str.indexOf("当事人：") + 4).trim();
                    int endIndex = name.replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        name = name.substring(0, endIndex);
                    }
                    //上面针对逗号的处理可能将公司英文名称后面CO.,LTD的内容去掉
                    if (str.toUpperCase().contains("CO.，LTD") && name.toUpperCase().endsWith("CO.")) {
                        int index = str.toUpperCase().indexOf("CO.，") + 4;
                        name = name + "," + str.substring(index, index + 3);
                    }
                    if (name.length() > 6) {//企业
                        adminPunish.setObjectType("01");
                        adminPunish.setEnterpriseName(name);//企业名称
                    } else {//个人
                        adminPunish.setObjectType("02");
                        adminPunish.setPersonName(name);//姓名
                    }
                }
                if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && str.contains("统一社会信用代码：")) {
                    String code = str.substring(str.indexOf("统一社会信用代码：") + 9).trim();
                    int endIndex = code.replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        code = code.substring(0, endIndex);
                    }
                    adminPunish.setEnterpriseCode1(code);//统一社会信用代码
                }
                if (StrUtil.isEmpty(adminPunish.getPersonName()) && (str.contains("法定代表人："))) {
                    String personName = str.substring(str.indexOf("法定代表人：") + 6).trim();
                    int endIndex = personName.replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        personName = personName.substring(0, endIndex);
                    }
                    adminPunish.setPersonName(personName);//企业法定代表人
                }
            }
            // 设置UniqueKey
            adminPunish.setUniqueKey(adminPunish.getUrl() + adminPunish.getJudgeNo() + adminPunish.getEnterpriseName() + adminPunish.getPersonName());
            if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getPersonName()) || StrUtil.isNotEmpty(adminPunish.getEnterpriseName()))) {
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在则插入
                    adminPunishMapper.insert(adminPunish);
                }
            }
        } catch (Exception e) {
            log.error("URL={}解析入库失败", map.get("sourceUrl"), e);
        }
    }

    private AdminPunish createAdminPunish() {
        AdminPunish adminPunish = new AdminPunish();
        Date now = new Date();
        adminPunish.setCreatedAt(now);//本条记录创建时间
        adminPunish.setUpdatedAt(now);// 本条记录最后更新时间
        adminPunish.setSource("");// 数据来源
        adminPunish.setSubject("");// 主题
        adminPunish.setUniqueKey("");//唯一性标识(url+企业名称/自然人名称+发布时间+发布机构)
        adminPunish.setUrl("");// url
        adminPunish.setObjectType("");// 主体类型: 01-企业 02-个人。默认为企业
        adminPunish.setEnterpriseName("");// 企业名称
        adminPunish.setEnterpriseCode1("");// 统一社会信用代码
        adminPunish.setEnterpriseCode2("");// 营业执照注册号
        adminPunish.setEnterpriseCode3("");// 组织机构代码
        adminPunish.setEnterpriseCode4("");// 税务登记号
        adminPunish.setPersonName("");// 法定代表人/负责人姓名|负责人姓名
        adminPunish.setPersonId("");// 法定代表人身份证号|负责人身份证号
        adminPunish.setPunishType("");//处罚类型
        adminPunish.setPunishReason("");// 处罚事由
        adminPunish.setPunishAccording("");//处罚依据
        adminPunish.setPunishResult("");// 处罚结果
        adminPunish.setJudgeNo("");// 执行文号
        adminPunish.setJudgeDate("");// 执行时间
        adminPunish.setJudgeAuth("");// 判决机关
        adminPunish.setPublishDate("");// 发布日期
        adminPunish.setStatus("");// 当前状态
        return adminPunish;
    }
}
