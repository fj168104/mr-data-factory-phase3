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
 * url：http://old.chinasafety.gov.cn/yfyj/yhpg/201706/t20170621_169300.shtml
 * 主题：2017年第一批国家级生产经营单位安全生产不良记录“黑名单”信息
 * 属性：公司名称、注册地址、注册号、违法行为
 * 提取：TODO 公司名称 发布日期  信息报送机关
 * 注：附件打不开
 */
@Slf4j
@Component("saws_2017_first_bljlhmd")
@Scope("prototype")
public class SAWS_2017_First_BLJLHMD extends SiteTaskExtend_CollgationSite {
    private String url = "http://old.chinasafety.gov.cn/yfyj/yhpg/201706/t20170621_169300.shtml";
    private String publishDate = "2016/06/21";
    private String source = "国家安全生产监督管理总局网站";
    private String subject = "2017年第一批国家级生产经营单位安全生产不良记录黑名单信息";
    private String hashKey = OCRUtil.DOWNLOAD_DIR + File.separator + "saws_2017_first_bljlhmd" + File.separator + MD5Util.encode(url);

    @Override
    protected String execute() throws Throwable {
        WebClient client=CrawlerUtil.createDefaultWebClient();
        client.getOptions().setJavaScriptEnabled(false);
        String htmlContent = CrawlerUtil.getHtmlPage(client,url);
        client.close();
        if (StrUtil.isEmpty(htmlContent)) {
            log.error("抓取{}页面内容失败");
            return null;
        }
        //写文件
        FileUtil.writeString(htmlContent, new File(hashKey + File.separator + "国务院安委办公布2017年第一批国家级生产经营单位安全生产不良记录黑名单信息.html"), "UTF-8");
        //解析html
        Document doc = Jsoup.parse(htmlContent);
        Element element = doc.getElementsByClass("maintxt").first();
        ScrapyData scrapy = createScrapyDataObject();
        if (element != null) {
            scrapy.setHtml(element.outerHtml());
            scrapy.setText(element.text());
        }
        scrapyDataMapper.deleteAllByUrl(url);
        scrapyDataMapper.insert(scrapy);

        //处罚类型，企业名称，统一社会信用代码，注册号，组织机构代码，处罚事由
        String[][] data = new String[][]{
                {"发生重大生产安全责任事故", "湖南省雄大西南建筑园林有限公司", "91431000582753597N", "", "", "由湖南省雄大西南建筑园林有限公司承建的郴州市苏仙区王仙岭景区公路景观提质工程，在结束现场施工返回项目部途中，项目负责人驾驶施工车辆严重违法违规搭载32名施工人员行驶至一长下坡右转急弯路段时翻坠，造成13人死亡，18人受伤，车辆及道路设施受损的重大生产安全责任事故。"},
                {"发生重大生产安全责任事故", "湖南省郴州市浩森林业工程监理有限公司", "91431003554904351U", "", "", "湖南省郴州市浩森林业工程监理有限公司使用假的监理资质，承担郴州市苏仙区王仙岭景区公路景观提质工程的监理工作，在结束现场施工返回项目部途中，项目负责人驾驶施工车辆严重违法违规搭载32名施工人员行驶至一长下坡右转急弯路段时翻坠，造成13人死亡，18人受伤，车辆及道路设施受损的重大生产安全责任事故。"},
                {"发生重大生产安全责任事故", "峨边东森物流有限责任公司", "", "", "69919233-2", "峨边东森物流有限责任公司对驾驶员招聘、教育、管理和车辆管理安全生产主体责任不落实，导致川L50862号重型罐式货车严重违法违规超载、超速行驶，发生重大生产安全责任事故，造成10人死亡，38人受伤。"},
                {"发生重大生产安全责任事故", "天台县足謦堂足浴中心", "", "331023620036759", "", "天台县足謦堂足浴中心内部管理混乱，消防安全责任和规章制度不落实，无视消防法律法规规定和竣工验收备案抽查整改要求，擅自将汗蒸房恢复功能投入使用，严重违法违规进行经营活动，导致发生重大火灾事故，造成18人死亡，18人受伤。"},
                {"发生重大生产安全责任事故", "涟源市斗笠山镇祖保煤矿", "", "430000000116646", "", "涟源市斗笠山镇祖保煤矿拒不执行监管部门停产指令，拉断绞车锁链，切断煤炭生产视频监控系统电源，逃避监管，严重违法违规擅自组织生产，导致发生重大生产安全责任事故，造成10人死亡，2人受伤。"},
                {"发生重大生产安全责任事故", "山西中煤担水沟煤业有限公司", "911400007259398751", "", "", "山西中煤担水沟煤业有限公司严重违法违规超能力生产，生产布局集中、接替顺序不合理、巷道压力明显增大时未采取有效措施，致使采动应力叠加，发生冒顶重大安全生产责任事故，造成10人死亡。"},
                {"发生重大生产安全责任事故", "登封市兴峪煤业有限公司", "91410185667227422X", "", "", "登封市兴峪煤业有限公司隐瞒矿井真实情况，不按突出矿井管理，逃避安全监管监察；不执行监管监察指令违规施工；安全投入不足，安全管理不到位；迟报事故，盲目施救，造成事故扩大。履行安全管理职责不到位，导致发生重大生产安全责任事故，造成12人死亡。"}
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
