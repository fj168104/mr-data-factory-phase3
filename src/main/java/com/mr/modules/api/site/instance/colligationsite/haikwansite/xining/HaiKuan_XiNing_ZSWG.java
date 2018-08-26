package com.mr.modules.api.site.instance.colligationsite.haikwansite.xining;

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
 * 主题：西宁海关走私违规行政处罚
 * url:http://xining.customs.gov.cn/xining_customs/533860/533876/533878/533880/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * modify by pxu 2018-08-26:解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_xining_zswg")
public class HaiKuan_XiNing_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String baseUrl = "http://xining.customs.gov.cn";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "西宁海关走私违规行政处罚";
        String area = "xining";//区域为：西宁

        String url = "http://xining.customs.gov.cn/xining_customs/533860/533876/533878/533880/index.html";
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
    public void extractWebData(Map<String, String> map) {
        String url = map.get("sourceUrl");

        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("西宁海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(url);// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期

        String title = map.get("title");
        if (title != null && title.contains("关缉") && title.contains("号")) {
            title = title.replace("(", "（");
            title = title.replace(")", "）");
            int beginIndex = title.indexOf("（");
            int endIndex = title.lastIndexOf("）");
            if (beginIndex != -1 && endIndex != -1 && endIndex > (beginIndex + 1)) {
                String judgeNo = title.substring(beginIndex + 1, endIndex).trim();
                judgeNo = judgeNo.replace("〔", "[");
                judgeNo = judgeNo.replace("〕", "]");
                judgeNo = judgeNo.replace("【", "[");
                judgeNo = judgeNo.replace("】", "]");
                judgeNo = judgeNo.replace("［", "[");
                judgeNo = judgeNo.replace("］", "]");
                adminPunish.setJudgeNo(judgeNo);
            }
        }
        try {
            StringBuilder sb = new StringBuilder();
            Document doc = Jsoup.connect(url).execute().parse();
            Elements ps = doc.getElementById("easysiteText").getElementsByTag("p");
            for (Element p : ps) {
                String pText = p.text();
                sb.append(pText).append("\n");
                pText = pText.replace("　", " ");//全角空格替换为半角
                pText = pText.replace(":", "：");
                pText = pText.replace(";", "；");
                pText = pText.replace("，", "；");
                pText = pText.replace(",", "；");
                pText = pText.replace("。", "；");
                pText = pText.replaceAll("当[\\s]{0,3}事[\\s]{0,3}人(姓名)?(/)?(名称)?[\\s]{0,3}", "当事人");//替换当事人标识
                pText = pText.replace("法人代表", "法定代表人");
                pText = pText.replace("法定代表人为：", "法定代表人：");
                pText = pText.replace("法定代表人为", "法定代表人：");
                pText = pText.replaceAll("法定代表人：[\\s]{0,3}", "法定代表人：");
                if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && pText.contains("关缉") && pText.contains("号")) {//决定书文号
                    pText = pText.replace("〔", "[");
                    pText = pText.replace("〕", "]");
                    pText = pText.replace("【", "[");
                    pText = pText.replace("】", "]");
                    pText = pText.replace("［", "[");
                    pText = pText.replace("］", "]");
                    adminPunish.setJudgeNo(pText.trim());
                }
                //当事人
                if (StrUtil.isEmpty(adminPunish.getObjectType()) && (pText.contains("当事人："))) {
                    int index = pText.indexOf("当事人：");//判断开始标记
                    if (index == -1) {
                        index = 0;
                    } else {
                        index = index + 4;
                    }
                    String name = pText.substring(index);
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
                //法定代表人
                if (StrUtil.isEmpty(adminPunish.getPersonName()) && pText.contains("法定代表人：")) {
                    int index = pText.indexOf("法定代表人：");//判断开始标记
                    if (index == -1) {
                        index = 0;
                    } else {
                        index = index + 6;
                    }
                    String name = pText.substring(index);
                    index = name.indexOf("；");//判断结束标记
                    if (index == -1) {
                        index = name.length();
                    }
                    adminPunish.setPersonName(name.substring(0, index).trim());
                }
                //统一社会信用代码
                if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && pText.contains("统一社会信用代码：")) {
                    int index = pText.indexOf("统一社会信用代码：");//判断开始标记
                    if (index == -1) {
                        index = 0;
                    } else {
                        index = index + 9;
                    }
                    String name = pText.substring(index);
                    index = name.indexOf("；");//判断结束标记
                    if (index == -1) {
                        index = name.length();
                    }
                    adminPunish.setEnterpriseCode1(name.substring(0, index));
                }
            }
            adminPunish.setPunishReason(sb.toString().trim());//处罚事由
            // 设置UniqueKey
            adminPunish.setUniqueKey(getUniqueKey(adminPunish));
            //不存在则插入
            if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                adminPunishMapper.insert(adminPunish);
            } else {
                log.info("此条记录已存在，不需要入库！");
            }
        } catch (Throwable t) {
            log.error("解析网页数据失败,url={}", url, t);
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
