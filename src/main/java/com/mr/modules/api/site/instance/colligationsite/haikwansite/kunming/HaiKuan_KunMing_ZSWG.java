package com.mr.modules.api.site.instance.colligationsite.haikwansite.kunming;

import com.mr.common.util.CrawlerUtil;
import com.mr.common.util.ExcelUtil;
import com.mr.framework.core.util.ObjectUtil;
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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：昆明海关走私违规行政处罚
 * url:http://kunming.customs.gov.cn/kunming_customs/611308/611347/611349/611351/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * Modified by pxu 2018-08-20：解析结构化数据
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_kunming_zswg")
public class HaiKuan_KunMing_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "昆明海关走私违规行政处罚";
        String area = "kunming";//区域为：昆明
        String baseUrl = "http://kunming.customs.gov.cn";
        String url = "http://kunming.customs.gov.cn/kunming_customs/611308/611347/611349/611351/index.html";
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

    //提取Web结构化数据
    @Override
    public void extractWebData(Map<String, String> map) {
        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("昆明海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        //解析html
        try {
            Document doc = Jsoup.connect(adminPunish.getUrl()).execute().parse();
            Element textElement = doc.getElementById("easysiteText");
            Element tableElement = textElement.getElementsByTag("table").first();
            if (tableElement == null) {//正文不是表格
                Elements pElements = textElement.getElementsByTag("p");//按段落获取数据
                for (Element pElement : pElements) {
                    String pText = CrawlerUtil.replaceHtmlNbsp(pElement.text());
                    if (pText.contains("行政处罚决定书文号：")) {
                        int beginIndex = pText.indexOf("行政处罚决定书文号：") + 10;
                        String judgeNo = pText.substring(beginIndex);
                        judgeNo = judgeNo.replace("〔", "[");
                        judgeNo = judgeNo.replace("〕", "]");
                        judgeNo = judgeNo.replace("【", "[");
                        judgeNo = judgeNo.replace("】", "]");
                        judgeNo = judgeNo.replace("［", "[");
                        judgeNo = judgeNo.replace("］", "]");
                        judgeNo.replace(" ", "");
                        adminPunish.setJudgeNo(judgeNo);
                        continue;
                    }
                    if (pText.contains("案件名称：")) {
                        continue;
                    }
                    if (pText.contains("处罚机关：")) {
                        int beginIndex = pText.indexOf("处罚机关：") + 5;
                        adminPunish.setJudgeAuth(pText.substring(beginIndex).replace(" ", ""));
                        continue;
                    }
                    if (pText.contains("行政相对人名称：")) {
                        int beginIndex = pText.indexOf("行政相对人名称：") + 8;
                        String name = pText.substring(beginIndex);
                        int endIndex = name.indexOf(" ");
                        if (endIndex > 0) {
                            name = name.substring(0, endIndex);
                        }
                        if (name.length() > 6) {
                            adminPunish.setObjectType("01");
                            adminPunish.setEnterpriseName(name);
                        } else {
                            adminPunish.setObjectType("02");
                            adminPunish.setPersonName(name);
                        }
                    }
                    if (pText.contains("行政相对人海关注册编码：")) {
                    }
                    if (pText.contains("法定代表人姓名：")) {
                        int beginIndex = pText.indexOf("法定代表人姓名：") + 8;
                        String fr = pText.substring(beginIndex);
                        int endIndex = fr.indexOf(" ");
                        if (endIndex > 0) {
                            fr = fr.substring(0, endIndex);
                        }
                        if (StrUtil.isEmpty(adminPunish.getPersonName())) {
                            adminPunish.setPersonName(fr);
                        }
                    }
                    if (pText.contains("法定代表人身份证号：") || pText.contains("法定代表人身份证号码：")) {
                        pText = pText.replace("法定代表人身份证号码：", "法定代表人身份证号：");
                        int beginIndex = pText.indexOf("法定代表人身份证号：") + 10;
                        String frId = pText.substring(beginIndex).replace(" ", "");
                        adminPunish.setPersonId(frId);
                    }
                    if (pText.contains("处罚类型：")) {
                        int beginIndex = pText.indexOf("处罚类型：") + 5;
                        adminPunish.setPunishType(pText.substring(beginIndex).replace(" ", ""));
                        continue;
                    }
                    if (pText.contains("处罚事由：") || pText.contains("处罚是由：")) {
                        pText = pText.replace("处罚是由：", "处罚事由：");
                        int beginIndex = pText.indexOf("处罚事由：") + 5;
                        adminPunish.setPunishReason(pText.substring(beginIndex));
                        continue;
                    }
                    if (pText.contains("处罚依据：")) {
                        int beginIndex = pText.indexOf("处罚依据：") + 5;
                        adminPunish.setPunishAccording(pText.substring(beginIndex));
                        continue;
                    }
                    if (pText.contains("处罚结果：")) {
                        int beginIndex = pText.indexOf("处罚结果：") + 5;
                        adminPunish.setPunishResult(pText.substring(beginIndex));
                        continue;
                    }
                    if (pText.contains("处罚决定日期：")) {
                        int beginIndex = pText.indexOf("处罚决定日期：") + 7;
                        adminPunish.setJudgeDate(pText.substring(beginIndex));
                        continue;
                    }
                }
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));//设置UniqueKey
                if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getEnterpriseName()) || StrUtil.isNotEmpty(adminPunish.getPersonName()))) {
                    if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {//插入不存在的数据
                        adminPunishMapper.insert(adminPunish);
                    }
                }
            } else {//正文是一个表格
                boolean isData = false;
                Elements trs = tableElement.getElementsByTag("tr");
                for (Element tr : trs) {
                    if (tr.text().contains("序号")) {
                        isData = true;
                        continue;
                    }
                    if (isData) {
                        Elements tds = tr.getElementsByTag("td");
                        AdminPunish adminPunishTd = ObjectUtil.clone(adminPunish);
                        //序号
                        //行政处罚决定书文号
                        adminPunishTd.setJudgeNo(tds.get(1).text());
                        //处罚名称
                        //处罚类别1
                        adminPunishTd.setPunishType(tds.get(3).text());
                        //处罚类别2
                        //处罚事由
                        adminPunishTd.setPunishReason(tds.get(5).text());
                        //处罚依据
                        adminPunishTd.setPunishAccording(tds.get(6).text());
                        //行政相对人名称
                        String name = tds.get(7).text();
                        if (name.length() > 6) {
                            adminPunishTd.setObjectType("01");
                            adminPunishTd.setEnterpriseName(name);
                        } else {
                            adminPunishTd.setObjectType("02");
                            adminPunishTd.setPersonName(name);
                        }
                        //行政相对人代码_1 (统一社会信用代码)
                        adminPunishTd.setEnterpriseCode1(tds.get(8).text());
                        //行政相对人代码_2 (组织机构代码)
                        adminPunishTd.setEnterpriseCode3(tds.get(9).text());
                        //行政相对人代码_3 (工商登记码)
                        adminPunishTd.setEnterpriseCode2(tds.get(10).text());
                        //行政相对人代码_4 (税务登记号)
                        adminPunishTd.setEnterpriseCode4(tds.get(11).text());
                        //行政相对人代码_5 (居民身份证号)
                        adminPunishTd.setPersonId(tds.get(12).text());
                        //法定代表人姓名
                        adminPunishTd.setPersonName(tds.get(13).text());
                        //处罚结果
                        adminPunishTd.setPunishResult(tds.get(14).text());
                        //处罚决定日期
                        adminPunishTd.setJudgeDate(tds.get(15).text());
                        //处罚机关
                        adminPunishTd.setJudgeAuth(tds.get(16).text());
                        //当前状态
                        adminPunishTd.setStatus(tds.get(17).text());
                        //地方编码
                        //数据更新时间戳
                        //备注

                        adminPunishTd.setUniqueKey(getUniqueKey(adminPunishTd));//设置UniqueKey
                        if (adminPunishMapper.selectCountByUniqueKey(adminPunishTd.getSource(), adminPunishTd.getSubject(), adminPunishTd.getUniqueKey()) == 0) {//插入不存在的数据
                            adminPunishMapper.insert(adminPunishTd);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析url={}页面数据失败", adminPunish.getUrl(), e);
        }
    }

    /**
     * 解析XLS数据
     *
     * @param map
     */
    @Override
    public void extractXlsData(Map<String, String> map) {
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");
        String xlsPath = filePath + File.separator + attachmentName;
        try {
            boolean flag = false;
            List<Map<String, Object>> list = ExcelUtil.importFromXls(xlsPath, new String[]{"序号", "行政处罚决定书文号", "处罚名称", "处罚类别1", "处罚类别2", "处罚事由", "处罚依据", "行政相对人名称", "行政相对人代码_1 (统一社会信用代码)", "行政相对人代码_2 (组织机构代码)", "行政相对人代码_3 (工商登记码)", "行政相对人代码_4 (税务登记号)", "行政相对人代码_5 (居民身份证号)", "法定代表人姓名", "处罚结果", "处罚决定日期", "处罚机关", "当前状态", "地方编码", "数据更新时间戳", "备注"});
            for (Map<String, Object> dataMap : list) {
                if (dataMap.get("序号") != null && dataMap.get("序号").toString().contains("序号")) {
                    flag = true;
                    continue;
                }
                if (flag) {
                    AdminPunish adminPunish = createAdminPunish();
                    adminPunish.setSource("昆明海关");// 数据来源
                    adminPunish.setSubject("海关走私违规行政处罚");// 主题
                    adminPunish.setUrl(map.get("sourceUrl"));// url
                    adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
                    //序号
                    //行政处罚决定书文号
                    if (dataMap.get("行政处罚决定书文号") != null) {
                        adminPunish.setJudgeNo(dataMap.get("行政处罚决定书文号").toString().trim());
                    }
                    //处罚名称
                    //处罚类别1
                    if (dataMap.get("处罚类别1") != null) {
                        adminPunish.setPunishType(dataMap.get("处罚类别1").toString().trim());
                    }
                    //处罚类别2
                    //处罚事由
                    if (dataMap.get("处罚事由") != null) {
                        adminPunish.setPunishReason(dataMap.get("处罚事由").toString().trim());
                    }
                    //处罚依据
                    if (dataMap.get("处罚依据") != null) {
                        adminPunish.setPunishAccording(dataMap.get("处罚依据").toString().trim());
                    }
                    //行政相对人名称
                    if (dataMap.get("行政相对人名称") != null) {
                        String name = dataMap.get("行政相对人名称").toString().trim();
                        if (name.length() > 6) {
                            adminPunish.setObjectType("01");
                            adminPunish.setEnterpriseName(name);
                        } else {
                            adminPunish.setObjectType("02");
                            adminPunish.setPersonName(name);
                        }
                    }
                    //行政相对人代码_1 (统一社会信用代码)
                    if (dataMap.get("行政相对人代码_1 (统一社会信用代码)") != null) {
                        adminPunish.setEnterpriseCode1(dataMap.get("行政相对人代码_1 (统一社会信用代码)").toString().trim());
                    }
                    //行政相对人代码_2 (组织机构代码)
                    if (dataMap.get("行政相对人代码_2 (组织机构代码)") != null) {
                        adminPunish.setEnterpriseCode3(dataMap.get("行政相对人代码_2 (组织机构代码)").toString().trim());
                    }
                    //行政相对人代码_3 (工商登记码)
                    if (dataMap.get("行政相对人代码_3 (工商登记码)") != null) {
                        adminPunish.setEnterpriseCode2(dataMap.get("行政相对人代码_3 (工商登记码)").toString().trim());
                    }
                    //行政相对人代码_4 (税务登记号)
                    if (dataMap.get("行政相对人代码_4 (税务登记号)") != null) {
                        adminPunish.setEnterpriseCode4(dataMap.get("行政相对人代码_4 (税务登记号)").toString().trim());
                    }
                    //行政相对人代码_5 (居民身份证号)
                    if (dataMap.get("行政相对人代码_5 (居民身份证号)") != null) {
                        adminPunish.setPersonId(dataMap.get("行政相对人代码_5 (居民身份证号)").toString().trim());
                    }
                    //法定代表人姓名
                    if (dataMap.get("法定代表人姓名") != null) {
                        adminPunish.setPersonName(dataMap.get("法定代表人姓名").toString().trim());
                    }
                    //处罚结果
                    if (dataMap.get("处罚结果") != null) {
                        adminPunish.setPunishResult(dataMap.get("处罚结果").toString().trim());
                    }
                    //处罚决定日期
                    if (dataMap.get("处罚决定日期") != null) {
                        adminPunish.setJudgeDate(dataMap.get("处罚决定日期").toString().trim());
                    }
                    //处罚机关
                    if (dataMap.get("处罚机关") != null) {
                        adminPunish.setJudgeAuth(dataMap.get("处罚机关").toString().trim());
                    }
                    //当前状态
                    if (dataMap.get("当前状态") != null) {
                        adminPunish.setStatus(dataMap.get("当前状态").toString().trim());
                    }
                    //地方编码
                    //数据更新时间戳
                    //备注

                    adminPunish.setUniqueKey(getUniqueKey(adminPunish));//设置UniqueKey
                    if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getEnterpriseName()) || StrUtil.isNotEmpty(adminPunish.getPersonName()))) {
                        if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {//插入不存在的数据
                            adminPunishMapper.insert(adminPunish);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析xls文件{}失败", xlsPath, e);
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
