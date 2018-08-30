package com.mr.modules.api.site.instance.colligationsite.haikwansite.chengdu;

import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.convert.Convert;
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

import java.util.Date;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：成都海关走私违规行政处罚
 * url:http://chengdu.customs.gov.cn/chengdu_customs/519405/519431/519433/519435/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_chengdu_zswg")
public class HaiKuan_ChengDu_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "成都海关走私违规行政处罚";
        String area = "chengdu";//区域为：成都
        String baseUrl = "http://chengdu.customs.gov.cn";
        String url = "http://chengdu.customs.gov.cn/chengdu_customs/519405/519431/519433/519435/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if (increaseFlag == null) {
            increaseFlag = "";
        }
        webContext(increaseFlag, baseUrl, url, ip, port, source, area);
        return null;
    }

    //提取Web结构化数据
    @Override
    public void extractWebData(Map<String, String> map) {
        String url = map.get("sourceUrl");

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("成都海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(url);// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        try {
            Document doc = Jsoup.connect(url).execute().parse();
            Element table = doc.getElementsByTag("table").first();//获取表格标签
            if (table != null) {
                Elements trs = table.getElementsByTag("tr");
                for (int i = 0; i < trs.size(); i++) {
                    String colName = "";
                    Elements tds = trs.get(i).getElementsByTag("td");
                    for (int j = 0; j < tds.size(); j++) {
                        String tdText = Convert.toDBC(tds.get(j).text());//全角转半角
                        if (j % 2 == 0) {
                            colName = tdText.replace(" ", "");
                            continue;
                        }
                        //数据内容
                        switch (colName) {
                            case "办案部门":
                                break;
                            case "作出处罚决定海关":
                                adminPunish.setJudgeAuth(tdText);
                                break;
                            case "案件名称":
                                if (tdText.contains("公司") || tdText.contains("厂")) {
                                    int endIndex = tdText.indexOf("公司") + 2;
                                    if (endIndex == 1) {
                                        endIndex = tdText.indexOf("厂") + 1;
                                    }
                                    adminPunish.setObjectType("01");
                                    adminPunish.setEnterpriseName(tdText.substring(0, endIndex));
                                }
                                break;
                            case "案件编号":
                                adminPunish.setJudgeNo(tdText.substring(tdText.indexOf("/") + 1, tdText.length()));
                                break;
                            case "处罚决定书文号":
                                adminPunish.setJudgeNo(tdText);
                                break;
                            case "处罚类别":
                                adminPunish.setPunishType(tdText);
                                break;
                            case "被处罚企业名":
                                adminPunish.setObjectType("01");
                                adminPunish.setEnterpriseName(tdText);
                                break;
                            case "法定代表人":
                                adminPunish.setPersonName(tdText);
                                break;
                            case "统一社会信用代码":
                                adminPunish.setEnterpriseCode1(tdText);
                                break;
                            case "拟公开案件信息":
                            case "违法事实":
                                adminPunish.setPunishReason(tdText);
                                break;
                            case "执法依据":
                                adminPunish.setPunishAccording(tdText);
                                break;
                            case "处罚结果":
                                adminPunish.setPunishResult(tdText);
                                break;
                            case "救济渠道":
                                break;
                            case "其他应当公开的信息":
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else {//普通网页文章
                Element textElement = doc.getElementById("easysiteText");//获取正文标签元素
                if (textElement != null) {
                    StringBuilder sText = new StringBuilder();
                    StringBuilder sReason = new StringBuilder();
                    Elements ps = textElement.getElementsByTag("p");
                    for (Element p : ps) {
                        sText.append(p.text()).append("@!@");
                    }
                    if (StrUtil.isBlank(sText)) {
                        sText.append(CrawlerUtil.replaceHtmlNbsp(Jsoup.parse(textElement.html().replace("<br>", "@!@").replace("<br/>", "@!@")).text()));
                    }
                    for (String lineText : sText.toString().split("@!@")) {
                        if (StrUtil.isBlank(lineText)) {//跳过空行元素
                            continue;
                        }
                        sReason.append(lineText).append("\n");

                        lineText = lineText.replace(" ", "");
                        lineText = lineText.replace(":", "：");
                        lineText = lineText.replace(";", "；");
                        lineText = lineText.replace("，", "；");
                        lineText = lineText.replace(",", "；");
                        lineText = lineText.replace("。", "；");
                        lineText = lineText.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名)?(/)?(名称)?[\\s]{0,3}", "当事人");//替换当事人标识
                        lineText = lineText.replace("被处罚企业名称：", "当事人：");
                        lineText = lineText.replace("被处罚自然人姓名：", "当事人：");
                        lineText = lineText.replace("法人代表", "法定代表人");
                        lineText = lineText.replace("法定代表人姓名", "法定代表人");
                        lineText = lineText.replace("法定代表人为：", "法定代表人：");
                        lineText = lineText.replace("法定代表人为", "法定代表人：");
                        lineText = lineText.replaceAll("法定代表人：[\\s]{0,3}", "法定代表人：");
                        lineText = lineText.replace("法定代表人", "；法定代表人");
                        lineText = lineText.replace("统一社会信用代码", "；统一社会信用代码");
                        lineText = lineText.replace("身份证号码", "身份证");
                        lineText = lineText.replace("身份证号", "身份证");

                        //处罚决定书文号
                        if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && ((lineText.contains("违字")) || lineText.contains("知字") || lineText.contains("关缉") || lineText.contains("单字") || lineText.contains("关知")) && !lineText.contains("根据")) {
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
                        //身份证号码
                        if (StrUtil.isEmpty(adminPunish.getPersonId()) && lineText.contains("身份证：")) {
                            int index = lineText.indexOf("身份证：") + 4;//判断开始标记
                            String id = lineText.substring(index);
                            index = id.indexOf("；");//判断结束标记
                            if (index == -1) {
                                index = id.length();
                            }
                            adminPunish.setPersonId(id.substring(0, index));
                        }
                    }
                    adminPunish.setPunishReason(sReason.toString());
                }
            }
            adminPunish.setUniqueKey(getUniqueKey(adminPunish));//设置UniqueKey
            if (StrUtil.isNotEmpty(adminPunish.getObjectType())) {
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {//插入不存在的数据
                    adminPunishMapper.insert(adminPunish);
                } else {
                    log.info("此条记录已存在，不需要入库！");
                }
            } else {
                log.warn("获取被处罚对象名称失败，数据不入库！adminPunish={}", adminPunish.toString());
            }
        } catch (Throwable e) {
            log.error("解析url={}页面数据失败", url, e);
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
