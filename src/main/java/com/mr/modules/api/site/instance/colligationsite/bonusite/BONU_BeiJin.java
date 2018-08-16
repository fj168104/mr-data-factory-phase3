package com.mr.modules.api.site.instance.colligationsite.bonusite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.common.util.WordUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 北京市统计局
 * 主题：行政处罚结果信息公示
 * 提取：TODO 处罚决定书文号、执法依据、案件名称、行政相对人名称、统一社会信用代码、处罚事由、决定部门、处罚结果
 *
 * @author pxu 2018/08/15
 */
@Slf4j
@Component("bonu_beijing")
@Scope("prototype")
public class BONU_BeiJin extends SiteTaskExtend_CollgationSite {
    private String source = "国统局（北京）";
    private String subject = "行政处罚结果信息公示";
    private String hashKey_prefix = OCRUtil.DOWNLOAD_DIR + File.separator + "bonu_beiJing" + File.separator;

    @Override
    protected String execute() throws Throwable {
        log.info("开始处理北京统计局-行政处罚结果信息公示...");
        try {
            xzcf_2017_03("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201801/t20180110_390619.html");
        } catch (Exception e) {
            log.error("抓取2017年北京市统计局行政处罚结果信息公示（第三批）信息失败", e);
        }
        try {
            xzcf_2017_02("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201712/t20171219_389153.html");
        } catch (Exception e) {
            log.error("抓取2017年北京市统计局行政处罚结果信息公示（第二批）信息失败", e);
        }
        try {
            xzcf_2017_01("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201711/t20171120_387589.html");
        } catch (Exception e) {
            log.error("抓取2017年北京市统计局行政处罚结果信息公示（第一批）信息失败", e);
        }
        try {
            xzcf_2017("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201701/P020170119397863763212.doc");
        } catch (Exception e) {
            log.error("抓取2017年北京市统计局行政处罚结果信息公示信息失败", e);
        }
        try {
            xzcf_2016("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201612/P020161220605362312432.doc");
        } catch (Exception e) {
            log.error("抓取2016年北京市统计局行政处罚结果信息公示信息失败", e);
        }
        log.info("处理北京统计局-行政处罚结果信息公示结束！");
        return "";
    }

    /**
     * 2017年北京市统计局行政处罚结果信息公示（第三批）
     */
    private void xzcf_2017_03(String url) throws Exception {
        String hashKey = hashKey_prefix + MD5Util.encode(url);
        if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(url);
            scrapyData.setHashKey(hashKey);

            WebClient webClient = CrawlerUtil.createDefaultWebClient();
            //保存网页
            HtmlPage htmpPage = webClient.getPage(url);
            saveFile(htmpPage, "2017年北京市统计局行政处罚结果信息公示（第三批）.html", hashKey);
            HtmlDivision division = (HtmlDivision) htmpPage.getByXPath("//div[@class='cf_tj_nei']").get(0);
            scrapyData.setHtml(division.asXml());
            scrapyData.setText(division.asText());
            //保存附件
            saveFile(webClient.getPage("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201801/P020180622589239427533.docx"), "附件：2017年北京市统计局行政处罚结果信息公示（第三批）.docx", hashKey);
            scrapyData.setAttachmentType("docx");
            scrapyDataMapper.insert(scrapyData);
            webClient.close();
        }
        //解析doc文件
        String filePath = hashKey + File.separator + "附件：2017年北京市统计局行政处罚结果信息公示（第三批）.docx";
        if (!new File(filePath).exists()) {
            log.warn("文件{}不存在", filePath);
            return;
        }
        List<List<List<String>>> tableList = WordUtil.readWordTable(filePath);
        //读取表格数据
        for (List<List<String>> table : tableList) {
            //读取每行数据
            for (List<String> row : table) {
                if ("处罚决定书文号".equals(row.get(0))) {//跳过标题行
                    continue;
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);
                adminPunish.setJudgeNo(row.get(0));//处罚决定书文号
                //row.get(1);//案件名称
                adminPunish.setEnterpriseName(row.get(2));//行政相对人名称
                if (!"*".equals(row.get(3))) {
                    adminPunish.setEnterpriseCode1(row.get(3));//统一社会信用代码
                }
                adminPunish.setPunishReason(row.get(4));//处罚事由
                adminPunish.setPunishResult(row.get(5));//处罚结果
                adminPunish.setPunishAccording("《中华人民共和国统计法》");//执法依据
                adminPunish.setJudgeAuth("北京市统计局"); //决定部门
                adminPunish.setPublishDate("2018-01-10");
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在时插入数据
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }
    }

    /**
     * 2017年北京市统计局行政处罚结果信息公示（第二批）
     */
    private void xzcf_2017_02(String url) throws Exception {
        String hashKey = hashKey_prefix + MD5Util.encode(url);
        if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(url);
            scrapyData.setHashKey(hashKey);

            WebClient webClient = CrawlerUtil.createDefaultWebClient();
            //保存网页
            HtmlPage htmpPage = webClient.getPage(url);
            saveFile(htmpPage, "2017年北京市统计局行政处罚结果信息公示（第二批）.html", hashKey);
            HtmlDivision division = (HtmlDivision) htmpPage.getByXPath("//div[@class='cf_tj_nei']").get(0);
            scrapyData.setHtml(division.asXml());
            scrapyData.setText(division.asText());
            //保存附件
            saveFile(webClient.getPage("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201712/P020171219396981534926.docx"), "附件：2017年北京市统计局行政处罚结果信息公示（第二批）.docx", hashKey);
            scrapyData.setAttachmentType("docx");
            scrapyDataMapper.insert(scrapyData);
            webClient.close();
        }
        //解析doc文件
        String filePath = hashKey + File.separator + "附件：2017年北京市统计局行政处罚结果信息公示（第二批）.docx";
        if (!new File(filePath).exists()) {
            log.warn("文件{}不存在", filePath);
            return;
        }
        List<List<List<String>>> tableList = WordUtil.readWordTable(filePath);
        //读取表格数据
        for (List<List<String>> table : tableList) {
            //读取每行数据
            for (List<String> row : table) {
                if ("处罚决定书文号".equals(row.get(0))) {//跳过标题行
                    continue;
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);
                adminPunish.setJudgeNo(row.get(0));//处罚决定书文号
                //row.get(1);//案件名称
                adminPunish.setEnterpriseName(row.get(2));//行政相对人名称
                if (!"*".equals(row.get(3))) {
                    adminPunish.setEnterpriseCode1(row.get(3));//统一社会信用代码
                }
                adminPunish.setPunishReason(row.get(4));//处罚事由
                adminPunish.setPunishResult(row.get(5));//处罚结果
                adminPunish.setPunishAccording("《中华人民共和国统计法》");//执法依据
                adminPunish.setJudgeAuth("北京市统计局"); //决定部门
                adminPunish.setPublishDate("2017-12-19");
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在时插入数据
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }
    }

    /**
     * 2017年北京市统计局行政处罚结果信息公示（第一批）
     */
    private void xzcf_2017_01(String url) throws Exception {
        String hashKey = hashKey_prefix + MD5Util.encode(url);
        if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(url);
            scrapyData.setHashKey(hashKey);

            WebClient webClient = CrawlerUtil.createDefaultWebClient();
            //保存网页
            HtmlPage htmpPage = webClient.getPage(url);
            saveFile(htmpPage, "2017年北京市统计局行政处罚结果信息公示（第一批）.html", hashKey);
            HtmlDivision division = (HtmlDivision) htmpPage.getByXPath("//div[@class='cf_tj_nei']").get(0);
            scrapyData.setHtml(division.asXml());
            scrapyData.setText(division.asText());
            //保存附件
            saveFile(webClient.getPage("http://tjj.beijing.gov.cn/zl/tjxxgs/cfjgxxgs/201711/P020171120360914256542.docx"), "附件：2017年北京市统计局行政处罚结果信息公示（第一批）.docx", hashKey);
            scrapyData.setAttachmentType("docx");
            scrapyDataMapper.insert(scrapyData);
            webClient.close();
        }
        //解析doc文件
        String filePath = hashKey + File.separator + "附件：2017年北京市统计局行政处罚结果信息公示（第一批）.docx";
        if (!new File(filePath).exists()) {
            log.warn("文件{}不存在", filePath);
            return;
        }
        List<List<List<String>>> tableList = WordUtil.readWordTable(filePath);
        //读取表格数据
        for (List<List<String>> table : tableList) {
            //读取每行数据
            for (List<String> row : table) {
                if ("处罚决定书文号".equals(row.get(0))) {//跳过标题行
                    continue;
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);
                adminPunish.setJudgeNo(row.get(0));//处罚决定书文号
                //row.get(1);//案件名称
                adminPunish.setEnterpriseName(row.get(2));//行政相对人名称
                if (!"*".equals(row.get(3))) {
                    adminPunish.setEnterpriseCode1(row.get(3));//统一社会信用代码
                }
                adminPunish.setPunishResult(row.get(4));//处罚结果
                adminPunish.setPunishAccording("《中华人民共和国统计法》");//执法依据
                adminPunish.setJudgeAuth("北京市统计局"); //决定部门
                adminPunish.setPublishDate("2017-11-20");
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在时插入数据
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }
    }

    /**
     * 2017年北京市统计局行政处罚结果信息公示
     */
    private void xzcf_2017(String url) throws Exception {
        String hashKey = hashKey_prefix + MD5Util.encode(url);
        if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
            WebClient webClient = CrawlerUtil.createDefaultWebClient();
            //下载文件
            saveFile(webClient.getPage(url), "2017年北京市统计局行政处罚结果信息公示.doc", hashKey);
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(url);
            scrapyData.setHashKey(hashKey);
            scrapyData.setAttachmentType("doc");
            scrapyDataMapper.insert(scrapyData);
            webClient.close();
        }
        //解析doc文件
        String filePath = hashKey + File.separator + "2017年北京市统计局行政处罚结果信息公示.doc";
        if (!new File(filePath).exists()) {
            log.warn("文件{}不存在", filePath);
            return;
        }
        List<List<List<String>>> tableList = WordUtil.readWordTable(filePath);
        //读取表格数据
        for (List<List<String>> table : tableList) {
            //读取每行数据
            for (List<String> row : table) {
                if ("处罚决定书文号".equals(row.get(0))) {//跳过标题行
                    continue;
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);
                adminPunish.setJudgeNo(row.get(0));//处罚决定书文号
                adminPunish.setPunishAccording(row.get(1));//执法依据
                //row.get(2);//案件名称
                adminPunish.setEnterpriseName(row.get(3));//行政相对人名称
                adminPunish.setEnterpriseCode1(row.get(4));//统一社会信用代码
                adminPunish.setPunishReason(row.get(5));//处罚事由
                adminPunish.setJudgeAuth(row.get(6)); //决定部门
                adminPunish.setPunishResult(row.get(7));//处罚结果
                adminPunish.setPublishDate("2017-01-19");
                //row.get(8);//救济渠道
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在时插入数据
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }
    }

    /**
     * 2016年北京市统计局行政处罚结果信息公示
     */
    private void xzcf_2016(String url) throws Exception {
        String hashKey = hashKey_prefix + MD5Util.encode(url);
        if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
            WebClient webClient = CrawlerUtil.createDefaultWebClient();
            //下载文件
            saveFile(webClient.getPage(url), "2016年北京市统计局行政处罚结果信息公示.doc", hashKey);
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(url);
            scrapyData.setHashKey(hashKey);
            scrapyData.setAttachmentType("doc");
            scrapyDataMapper.insert(scrapyData);
            webClient.close();
        }
        //解析doc文件
        String filePath = hashKey + File.separator + "2016年北京市统计局行政处罚结果信息公示.doc";
        if (!new File(filePath).exists()) {
            log.warn("文件{}不存在", filePath);
            return;
        }
        List<List<List<String>>> tableList = WordUtil.readWordTable(filePath);
        //读取表格数据
        for (List<List<String>> table : tableList) {
            //读取每行数据
            for (List<String> row : table) {
                if ("处罚决定书文号".equals(row.get(0))) {//跳过标题行
                    continue;
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);
                adminPunish.setJudgeNo(row.get(0));//处罚决定书文号
                adminPunish.setPunishAccording(row.get(1));//执法依据
                //row.get(2);//案件名称
                adminPunish.setEnterpriseName(row.get(3));//行政相对人名称
                adminPunish.setEnterpriseCode1(row.get(4));//统一社会信用代码
                adminPunish.setPunishReason(row.get(5));//处罚事由
                adminPunish.setJudgeAuth(row.get(6)); //决定部门
                adminPunish.setPunishResult(row.get(7));//处罚结果
                adminPunish.setPublishDate("2016-12-20");
                //row.get(8);//救济渠道
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在时插入数据
                    adminPunishMapper.insert(adminPunish);
                }
            }
        }
    }

    private ScrapyData createScrapyDataObject() {
        ScrapyData scrapy = new ScrapyData();
        scrapy.setUrl("");
        scrapy.setSource(source);
        scrapy.setHashKey("");
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
        adminPunish.setUrl("");// url
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
        adminPunish.setPublishDate("");// 发布日期
        adminPunish.setStatus("");// 当前状态
        return adminPunish;
    }

    private String getUniqueKey(AdminPunish adminPunish) {
        return adminPunish.getUrl() + ("01".equals(adminPunish.getObjectType()) ? adminPunish.getEnterpriseName() : adminPunish.getPersonName()) + adminPunish.getJudgeNo() + adminPunish.getJudgeDate() + adminPunish.getJudgeAuth();
    }
}
