package com.mr.modules.api.site.instance.colligationsite.haikwansite.haikou;

import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.util.ObjectUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
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
 * 主题：海口海关走私违规行政处罚
 * url:http://haikou.customs.gov.cn/haikou_customs/605737/605757/605759/605761/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * Modified by pxu 2018-08-20：解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_haikou_zswg")
public class HaiKuan_HaiKou_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "海口海关走私违规行政处罚";
        String area = "haikou";//区域为：海口
        String baseUrl = "http://haikou.customs.gov.cn";
        String url = "http://haikou.customs.gov.cn/haikou_customs/605737/605757/605759/605761/index.html";
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

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("海口海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        //解析html
        try {
            Document doc = Jsoup.parse(html);
            Element tableElement = doc.getElementsByTag("table").first();
            if (tableElement != null) {//网页正文为表格
                String colName = "";
                Elements trs = tableElement.getElementsByTag("tr");
                //遍历行
                for (Element tr : trs) {
                    Elements tds = tr.getElementsByTag("td");
                    //遍历单元格
                    for (int i = 0; i < tds.size(); i++) {
                        String tdText = CrawlerUtil.replaceHtmlNbsp(tds.get(i).text());
                        //奇数单元格
                        if (i % 2 == 0) {
                            colName = tdText;
                            continue;
                        }
                        //偶数单元额
                        switch (colName) {
                            case "案件名称":
                                break;
                            case "决定书号":
                                tdText = tdText.replace("〔", "[");
                                tdText = tdText.replace("〕", "]");
                                tdText = tdText.replace("【", "[");
                                tdText = tdText.replace("】", "]");
                                tdText = tdText.replace("［", "[");
                                tdText = tdText.replace("］", "]");
                                adminPunish.setJudgeNo(tdText);
                                break;
                            case "决定日期":
                                adminPunish.setJudgeDate(tdText);
                                break;
                            case "处罚类别":
                                adminPunish.setPunishType(tdText);
                                break;
                            case "决定机关":
                                adminPunish.setJudgeAuth(tdText);
                                break;
                            case "当事人":
                                tdText = tdText.replace("（申报单位）", "");
                                tdText = tdText.replace("（简称海域公司）", "");
                                //当事人名称
                                String name = tdText.trim();
                                int endIndex = name.replace(",", "，").replace("。", "，").indexOf("，");
                                if (endIndex > 0) {//若后面有逗号或句号，则截取
                                    name = name.substring(0, endIndex);
                                }
                                if (tdText.length() > 6) {
                                    adminPunish.setObjectType("01");
                                    adminPunish.setEnterpriseName(name);
                                } else {
                                    adminPunish.setObjectType("02");
                                    adminPunish.setPersonName(name);
                                }
                                //法定代表人
                                if (StrUtil.isEmpty(adminPunish.getPersonName()) && tdText.contains("法定代表人：")) {
                                    String fr = tdText.substring(tdText.indexOf("法定代表人：") + 6).trim();
                                    endIndex = fr.replace(",", "，").replace("。", "，").indexOf("，");
                                    if (endIndex > 0) {//若后面有逗号或句号，则截取
                                        fr = fr.substring(0, endIndex);
                                    }
                                    adminPunish.setPersonName(fr);
                                }
                                break;
                            case "违法事实":
                                adminPunish.setPunishReason(tdText);
                                break;
                            case "处罚结果及依据":
                                adminPunish.setPunishResult(tdText);
                                break;
                            case "救济渠道":
                                break;
                            default:
                                break;
                        }
                    }
                }
            } else {//网页正文为文章
                adminPunish.setPunishReason(doc.text());
                html = html.replace("<br>", "@!@");//替换换行符
                doc = Jsoup.parse(html);//重新解析html
                Elements pElements = doc.getElementsByTag("p");//按段落获取
                for (Element pElement : pElements) {
                    String pText = CrawlerUtil.replaceHtmlNbsp(pElement.text());
                    for (String text : pText.split("@!@")) {
                        if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && text.contains("缉违") && text.contains("号")) {//决定书文号
                            text = text.replace("〔", "[");
                            text = text.replace("〕", "]");
                            text = text.replace("【", "[");
                            text = text.replace("】", "]");
                            text = text.replace("［", "[");
                            text = text.replace("］", "]");
                            adminPunish.setJudgeNo(text.trim());
                            continue;
                        }
                        if (StrUtil.isEmpty(adminPunish.getEnterpriseName()) && StrUtil.isEmpty(adminPunish.getPersonName()) && text.contains("当事人：")) {
                            String name = text.substring(text.indexOf("当事人：") + 4).trim();
                            if (text.length() > 6) {//企业
                                adminPunish.setObjectType("01");
                                adminPunish.setEnterpriseName(name);
                            } else {//个人
                                adminPunish.setObjectType("02");
                                adminPunish.setPersonName(name);
                            }
                            continue;
                        }
                        if (StrUtil.isEmpty(adminPunish.getPersonName()) && text.contains("法定代表人：")) {
                            String fr = text.substring(text.indexOf("法定代表人：") + 6).trim();
                            adminPunish.setPersonName(fr);
                        }
                    }
                }
                if (adminPunish.getJudgeNo().contains("浦区关缉违字[2016]0001号")) {
                    adminPunish.setJudgeNo("浦区关缉违字[2016]0001号");
                    adminPunish.setPunishType("警告");
                    adminPunish.setJudgeDate("2016年4月14日");
                    adminPunish.setJudgeAuth("洋浦经济开发区海关");
                }
            }
            if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getEnterpriseName()) || StrUtil.isNotEmpty(adminPunish.getPersonName()))) {
                if (adminPunish.getEnterpriseName().contains("、")) {//若当事人存在多个，则插入多条记录
                    String[] names = adminPunish.getEnterpriseName().split("、");
                    for (String name : names) {
                        AdminPunish adminPunishCopy = ObjectUtil.clone(adminPunish);
                        adminPunishCopy.setEnterpriseName(name.trim());
                        //拼接UniqueKey
                        adminPunishCopy.setUniqueKey(adminPunishCopy.getUrl() + adminPunishCopy.getJudgeNo() + adminPunishCopy.getEnterpriseName() + adminPunishCopy.getPersonName());
                        //只插入不存在的记录
                        if (adminPunishMapper.selectCountByUniqueKey(adminPunishCopy.getSource(), adminPunishCopy.getSubject(), adminPunishCopy.getUniqueKey()) == 0) {
                            adminPunishMapper.insert(adminPunishCopy);
                        } else {
                            log.info("此条记录已存在，不需要入库！");
                        }
                    }
                } else {
                    //拼接UniqueKey
                    adminPunish.setUniqueKey(adminPunish.getUrl() + adminPunish.getJudgeNo() + adminPunish.getEnterpriseName() + adminPunish.getPersonName());
                    //只插入不存在的记录
                    if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                        adminPunishMapper.insert(adminPunish);
                    } else {
                        log.info("此条记录已存在，不需要入库！");
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析url={}页面数据失败", adminPunish.getUrl());
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
