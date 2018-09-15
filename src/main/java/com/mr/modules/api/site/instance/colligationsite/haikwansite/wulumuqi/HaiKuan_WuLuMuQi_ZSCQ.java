package com.mr.modules.api.site.instance.colligationsite.haikwansite.wulumuqi;

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

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：乌鲁木齐海关知识产权行政处罚
 * url:http://urumqi.customs.gov.cn/urumqi_customs/556651/556680/556682/556683/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * modify by pxu 2018-08-22 解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_wulumuqi_zscq")
public class HaiKuan_WuLuMuQi_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String baseUrl = "http://urumqi.customs.gov.cn";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "乌鲁木齐海关知识产权行政处罚";
        String area = "urumqi";//区域为：乌鲁木齐
        String url = "http://urumqi.customs.gov.cn/urumqi_customs/556651/556680/556682/556683/index.html";
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
     * 解析PDF附件
     *
     * @param map
     */
    @Override
    public void extractPdfData(Map<String, String> map) {
        String pdfPath = map.get("filePath");
        String pdfName = map.get("attachmentName");
        String url = map.get("sourceUrl");
        try {
            //根据URL查询库中是否存在指定记录
            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(map.get("sourceUrl")) > 0) {
                log.info("此条记录已存在，不需要入库！");
                return;
            }
            //调用百度通用OCR解析PDF中的图片
            String text = BaiduOCRUtil.getTextStrFromPDFImg(pdfPath, pdfName, "\n");
            //解析文本内容
            parseText(map, text);
        } catch (Throwable e) {
            log.error("读取PDF文件{}{}{}失败", pdfPath, File.separator, pdfName, e);
        }
    }

    /**
     * 解析网页图片
     *
     * @param map
     */
    @Override
    public void extractImgData(Map<String, String> map) {
        String html = map.get("html");
        String url = map.get("sourceUrl");
        try {
            //根据URL查询库中是否存在指定记录
            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(map.get("sourceUrl")) > 0) {
                log.info("此条记录已存在，不需要入库！");
                return;
            }
            List<String> urls = new ArrayList<>();
            Document doc = Jsoup.parse(html);
            Elements imgs = doc.getElementsByTag("img");//获取网页中的图片URL地址
            for (Element img : imgs) {
                urls.add(baseUrl + URLEncoder.encode(img.attr("src"), "UTF-8").replace("%2F", "/"));
            }
            if (urls.size() == 0) {
                return;
            }
            StringBuilder sText = new StringBuilder();
            //通过百度OCR获取文本内容
            List<String> textList = BaiduOCRUtil.getTextFromImageUrlList(urls);
            for (String text : textList) {
                sText.append(text).append("\n");
            }
            //解析文本内容
            parseText(map, sText.toString());
        } catch (Throwable e) {
            log.error("解析网页图片失败,html={}", html, e);
        }
    }

    /**
     * 解析文本内容
     *
     * @param
     * @param content
     */
    private void parseText(Map<String, String> map, String content) {
        if (StrUtil.isEmpty(content)) {
            return;
        }
        content = content.replace(":", "：");
        content = content.replace(";", "；");
        content = content.replace("，", "；");
        content = content.replace(",", "；");
        content = content.replace("。", "；");
        content = content.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名)?(/)?(名称)?[\\s]{0,3}", "当事人");//替换当事人标识
        content = content.replace("法人代表", "法定代表人");

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("乌鲁木齐海关");// 数据来源
        adminPunish.setSubject("海关知识产权行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        for (String lineText : content.split("\n")) {//按行分割读取
            //处罚决定书文号
            if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && (lineText.contains("知字"))) {
                lineText = lineText.replace("〔", "[");
                lineText = lineText.replace("〕", "]");
                lineText = lineText.replace("【", "[");
                lineText = lineText.replace("】", "]");
                lineText = lineText.replace("［", "[");
                lineText = lineText.replace("］", "]");
                lineText = lineText.replace(" ", "");
                adminPunish.setJudgeNo(lineText);
            }
            //当事人
            if (StrUtil.isEmpty(adminPunish.getObjectType()) && (lineText.contains("当事人："))) {
                int index = lineText.indexOf("当事人：");//判断开始标记
                if (index == -1) {
                    index = 0;
                } else {
                    index = index + 4;
                }
                String name = lineText.substring(index);
                index = name.indexOf("；");//判断结束标记
                if (index == -1) {
                    index = name.length();
                }
                name = name.substring(0, index).trim();
                if (name.length() > 6) {
                    adminPunish.setObjectType("01");
                    adminPunish.setEnterpriseName(name);
                } else {
                    adminPunish.setObjectType("02");
                    adminPunish.setPersonName(name);
                }
            }
            //法定代表人
            if (StrUtil.isEmpty(adminPunish.getPersonName()) && lineText.contains("法定代表人：")) {
                int index = lineText.indexOf("法定代表人：");//判断开始标记
                if (index == -1) {
                    index = 0;
                } else {
                    index = index + 6;
                }
                String name = lineText.substring(index);
                index = name.indexOf("；");//判断结束标记
                if (index == -1) {
                    index = name.length();
                }
                adminPunish.setPersonName(name.substring(0, index).trim());
            }
            //法人身份证号码
            if (StrUtil.isEmpty(adminPunish.getPersonId()) && lineText.contains("证件名称、证件号码：")) {
                lineText = lineText.replace("法人身份证号码", "").replace("法人身份证号", "").replace("法人身份证", "");
                int index = lineText.indexOf("证件名称、证件号码：");//判断开始标记
                if (index == -1) {
                    index = 0;
                } else {
                    index = index + 10;
                }
                String name = lineText.substring(index);
                index = name.indexOf("；");//判断结束标记
                if (index == -1) {
                    index = name.length();
                }
                adminPunish.setPersonId(name.substring(0, index).trim());
            }
            //统一社会信用代码
            if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && lineText.contains("统一社会信用代码：")) {
                int index = lineText.indexOf("统一社会信用代码：");//判断开始标记
                if (index == -1) {
                    index = 0;
                } else {
                    index = index + 9;
                }
                String name = lineText.substring(index);
                index = name.indexOf("；");//判断结束标记
                if (index == -1) {
                    index = name.length();
                }
                adminPunish.setEnterpriseCode1(name.substring(0, index));
            }
        }
        adminPunish.setPunishReason(content);//处罚事由
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
