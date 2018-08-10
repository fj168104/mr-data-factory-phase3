package com.mr.modules.api.site.instance.colligationsite.sawssite;

import com.mr.common.OCRUtil;
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
 * url：http://old.chinasafety.gov.cn/yfyj/yhpg/201612/t20161214_173534.shtml
 * 主题：2016年第二批国家级生产经营单位 安全生产不良记录“黑名单”信息
 * 属性：公司名称、注册地址、注册号、违法行为
 * 提取：TODO 公司名称 发布日期  信息报送机关
 */
@Slf4j
@Component("saws_2016_second_bljlhmd")
@Scope("prototype")
public class SAWS_2016_Second_BLJLHMD extends SiteTaskExtend_CollgationSite {
    private String url = "http://old.chinasafety.gov.cn/yfyj/yhpg/201706/t20170621_169300.shtml";
    private String publishDate = "2016/12/14";
    private String source = "国家安全生产监督管理总局网站";
    private String subject = "2016年第二批国家级生产经营单位安全生产不良记录黑名单信息";
    private String hashKey = OCRUtil.DOWNLOAD_DIR + File.separator + "saws_2016_second_bljlhmd" + File.separator + MD5Util.encode(url);

    @Override
    protected String execute() throws Throwable {
        String htmlContent = getData(url);
        if (StrUtil.isEmpty(htmlContent)) {
            log.error("抓取{}页面内容失败");
            return null;
        }
        //写文件
        FileUtil.writeString(htmlContent, new File(hashKey + File.separator + "国务院安委办公布2016年第二批国家级生产经营单位安全生产不良记录黑名单信息.html"), "UTF-8");
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
                {"发生特别重大生产安全事故","重庆市永川区金山沟煤业有限责任公司","","500383002741125","","2016年10月31日，该企业发生瓦斯爆炸事故，造成33人死亡。目前，国务院事故调查组正在对该起事故进行调查处理"},
                {"发生特别重大生产安全事故","内蒙古自治区赤峰宝马矿业有限责任公司","","150000000000866","","2016年12月3日，该企业发生瓦斯爆炸事故，造成32人死亡。目前，国务院事故调查组正在对该起事故进行调查处理"},
                {"发生重大生产安全事故","吉煤集团通化矿业（集团）有限责任公司松树镇煤矿","91220625825611973F","","","2016年3月6日，该矿发生煤与瓦斯突出事故，造成12人死亡。目前，吉林煤矿安全监察局已对该矿处以150万元罚款"},
                {"发生重大生产安全事故","山西同煤集团同生公司安平煤业有限公司","41140000588541704W","","","2016年3月23日，发生顶板大面积垮落导致的瓦斯爆炸事故，造成20人死亡。目前，山西煤矿安全监察局已对该矿处以罚款1100万元"},
                {"发生重大生产安全事故","新疆莎车县天利煤矿","","650000050003517","","2016年4月3日，该矿发生顶板事故，造成10人死亡。目前，新疆煤矿安全监察局已吊销该矿煤矿安全生产许可证，并处以1339.4万元罚款，莎车县人民政府对该矿予以淘汰"},
                {"发生重大生产安全事故","陕西铜川市耀州区照金煤矿","91610000719756167Y","","","2016年4月25日，该矿发生水害事故，造成11人死亡。目前，陕西煤矿安全监察局对该矿处罚款150万元"},
                {"发生重大生产安全事故","宁夏林利煤炭有限公司","916400007508244125","","","2016年9月27日，该企业三号井发生瓦斯爆炸事故，造成20人死亡。目前，宁夏煤矿安全监察局正在对该起事故进行调查处理"},
                {"发生重大生产安全事故","黑龙江七台河市景有煤矿","","230000100051427","","2016年11月29日，该矿发生瓦斯爆炸事故，造成22人死亡。目前，黑龙江煤矿安全监察局正在对该起事故进行调查处理"},
                {"发生重大生产安全事故","湖北恩施州巴东县辛家煤矿有限责任公司","","422800000027891","","2016年12月5日，该企业发生瓦斯突出事故，造成11人死亡。目前，湖北煤矿安全监察局正在对该起事故进行调查处理"},
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
