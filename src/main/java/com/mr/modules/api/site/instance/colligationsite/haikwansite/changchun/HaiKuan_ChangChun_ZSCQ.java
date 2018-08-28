package com.mr.modules.api.site.instance.colligationsite.haikwansite.changchun;

import com.mr.common.util.AIOCRUtil;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：长春海关知识产权行政处罚
 * url:http://changchun.customs.gov.cn/changchun_customs/465846/465861/465863/465864/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_changchun_zscq")
public class HaiKuan_ChangChun_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "长春海关知识产权行政处罚";
        String area = "changchun";//区域为：长春
        String baseUrl = "http://changchun.customs.gov.cn";
        String url = "http://changchun.customs.gov.cn/changchun_customs/465846/465861/465863/465864/index.html";
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

    @Override
    public void extractPdfData(Map<String, String> map) {
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");
        String url = map.get("sourceUrl");
        try {
            //根据URL查询库中是否存在指定记录
            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(url) > 0) {
                log.info("此条记录已存在，不需要入库！");
                return;
            }
            String text = AIOCRUtil.getTextStrFromPDFImg(filePath, attachmentName);//调用腾讯OCR
            if (StrUtil.isBlank(text)) {
                text = BaiduOCRUtil.getTextStrFromPDFFile(filePath, attachmentName);//调用百度OCR
            }
            //解析文本内容
            parseText(map, text);
        } catch (Throwable t) {
            log.error("解析PDF{}失败,url={}", filePath + File.separator + attachmentName, url, t);
        }
    }

    @Override
    public void extractImgData(Map<String, String> map) {
        String url = map.get("sourceUrl");
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");
        String attachmentList = map.get("attachmentList");
        log.info("filePath=" + filePath);
        log.info("attachmentName=" + attachmentName);
        log.info("attachmentList=" + attachmentList);
        try {
            //根据URL查询库中是否存在指定记录
            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(url) > 0) {
                log.info("此条记录已存在，不需要入库！");
                return;
            }
            if (StrUtil.isNotBlank(attachmentList)) {
                StringBuilder sText = new StringBuilder();

                List<String> imgs = new ArrayList<>();
                for (String img : attachmentList.split("@!@")) {
                    File f = new File(img);
                    if (img.contains(".tif")) {//tif图片附件
                        String bodyText = AIOCRUtil.getTextStrFromTIFFile(f.getParent(), f.getName());//优先调用腾讯OCR
                        if (StrUtil.isBlank(bodyText)) {
                            bodyText = BaiduOCRUtil.getTextStrFromTIFFile(f.getParent(), f.getName());//百度OCR为备用
                        }
                        sText.append(bodyText);
                    } else {
                        imgs.add(img);
                    }
                }
                if (imgs.size() > 0) {
                    sText.append(AIOCRUtil.getTextFromImageFileList(imgs));//调用腾讯OCR识别
                    if (StrUtil.isBlank(sText.toString())) {
                        List<String> textList = BaiduOCRUtil.getTextFromImageFileList(imgs);//调用百度OCR识别
                        for (String text : textList) {
                            sText.append(text).append("\n");
                        }
                    }
                }
                //解析文本内容
                parseText(map, sText.toString());
            }
        } catch (Throwable t) {
            log.error("解析网页图片数据失败,url={}", url, t);
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

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("长春海关");// 数据来源
        adminPunish.setSubject("海关知识产权行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        content = content.replace(":", "：");
        content = content.replace(";", "；");
        content = content.replace("，", "；");
        content = content.replace(",", "；");
        content = content.replace("。", "；");
        content = content.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名)?(/)?(名称)?[\\s]{0,3}", "当事人");//替换当事人标识
        content = content.replace("被处罚企业名称：", "当事人：");
        content = content.replace("被处罚自然人姓名：", "当事人：");
        content = content.replace("法人代表", "法定代表人");
        content = content.replace("法定代表人姓名", "法定代表人");
        content = content.replace("法定代表人为：", "法定代表人：");
        content = content.replace("法定代表人为", "法定代表人：");
        content = content.replaceAll("法定代表人：[\\s]{0,3}", "法定代表人：");
        //content = content.replace(" ", "；");
        //加一个分隔符
        content = content.replace("法定代表人", "；法定代表人");
        content = content.replace("统一社会信用代码", "；统一社会信用代码");

        for (String lineText : content.split("\n")) {//按行分割读取
            //处罚决定书文号
            if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && ((lineText.contains("违字")) || lineText.contains("知字") || lineText.contains("关缉") || lineText.contains("关知") || lineText.contains("法字")) && !lineText.contains("根据")) {
                lineText = lineText.replace("〔", "[");
                lineText = lineText.replace("〕", "]");
                lineText = lineText.replace("【", "[");
                lineText = lineText.replace("】", "]");
                lineText = lineText.replace("［", "[");
                lineText = lineText.replace("］", "]");
                lineText = lineText.replace(" ", "");
                lineText = lineText.replace("；", "");
                if (lineText.contains("：")) {
                    lineText = lineText.substring(lineText.indexOf("：") + 1, lineText.length());
                }
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
                    if (name.endsWith(")") && name.contains("(")) {
                        index = name.indexOf("(");
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
            if (StrUtil.isEmpty(adminPunish.getObjectType()) && (lineText.contains("公司") || lineText.contains("厂"))) {
                int index = lineText.indexOf("公司") + 2;//判断结束标记
                if (index == 1) {
                    index = lineText.indexOf("厂") + 1;
                }
                String name = lineText.substring(0, index);
                name = name.replace("：", "；");
                index = name.indexOf("；") + 1;//判断开始标记
                if (index > 0) {
                    name = name.substring(index, name.length());
                }
                adminPunish.setObjectType("01");
                adminPunish.setEnterpriseName(name.trim().replace("事人：", "").replace("当事人", ""));
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
            //处罚机关
            if (StrUtil.isEmpty(adminPunish.getJudgeAuth()) && lineText.contains("作出处罚决定的海关名称：")) {
                int index = lineText.indexOf("作出处罚决定的海关名称：") + 12;//判断开始标记
                String name = lineText.substring(index);
                index = name.indexOf("；");//判断结束标记
                if (index == -1) {
                    index = name.length();
                }
                adminPunish.setJudgeAuth(name.substring(0, index));
            }
        }
        adminPunish.setPunishReason(oriContent);//处罚事由
        // 设置UniqueKey
        adminPunish.setUniqueKey(getUniqueKey(adminPunish));
        if (StrUtil.isNotEmpty(adminPunish.getObjectType())) {
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
