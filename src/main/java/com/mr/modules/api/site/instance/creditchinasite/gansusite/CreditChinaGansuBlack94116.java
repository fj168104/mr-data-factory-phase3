package com.mr.modules.api.site.instance.creditchinasite.gansusite;

import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.model.DiscreditBlacklist;
import com.mr.modules.api.site.SiteTaskExtend_CreditChina;
import com.mr.modules.api.site.instance.creditchinasite.CreditChinaSite;

import lombok.extern.slf4j.Slf4j;

/**
 * 信用中国（甘肃）-“共筑诚信·德润金昌”金昌市诚信黑榜名单
 * <p>
 * http://www.gscredit.gov.cn/blackList/94116.jhtml
 *
 * @author pxu 2018年6月26日
 */
@Slf4j
@Component("creditchina-gansu-black-94116")
@Scope("prototype")
public class CreditChinaGansuBlack94116 extends SiteTaskExtend_CreditChina {
    private String url = CreditChinaSite.GANSU.getBaseUrl() + "/blackList/94116.jhtml";

    /**
     * 抓取页面数据
     */
    @Override
    protected String execute() throws Throwable {
        discreditBlacklistMapper.deleteAllByUrl(url);// 删除该URL下的全部数据
        log.info("开始抓取url={}", url);
        extractContent(url);
        log.info("抓取url={}结束！", url);
        return null;
    }

    /**
     * 抓取内容
     */
    public void extractContent(String url) throws Throwable {
        String contentHtml = getData(url);
        Document doc = Jsoup.parse(contentHtml);

        log.debug("==============================");
        Elements contents = doc.select("div.artical_content_wrap > div");

        DiscreditBlacklist discreditBlacklist = null;
        int itemIndex = 0;// 条目索引
        String subject = "";// 主题
        String judgeAuth = "";// 发布机关
        int iCount = 0;// 计数器
        for (Element content : contents) {
            String text = CrawlerUtil.replaceHtmlNbsp(content.text()).replace("　", " ").trim();// 替换&nbsp;和全角空格
            if (StrUtil.isEmpty(text)) {// 跳过空的div
                continue;
            }
            if (itemIndex == 0) {
                if (text.contains("一、市中级人民法院失信被执行人名单")) {
                    itemIndex = 1;
                    iCount = 0;
                    subject = "市中级人民法院诚信黑榜";
                    judgeAuth = "甘肃省金昌市中级人民法院";
                    continue;
                }
                if (text.contains("金昌市国税局黑榜企业名单（1家）")) {
                    itemIndex = 2;
                    iCount = 0;
                    subject = "市国税局黑榜企业";
                    judgeAuth = "甘肃省金昌市国税局";
                    continue;
                }
                if (text.contains("二、金昌市地税局黑榜企业名单（4家）")) {
                    itemIndex = 3;
                    iCount = 0;
                    subject = "市地税局黑榜企业";
                    judgeAuth = "甘肃省金昌市地税局";
                    continue;
                }
                continue;
            }

            // 一、市中级人民法院失信被执行人名单
            if (itemIndex == 1) {
                if (text.contains("惩戒措施：")) {// 处理结束,重新判断itemIndex
                    itemIndex = 0;
                    continue;
                }
                if (text.contains("法人单位（2家）：")) {
                    continue;
                }
                iCount++;
                discreditBlacklist = createDefaultDiscreditBlacklist();
                discreditBlacklist.setSubject(subject);
                discreditBlacklist.setJudgeAuth(judgeAuth);
                discreditBlacklist.setEnterpriseName(text);
                discreditBlacklist.setDiscreditType("失信被执行人");
                String punishResult = "1.禁止部分高消费行为，包括外出禁止乘坐飞机、列车软卧；"//
                        + "2.实施信用惩戒，包括限制在金融机构贷款或办理信用卡；"//
                        + "3.失信被执行人为自然人的，不得担任企业的法定代表人、董事、监事、高级管理人员等。";
                discreditBlacklist.setPunishResult(punishResult);
                discreditBlacklist.setUniqueKey(discreditBlacklist.getUrl() + "@" + discreditBlacklist.getEnterpriseName() + "@" + discreditBlacklist.getPersonName() + "@" + discreditBlacklist.getJudgeNo() + "@" + discreditBlacklist.getJudgeAuth());
                discreditBlacklistMapper.insert(discreditBlacklist);
                discreditBlacklist = null;
                continue;
            }
            // 金昌市国税局黑榜企业名单（1家）
            if (itemIndex == 2) {
                if (text.contains("惩戒措施：")) {// 处理结束,重新判断itemIndex
                    itemIndex = 0;
                    continue;
                }
                iCount++;
                discreditBlacklist = createDefaultDiscreditBlacklist();
                discreditBlacklist.setSubject(subject);
                discreditBlacklist.setJudgeAuth(judgeAuth);
                discreditBlacklist.setEnterpriseName(text);
                discreditBlacklist.setDiscreditType("国税局黑榜");
                String punishResult = "依法将失信企业纳税信用等级直接判定为D级。"//
                        + "（1）公开其直接责任人员名单，对直接责任人注册等级或者负责经营的其他纳税人纳税信用直接判为D级；"//
                        + "（2）普通发票的领用实行交（验）旧供新，严格限量供应。" //
                        + "（3）将纳税信用评价结果通报相关部门，按照法律法规等有关规定，在经营、投融资、取得政府供应土地、进出口、出入境、注册新公司、工程招投标、政府采购、获得荣誉、安全许可、生产许可、从业任职资格、资质审核等方面予以限制或禁止。";
                discreditBlacklist.setPunishResult(punishResult);
                discreditBlacklistMapper.insert(discreditBlacklist);
                discreditBlacklist.setUniqueKey(discreditBlacklist.getUrl() + "@" + discreditBlacklist.getEnterpriseName() + "@" + discreditBlacklist.getPersonName() + "@" + discreditBlacklist.getJudgeNo() + "@" + discreditBlacklist.getJudgeAuth());
                discreditBlacklist = null;
                continue;
            }
            // 二、金昌市地税局黑榜企业名单（4家）
            if (itemIndex == 3) {
                if (text.contains("惩戒措施：")) {// 处理结束,重新判断itemIndex
                    itemIndex = 0;
                    continue;
                }
                iCount++;
                discreditBlacklist = createDefaultDiscreditBlacklist();
                discreditBlacklist.setSubject(subject);
                discreditBlacklist.setJudgeAuth(judgeAuth);
                discreditBlacklist.setEnterpriseName(text);
                discreditBlacklist.setDiscreditType("地税局黑榜");
                String punishResult = "依法将失信企业纳税信用等级直接判定为D级。"//
                        + "（1）公开其直接责任人员名单，对直接责任人注册等级或者负责经营的其他纳税人纳税信用直接判为D级；"//
                        + "（2）普通发票的领用实行交（验）旧供新，严格限量供应。"//
                        + "（3）将纳税信用评价结果通报相关部门，按照法律法规等有关规定，在经营、投融资、取得政府供应土地、进出口、出入境、注册新公司、工程招投标、政府采购、获得荣誉、安全许可、生产许可、从业任职资格、资质审核等方面予以限制或禁止。";
                discreditBlacklist.setPunishResult(punishResult);
                discreditBlacklist.setUniqueKey(discreditBlacklist.getUrl() + "@" + discreditBlacklist.getEnterpriseName() + "@" + discreditBlacklist.getPersonName() + "@" + discreditBlacklist.getJudgeNo() + "@" + discreditBlacklist.getJudgeAuth());
                discreditBlacklistMapper.insert(discreditBlacklist);
                discreditBlacklist = null;
                continue;
            }
        }
        log.debug("==============================");

    }

    private DiscreditBlacklist createDefaultDiscreditBlacklist() {
        Date nowDate = new Date();
        DiscreditBlacklist blackList = new DiscreditBlacklist();
        blackList.setCreatedAt(nowDate);// 本条记录创建时间
        blackList.setUpdatedAt(nowDate);// 本条记录最后更新时间
        blackList.setSource(CreditChinaSite.GANSU.getSiteName());// 数据来源
        blackList.setSubject("");// 主题
        blackList.setUrl(url);// url
        blackList.setObjectType("01");// 主体类型: 01-企业 02-个人。默认为企业
        blackList.setEnterpriseName("");// 企业名称
        blackList.setEnterpriseCode1("");// 统一社会信用代码
        blackList.setEnterpriseCode2("");// 营业执照注册号
        blackList.setEnterpriseCode3("");// 组织机构代码
        blackList.setEnterpriseCode4("");// 税务登记号
        blackList.setPersonName("");// 法定代表人/负责人姓名|负责人姓名
        blackList.setPersonId("");// 法定代表人身份证号|负责人身份证号
        blackList.setDiscreditType("");// 失信类型
        blackList.setDiscreditAction("");// 失信行为
        blackList.setPunishReason("");// 列入原因
        blackList.setPunishResult("");// 处罚结果
        blackList.setJudgeNo("");// 执行文号
        blackList.setJudgeDate("");// 执行时间
        blackList.setJudgeAuth("");// 判决机关
        blackList.setPublishDate("2015/10/26");// 发布日期
        blackList.setStatus("");// 当前状态
        return blackList;
    }
}
