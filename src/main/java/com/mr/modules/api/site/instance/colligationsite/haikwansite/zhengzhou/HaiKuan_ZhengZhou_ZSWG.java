package com.mr.modules.api.site.instance.colligationsite.haikwansite.zhengzhou;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：郑州海关走私违规行政处罚
 * url:http://zhengzhou.customs.gov.cn/zhengzhou_customs/501404/501425/501427/501429/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * Modified by pxu 2018-08-21：结构数据入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_zhengzhou_zswg")
public class HaiKuan_ZhengZhou_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "郑州海关走私违规行政处罚";
        String area = "zhengzhou";//区域为：郑州
        String baseUrl = "http://zhengzhou.customs.gov.cn";
        String url = "http://zhengzhou.customs.gov.cn/zhengzhou_customs/501404/501425/501427/501429/index.html";
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
        String html = map.get("html");

        try {
            Document doc = Jsoup.parse(html);
            Element table = doc.getElementsByTag("table").first();//获取表格标签
            if (table != null) {
                List<String> colList = new ArrayList<>();
                Elements trs = table.getElementsByTag("tr");
                for (int i = 0; i < trs.size(); i++) {
                    AdminPunish adminPunish = createAdminPunish();
                    adminPunish.setSource("郑州海关");// 数据来源
                    adminPunish.setSubject("海关走私违规行政处罚");// 主题
                    adminPunish.setUrl(map.get("sourceUrl"));// url
                    adminPunish.setPublishDate(map.get("publishDate"));// 发布日期

                    Elements tds = trs.get(i).getElementsByTag("td");
                    for (int j = 0; j < tds.size(); j++) {
                        String tdText = Convert.toDBC(tds.get(j).text());
                        if (i == 0) {//第一行为标题行，将标题内容加入LIST
                            colList.add(tdText.replace(" ", ""));
                            continue;
                        }
                        String colTitle = colList.get(j);
                        if (colTitle.contains("行政处罚决定书号")) {
                            adminPunish.setJudgeNo(tdText.replace(" ", ""));
                        } else if (colTitle.contains("被行政处罚当事人名称")) {
                            tdText = tdText.replace(";", "；");//替换英文分号
                            tdText = tdText.replace(":", "：");//替换英文冒号
                            tdText = tdText.replace(",", "，");
                            tdText = tdText.replace("，", "；");
                            tdText = tdText.replace(" ", "；");//将空格替换为逗号
                            tdText = tdText.replace("统一社会信用代码：", "；统一社会信用代码：");//前面加一个分隔符
                            tdText = tdText.replace("法定代表人：", "；法定代表人：");//前面加一个分隔符
                            List<String> tdList = StrUtil.splitTrim(tdText, "；");
                            for (String nameCode : tdList) {
                                int beginIndex = nameCode.indexOf("：") + 1;
                                if (beginIndex == -1) {
                                    beginIndex = 0;
                                }
                                String text = nameCode.substring(beginIndex);//字符串截取
                                //设置企业或个人名称
                                if (StrUtil.isEmpty(adminPunish.getObjectType()) && StrUtil.isEmpty(adminPunish.getEnterpriseName()) && StrUtil.isEmpty(adminPunish.getPersonName())) {
                                    if (text.length() > 6) {//企业
                                        adminPunish.setObjectType("01");
                                        adminPunish.setEnterpriseName(text);
                                    } else {//个人
                                        adminPunish.setObjectType("02");
                                        adminPunish.setPersonName(text);
                                    }
                                    continue;
                                }
                                if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && (nameCode.contains("统一社会信用代码") || Pattern.matches("^[A-Za-z0-9（）()/\\-]{18,}$", text))) {
                                    text = text.replace("统一社会信用代码", "");
                                    adminPunish.setEnterpriseCode1(text);
                                    continue;
                                }
                                if (StrUtil.isEmpty(adminPunish.getEnterpriseCode2()) && (nameCode.contains("营业执照注册号") || Pattern.matches("^[A-Za-z0-9\\-]{15,}$", text))) {
                                    text = text.replace("营业执照注册号", "");
                                    adminPunish.setEnterpriseCode1(text);
                                    continue;
                                }
                                if (StrUtil.isEmpty(adminPunish.getPersonName()) && (nameCode.contains("法定代表人") || isContainChinese(text))) {
                                    text = text.replace("法定代表人", "");
                                    adminPunish.setPersonName(text);
                                }
                            }
                        } else if ("主要违法事实".equals(colTitle)) {
                            adminPunish.setPunishReason(tdText);
                        } else if ("行政处罚依据".equals(colTitle)) {
                            adminPunish.setPunishAccording(tdText);
                        } else if ("行政处罚决定".equals(colTitle)) {
                            adminPunish.setPunishResult(tdText);
                        } else if ("作出行政处罚决定的海关名称".equals(colTitle)) {
                            adminPunish.setJudgeAuth(tdText);
                        } else if ("救济渠道".equals(colTitle)) {

                        } else if ("行政处罚作出日期".equals(colTitle)) {
                            adminPunish.setJudgeDate(tdText.replace(" ", ""));
                        }
                    }
                    adminPunish.setUniqueKey(getUniqueKey(adminPunish));//设置UniqueKey
                    if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getEnterpriseName()) || StrUtil.isNotEmpty(adminPunish.getPersonName()))) {
                        if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {//插入不存在的数据
                            adminPunishMapper.insert(adminPunish);
                        } else {
                            log.info("此条记录已存在，不需要入库！");
                        }
                    } else {
                        log.warn("获取被处罚对象名称失败，数据不入库！adminPunish={}", adminPunish.toString());
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析url={}页面数据失败", map.get("sourceUrl"), e);
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

    /**
     * 判断字符串是否含有中文
     *
     * @param str
     * @return
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
