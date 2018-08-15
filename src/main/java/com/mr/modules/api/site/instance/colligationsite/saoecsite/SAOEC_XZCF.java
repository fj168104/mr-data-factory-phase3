package com.mr.modules.api.site.instance.colligationsite.saoecsite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * 站点：国家外汇管理局网站
 * url：http://www.safe.gov.cn/wps/portal/sy/xxgk_whzfxx_whwfxxcx
 * 主题： 外汇行政处罚信息查询
 * 属性：违规主体名称、注册地、机构代码、查处机构、行政处罚文书文号、违规行为、处罚依据、处罚时间、处罚内容、行政处罚变更情况
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 */
@Slf4j
@Component("saoec_xzcf")
@Scope("prototype")
public class SAOEC_XZCF extends SiteTaskExtend_CollgationSite {
    private String url = "http://www.safe.gov.cn/SafeEscapeMessage/view/safeIllegal_lookQuery.action";
    private String publishDate = "";
    private String source = "国家外汇管理局网站";
    private String subject = " 外汇行政处罚信息查询";
    private String hashKey = OCRUtil.DOWNLOAD_DIR + File.separator + "saoec_xzcf" + File.separator + MD5Util.encode(url);

    private static boolean isNeedInsertScrapy = true;

    @Override
    protected String execute() throws Throwable {
        log.info("抓取“国家外汇管理局-行政处罚信息”信息开始...");
        //获取参数-关键字
        String keyWord = SiteParams.map.get("keyWord");
        if (StrUtil.isEmpty(keyWord)) {
            log.warn("[国家外汇管理局网站-行政处罚信息查询]查询关键字为空，不能进行网页抓取");
            return null;
        }
        //初始化scrapyData表
        if (isNeedInsertScrapy) {
            ScrapyData scrapy = createScrapyDataObject();
            scrapyDataMapper.deleteAllByUrl(url);
            scrapyDataMapper.insert(scrapy);
            isNeedInsertScrapy = false;
        }
        //keyWord = URLEncoder.encode(keyWord, "UTF-8");// 对关键字进行UTF-8编码
        WebClient webClient = CrawlerUtil.createDefaultWebClient();
        HtmlPage page = webClient.getPage(url);
        //log.debug(page.asXml());
        HtmlForm form = page.getFormByName("lookQuery");
        HtmlTextInput txtOrgNo = form.getInputByName("safeIllegalBean.irregularityNo"); //组织机构代码查询输入框
        HtmlTextInput txtOrgName = form.getInputByName("safeIllegalBean.irregularityName");//组织机构名称查询输入框
        if (isOrganizeCode(keyWord)) {//关键字为组织机构代码
            txtOrgNo.setValueAttribute(keyWord);
        } else {//关键字为组织机构名称
            txtOrgName.setValueAttribute(keyWord);
        }
        HtmlButtonInput button = form.querySelector("input[type=\"button\"]");
        page = button.click();//执行查询
        //log.debug(page.asXml());
        DomElement errorElement = page.getElementById("showerror");
        if (errorElement == null) {//查询到了结果
            HtmlTable table = (HtmlTable) page.getElementsByTagName("table").get(1);
            //log.debug("table:" + table.asXml());
            AdminPunish adminPunish = createAdminPunish();
            for (int i = 1; i < table.getRowCount(); i++) {//跳过标题行，从数据行开始遍历
                //table.getCellAt(i, 0).asText();//序号
                adminPunish.setEnterpriseName(table.getCellAt(i, 1).asText());//违规主体名称
                //table.getCellAt(i, 2).asText();//注册地
                adminPunish.setEnterpriseCode3(table.getCellAt(i, 3).asText());//机构代码
                adminPunish.setJudgeAuth(table.getCellAt(i, 4).asText());//查处机构
                adminPunish.setJudgeNo(table.getCellAt(i, 5).asText());//行政处罚文书文号
                adminPunish.setPunishReason(table.getCellAt(i, 6).asText());//违规行为
                adminPunish.setPunishAccording(table.getCellAt(i, 7).asText());//处罚依据
                adminPunish.setJudgeDate(table.getCellAt(i, 8).asText());//处罚时间
                adminPunish.setPunishResult(table.getCellAt(i, 9).asText());//处罚内容
                //table.getCellAt(i, 10).asText();//处罚金额（单位：万元人民币）
                //table.getCellAt(i, 11).asText();//行政处罚变更情况
                //table.getCellAt(i, 12).asText();//备注
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                //adminPunishMapper.
            }
            //记录不存在时才插入(增量插入)
            if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {
                adminPunishMapper.insert(adminPunish);
            }
        } else {//没有查询到结果
            log.debug(errorElement.asXml());
        }
        log.info("抓取“国家外汇管理局-行政处罚信息”信息结束!");
        return null;
    }

    private ScrapyData createScrapyDataObject() {
        ScrapyData scrapy = new ScrapyData();
        scrapy.setUrl(url);
        scrapy.setSource(source);
        scrapy.setHashKey(hashKey);
        scrapy.setAttachmentType("");
        scrapy.setHtml("");
        scrapy.setText("");
        scrapy.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
        return scrapy;
    }

    private AdminPunish createAdminPunish() {
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUpdatedAt(new Date());// 本条记录最后更新时间
        adminPunish.setSource(source);// 数据来源
        adminPunish.setSubject(subject);// 主题
        adminPunish.setUniqueKey("");//唯一性标识(url+企业名称/自然人名称+发布时间+发布机构)
        adminPunish.setUrl(url);// url
        adminPunish.setObjectType("01");// 主体类型: 01-企业 02-个人。默认为企业
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
        adminPunish.setPublishDate(publishDate);// 发布日期
        adminPunish.setStatus("");// 当前状态
        return adminPunish;
    }

    /**
     * 拼接UK
     *
     * @param adminPunish
     * @return
     */
    private String getUniqueKey(AdminPunish adminPunish) {
        return adminPunish.getUrl() + adminPunish.getEnterpriseName() + adminPunish.getJudgeNo() + adminPunish.getJudgeDate() + adminPunish.getJudgeAuth();
    }

    /**
     * 校验传入的关键字是否可能为组织机构代码
     *
     * @param orgCode
     * @return
     */
    public static boolean isOrganizeCode(String orgCode) {
        if (StrUtil.isEmpty(orgCode)) {
            return false;
        }
        orgCode = orgCode.trim().replace("-", "");
        if (orgCode.length() != 9) {//组织机构代码为9位
            return false;
        }
        return Pattern.matches("[A-Za-z0-9]{9}", orgCode);
    }
}
