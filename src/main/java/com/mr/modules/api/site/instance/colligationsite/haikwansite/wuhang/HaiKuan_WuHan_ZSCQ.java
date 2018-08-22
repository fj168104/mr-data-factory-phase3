package com.mr.modules.api.site.instance.colligationsite.haikwansite.wuhang;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：武汉海关知识产权行政处罚
 * url:http://wuhan.customs.gov.cn/wuhan_customs/506378/506396/506398/506399/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * modified by pxu 2018-08-22 解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_wuhan_zscq")
public class HaiKuan_WuHan_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String baseUrl = "http://wuhan.customs.gov.cn";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "武汉海关知识产权行政处罚";
        String area = "wuhan";//区域为：武汉
        String url = "http://wuhan.customs.gov.cn/wuhan_customs/506378/506396/506398/506399/index.html";
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
     * 解析网页图片数据
     *
     * @param map
     */
    @Override
    public void extractImgData(Map<String, String> map) {
        String html = map.get("html");

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("武汉海关");// 数据来源
        adminPunish.setSubject("海关知识产权行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        try {
            List<String> urls = new ArrayList<>();
            Document doc = Jsoup.parse(html);
            Elements imgs = doc.getElementsByTag("img");//获取网页中的图片URL地址
            for (Element img : imgs) {
                urls.add(baseUrl + img.attr("src"));
            }
            StringBuilder reason = new StringBuilder();
            //通过百度OCR获取文本内容
            List<String> textList = BaiduOCRUtil.getTextFromImageUrlList(urls);
            for (String lineText : textList) {//按行读取
                if (lineText == null) {
                    continue;
                }
                reason.append(lineText + "\n");//拼接处罚事由
                lineText = lineText.replace(":", "：");
                lineText = lineText.replace(",", "，");
                //行政处罚决定书文号
                if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && lineText.contains("知字")) {
                    adminPunish.setJudgeNo(lineText.trim());
                }
                //当事人
                if (StrUtil.isEmpty(adminPunish.getObjectType()) && lineText.contains("当事人：")) {
                    String name = lineText.substring(lineText.indexOf("当事人：") + 4);
                    int endIndex = lineText.indexOf("，");
                    if (endIndex == -1) {
                        endIndex = name.length();
                    }
                    name = name.substring(0, endIndex);
                    if (name.length() > 6) {//企业
                        adminPunish.setObjectType("01");
                        adminPunish.setEnterpriseName(name);
                    } else {//个人
                        adminPunish.setObjectType("02");
                        adminPunish.setPersonName(name);
                    }
                }
                //设置法定代表人
                if (StrUtil.isEmpty(adminPunish.getPersonName()) && lineText.contains("法定代表人：")) {
                    String name = lineText.substring(lineText.indexOf("法定代表人：") + 6);
                    int endIndex = lineText.indexOf("，");
                    if (endIndex == -1) {
                        endIndex = name.length();
                    }
                    adminPunish.setPersonName(name.substring(0, endIndex));
                }
            }
            adminPunish.setPunishReason(reason.toString());//处罚事由
            // 设置UniqueKey
            adminPunish.setUniqueKey(getUniqueKey(adminPunish));
            if (StrUtil.isNotEmpty(adminPunish.getObjectType())) {//获取了当事人名称
                //不存在则插入
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                    adminPunishMapper.insert(adminPunish);
                } else {
                    log.info("此条记录已存在，不需要入库！");
                }
            }
        } catch (Exception e) {
            log.error("解析html失败，html={}", html);
        }
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

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("武汉海关");// 数据来源
        adminPunish.setSubject("海关知识产权行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        try {
            //调用百度OCR获取文本内容
            String text = BaiduOCRUtil.getTextStrFromPDFImg(filePath, attachmentName, "\n");
            adminPunish.setPunishReason(text);
            for (String lineText : text.split("\n")) {//按行读取文本
                if (lineText == null) {
                    continue;
                }
                lineText = lineText.replace(":", "：");
                lineText = lineText.replace(",", "，");
                //行政处罚决定书文号
                if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && lineText.contains("知字") && lineText.contains("号")) {
                    adminPunish.setJudgeNo(lineText.trim());
                }
                //当事人
                if (StrUtil.isEmpty(adminPunish.getObjectType()) && lineText.contains("当事人：")) {
                    String name = lineText.substring(lineText.indexOf("当事人：") + 4);
                    int endIndex = lineText.indexOf("，");
                    if (endIndex == -1) {
                        endIndex = name.length();
                    }
                    name = name.substring(0, endIndex);
                    if (name.length() > 6) {//企业
                        adminPunish.setObjectType("01");
                        adminPunish.setEnterpriseName(name);
                    } else {//个人
                        adminPunish.setObjectType("02");
                        adminPunish.setPersonName(name);
                    }
                }
                //设置法定代表人
                if (StrUtil.isEmpty(adminPunish.getPersonName()) && lineText.contains("法定代表人：")) {
                    String name = lineText.substring(lineText.indexOf("法定代表人：") + 6);
                    int endIndex = lineText.indexOf("，");
                    if (endIndex == -1) {
                        endIndex = name.length();
                    }
                    adminPunish.setPersonName(name.substring(0, endIndex));
                }
            }
            // 设置UniqueKey
            adminPunish.setUniqueKey(getUniqueKey(adminPunish));
            if (StrUtil.isNotEmpty(adminPunish.getObjectType())) {//获取了当事人名称
                //不存在则插入
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                    adminPunishMapper.insert(adminPunish);
                } else {
                    log.info("此条记录已存在，不需要入库！");
                }
            }
        } catch (Exception e) {
            log.error("解析PDF失败", e);
        }
    }

    private String getUniqueKey(AdminPunish adminPunish) {
        return MD5Util.encode(adminPunish.getUrl() + adminPunish.getJudgeNo() + adminPunish.getObjectType() + adminPunish.getEnterpriseName() + adminPunish.getPersonName());
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
