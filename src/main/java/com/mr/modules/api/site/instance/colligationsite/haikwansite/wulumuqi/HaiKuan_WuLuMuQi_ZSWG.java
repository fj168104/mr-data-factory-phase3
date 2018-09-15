package com.mr.modules.api.site.instance.colligationsite.haikwansite.wulumuqi;

import com.mr.common.util.AIOCRUtil;
import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.ocr.OcrUtils;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：乌鲁木齐海关走私违规行政处罚
 * url:http://urumqi.customs.gov.cn/urumqi_customs/556651/556680/556682/556684/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * modify by pxu 2018-08-23 解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_wulumuqi_zswg")
public class HaiKuan_WuLuMuQi_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String baseUrl = "http://urumqi.customs.gov.cn";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "乌鲁木齐海关走私违规行政处罚";
        String area = "urumqi";//区域为：乌鲁木齐
        String url = "http://urumqi.customs.gov.cn/urumqi_customs/556651/556680/556682/556684/index.html";
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
            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(url) > 0) {
                log.info("此条记录已存在，不需要入库！");
                return;
            }
            //1、pdf是可读的文本
            String text = null;
            try {
                OcrUtils ocr = new OcrUtils(pdfPath);
                File textFile = new File(ocr.readPdf(pdfName));//解析pdf成txt
                text = FileUtils.readFileToString(textFile, "utf-8");//读取txt
                FileUtils.deleteQuietly(textFile);//删除txt
            } catch (Throwable t) {
                log.warn("读取PDF文件失败", t);
            }
            //2、pdf是图片，调用腾讯
            if (StrUtil.isBlank(text)) {
                //调用OCR识别PDF中的图片
                text = AIOCRUtil.getTextStrFromPDFImg(pdfPath, pdfName);//先调用腾讯OCR识别
            }
            //3、pdf是图片，调用百度
            if (StrUtil.isBlank(text)) {
                text = BaiduOCRUtil.getTextStrFromPDFImg(pdfPath, pdfName, "\n");//若腾讯OCR识别失败，则调用百度OCR
            }
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
            sText.append(AIOCRUtil.getTextFromImageUrlList(urls));//调用腾讯OCR识别
            if (StrUtil.isBlank(sText.toString())) {
                List<String> textList = BaiduOCRUtil.getTextFromImageUrlList(urls);//调用腾讯OCR识别
                for (String text : textList) {
                    sText.append(text).append("\n");
                }
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
        if (StrUtil.isBlank(content)) {
            return;
        }
        String oriContent = content;//不做任何处理的原始内容

        content = content.replace(":", "：");
        content = content.replace(";", "；");
        content = content.replace("，", "；");
        content = content.replace(",", "；");
        content = content.replace("。", "；");
        content = content.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名)?(/)?(名称)?[\\s]{0,3}", "当事人");//替换当事人标识
        content = content.replace("法人代表", "法定代表人");
        content = content.replace("法定代表人为：", "法定代表人：");
        content = content.replace("法定代表人为", "法定代表人：");
        content = content.replaceAll("法定代表人：[\\s]{0,3}", "法定代表人：");
        content = content.replace(" ", "；");

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("乌鲁木齐海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        for (String lineText : content.split("\n")) {//按行分割读取
            //处罚决定书文号
            if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && ((lineText.contains("违字")) || lineText.contains("罚字") | lineText.contains("查字") || lineText.contains("简字") || lineText.contains("易字")) ||
                    lineText.contains("单字")) {
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
                if (StrUtil.isNotBlank(name)) {
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
            }
            //若上述无法获得当事人名称，则根据“公司”二字判断
            if (StrUtil.isEmpty(adminPunish.getObjectType()) && (lineText.contains("公司"))) {
                int index = lineText.indexOf("公司") + 2;//判断结束标记
                String name = lineText.substring(0, index);
                index = name.indexOf("；") + 1;//判断开始标记
                if (index > 0) {
                    name = name.substring(index, name.length());
                }
                adminPunish.setObjectType("01");
                adminPunish.setEnterpriseName(name.trim());
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
        adminPunish.setPunishReason(oriContent);//处罚事由
        // 设置UniqueKey
        adminPunish.setUniqueKey(getUniqueKey(adminPunish));
        //不存在则插入
        if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
            adminPunishMapper.insert(adminPunish);
        } else {
            log.info("此条记录已存在，不需要入库！");
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
