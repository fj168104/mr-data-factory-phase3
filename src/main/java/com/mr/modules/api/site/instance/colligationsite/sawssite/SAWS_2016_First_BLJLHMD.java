package com.mr.modules.api.site.instance.colligationsite.sawssite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

/**
 * 站点：国家安全生产监督管理总局网站
 * url：http://old.chinasafety.gov.cn/yfyj/yhpg/201607/t20160722_173531.shtml
 * 主题：2016年第一批国家级生产经营单位 安全生产不良记录“黑名单”信息
 * 属性：公司名称、注册地址、注册号、违法行为
 * 提取：TODO 公司名称 发布日期  信息报送机关
 */
@Slf4j
@Component("saws_2016_first_bljlhmd")
@Scope("prototype")
public class SAWS_2016_First_BLJLHMD extends SiteTaskExtend_CollgationSite {
    private String url = "http://old.chinasafety.gov.cn/yfyj/yhpg/201607/t20160722_173531.shtml";
    private String publishDate = "2016/07/22";
    private String source = "国家安全生产监督管理总局网站";
    private String subject = "2016年第一批国家级生产经营单位安全生产不良记录黑名单信息";
    private String hashKey = OCRUtil.DOWNLOAD_DIR + File.separator + "saws_2016_first_bljlhmd" + File.separator + MD5Util.encode(url);

    @Override
    protected String execute() throws Throwable {
        WebClient client= CrawlerUtil.createDefaultWebClient();
        client.getOptions().setJavaScriptEnabled(false);
        String htmlContent = CrawlerUtil.getHtmlPage(client,url);
        client.close();
        if (StrUtil.isEmpty(htmlContent)) {
            log.error("抓取{}页面内容失败");
            return null;
        }
        //写文件
        FileUtil.writeString(htmlContent, new File(hashKey + File.separator + "国务院安委办公布2016年第一批国家级生产经营单位安全生产不良记录黑名单信息.html"), "UTF-8");
        //解析html
        Document doc = Jsoup.parse(htmlContent);
        Element element = doc.getElementById("nrBox");
        ScrapyData scrapy = createScrapyDataObject();
        if (element != null) {
            scrapy.setHtml(element.outerHtml());
            scrapy.setText(element.text());
        }
        scrapyDataMapper.deleteAllByUrl(url);
        scrapyDataMapper.insert(scrapy);

        //删除该URL下的全部数据
        adminPunishMapper.deleteAllByUrl(url);
        //处罚类型，企业名称，统一社会信用代码，注册号，组织机构代码，处罚事由
        String[][] data = new String[][]{
                {"发生重特大生产安全事故", "辽宁连山钼业集团兴利矿业有限公司", "", "211402004015816", "", "2015年12月17日，辽宁连山钼业集团兴利矿业有限公司井下发生重大火灾事故，造成17人死亡、17人受伤，直接经济损失2199.1万元。辽宁省安全生产监督管理局对该企业处以500万元罚款"},
                {"发生重特大生产安全事故", "河南省通许县通安烟花爆竹有限公司", "", "", "", "2016年1月14日上午10时48分左右，河南省开封市通许县通安烟花爆竹有限公司将老厂房出租给他人，在违法生产烟花爆竹（超规格双响类产品）过程中发生爆炸，造成10人死亡、7人受伤。该起事故正在调查处理中"},
                {"瞒报生产安全事故", "唐山燕山电力设备检修有限公司", "", "130293000015867", "", "2015年10月12日 ，唐山燕山电力设备检修公司在神华国能宁夏煤电有限公司鸳鸯湖电厂进行锅炉检修时违章作业，发生倾砸事故，造成1人死亡。事故发生后，施工单位未向建设单位和政府有关职能部门报告，导致事故存在瞒报行为"},
                {"存在其他严重违法违规行为", "山东省潍坊长兴化工有限公司", "", "", "", "违法出租生产装置和设备、不具备安全生产条件组织生产、现场安全管理缺失"},
                {"存在其他严重违法违规行为", "四川省金路树脂股份有限公司", "", "", "", "违规进入受限空间作业、教育培训缺失"},
                {"存在其他严重违法违规行为", "江西省上饶市广丰县鸿盛花炮制造有限公司", "", "", "", "多股东各自独立组织生产，库房超量储存"},
                {"存在其他严重违法违规行为", "广西区钦州市浦北县泉水烟花基地", "", "", "", "将闲置的工房出租给他人、违法组织生产组合烟花，并故意瞒报事故"},
                {"存在其他严重违法违规行为", "广西壮族自治区玉林市博白县龙潭炮竹厂", "", "", "", "违反停产整顿指令组织生产、违规在称量工房内同时称量氧化剂和还原剂等"},
                {"存在其他严重违法违规行为", "广西壮族自治区玉林市博白县松旺烟花厂", "", "", "", "改变工房用途、私建工房、制度不落实、内部管理混乱"}
        };
        //删除该URL下的全部数据
        adminPunishMapper.deleteAllByUrl(url);
        for (int i = 0; i < data.length; i++) {
            AdminPunish adminPunish = createAdminPunish();
            adminPunish.setPunishType(data[i][0]);
            adminPunish.setEnterpriseName(data[i][1]);
            adminPunish.setEnterpriseCode1(data[i][2]);
            adminPunish.setEnterpriseCode2(data[i][3]);
            adminPunish.setEnterpriseCode3(data[i][4]);
            adminPunish.setPunishReason(data[i][5]);
            if ("唐山燕山电力设备检修有限公司".equals(adminPunish.getEnterpriseName())) {
                adminPunish.setPunishResult(adminPunish.getPunishResult() + ",宁夏回族自治区宁东能源化工基地管理委员会安全生产监督管理局对唐山燕山电力设备检修有限公司处以50万元罚款");
            }
            adminPunish.setUniqueKey(getUniqueKey(adminPunish));
            adminPunishMapper.insert(adminPunish);
        }

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
        adminPunish.setPunishResult("公开曝光，按规定纳入“黑名单”管理，并推送至全国信用信息共享平台，相关部门将对其开展联合惩戒");// 处罚结果
        adminPunish.setJudgeNo("");// 执行文号
        adminPunish.setJudgeDate("");// 执行时间
        adminPunish.setJudgeAuth("");// 判决机关
        adminPunish.setPublishDate(publishDate);// 发布日期
        adminPunish.setStatus("");// 当前状态
        return adminPunish;
    }

    private String getUniqueKey(AdminPunish adminPunish) {
        return adminPunish.getUrl() + ("01".equals(adminPunish.getObjectType()) ? adminPunish.getEnterpriseName() : adminPunish.getPersonName()) + adminPunish.getPublishDate() + adminPunish.getJudgeAuth();
    }
}
