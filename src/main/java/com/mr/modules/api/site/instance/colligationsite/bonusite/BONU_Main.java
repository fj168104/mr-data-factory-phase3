package com.mr.modules.api.site.instance.colligationsite.bonusite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * 站点：国统局（总局）
 * url：http://www.stats.gov.cn/tjfw/sxqygs/gsxx/
 * 主题： 统计上严重失信企业
 * 属性：行政处罚决定书文号、处罚名称、处罚类别1、处罚类别2、处罚事由、处罚依据、行政相对人名称、行政相对人代码1、政相对人代码2、政相对人代码3、政相对人代码4、政相对人代码5、处罚结果、处罚决定日期、公示期限、处罚机关、当前状态、地方编码
 * 提取：TODO 违规主体名称 发布日期  查处机构,行政处罚文书文号
 *
 * @author pxu 2018-08-28
 */
@Slf4j
@Component("bonu_main")
@Scope("prototype")
public class BONU_Main extends SiteTaskExtend_CollgationSite {
    private String source = "国统局（总局）";
    private String subject = "统计上严重失信企业";
    private String indexUrl = "http://www.stats.gov.cn/tjfw/sxqygs/gsxx/";
    private String hashKey_prefix = OCRUtil.DOWNLOAD_DIR + File.separator + "bonu_main" + File.separator;

    @Override
    protected String execute() throws Throwable {
        Document doc = Jsoup.connect(indexUrl).execute().parse();
        Element ul = doc.getElementsByClass("center_list_contlist").first();
        if (ul != null) {
            Elements as = ul.getElementsByTag("a");
            for (Element a : as) {
                if (a.getElementsByClass("cont_tit").size() == 0) {//不是有效数据的，跳过
                    continue;
                }
                String url = indexUrl + a.attr("href").replace("./", "");//每页详情URL
                String title = a.text();
                String hashKey = hashKey_prefix + MD5Util.encode(url);

                WebClient webClient = CrawlerUtil.createDefaultWebClient();
                HtmlPage htmlPage = webClient.getPage(url);
                if (htmlPage.getWebResponse().getStatusCode() == 404) {
                    log.warn("访问的url不存在,url={}", url);
                    continue;
                }
                HtmlElement htmlElement = htmlPage.getFirstByXPath("//div[@class='center']");
                if (scrapyDataMapper.selectCountByUrl(url) == 0) {//库中不存在，插入数据
                    //下载文件
                    saveFile(htmlPage, title + ".html", hashKey);
                    ScrapyData scrapyData = createScrapyDataObject();
                    scrapyData.setUrl(url);
                    scrapyData.setHashKey(hashKey);
                    scrapyData.setHtml(htmlElement.asXml());
                    scrapyData.setText(htmlElement.asText());
                    scrapyDataMapper.insert(scrapyData);
                    webClient.close();
                } else {
                    log.info("[ScrapyData]此记录已存在，不需要入库！");
                }
                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setUrl(url);// url
                //获取发布时间
                HtmlElement dateElement = htmlElement.getFirstByXPath("//font[@class='xilan_titf']");
                if (dateElement != null) {
                    String text = dateElement.asText().trim();
                    int beginIndex = text.indexOf("发布时间：");
                    int endIndex = text.indexOf(" ");
                    if (endIndex == -1 || endIndex <= beginIndex) {
                        endIndex = text.length();
                    }
                    if (beginIndex != -1 && endIndex > beginIndex + 5) {
                        adminPunish.setPublishDate(text.substring(beginIndex + 5, endIndex));// 发布日期
                    }
                }
                HtmlTable table = (HtmlTable) htmlElement.getElementsByTagName("table").get(0);
                for (HtmlTableRow row : table.getRows()) {
                    List<HtmlTableCell> cells = row.getCells();
                    if (cells.size() == 2) {//每行两个单元格
                        String colName = cells.get(0).asText();
                        String colValue = cells.get(1).asText();
                        if (colName.contains("行政处罚决定书文号")) {
                            adminPunish.setJudgeNo(colValue);
                            continue;
                        }
                        if (colName.contains("处罚名称")) {
                            continue;
                        }
                        if (colName.contains("处罚类别1")) {
                            adminPunish.setPunishType(colValue);
                            continue;
                        }
                        if (colName.contains("处罚类别2")) {
                            if (StrUtil.isNotBlank(colValue)) {
                                adminPunish.setPunishType(adminPunish.getPunishType() + "、" + colValue);
                            }
                            continue;
                        }
                        if (colName.contains("处罚事由")) {
                            adminPunish.setPunishReason(colValue);
                            continue;
                        }
                        if (colName.contains("处罚依据")) {
                            adminPunish.setPunishAccording(colValue);
                            continue;
                        }
                        if (colName.contains("行政相对人名称")) {
                            if (colValue.length() > 6) {
                                adminPunish.setObjectType("01");
                                adminPunish.setEnterpriseName(colValue);
                            } else {
                                adminPunish.setObjectType("02");
                                adminPunish.setPersonName(colValue);
                            }
                            continue;
                        }
                        if (colName.contains("统一社会信用代码")) {
                            adminPunish.setEnterpriseCode1(colValue);
                            continue;
                        }
                        if (colName.contains("组织 机构代码")) {
                            adminPunish.setEnterpriseCode3(colValue);
                            continue;
                        }
                        if (colName.contains("工商登记码")) {
                            adminPunish.setEnterpriseCode2(colValue);
                            continue;
                        }
                        if (colName.contains("税务登记号")) {
                            adminPunish.setEnterpriseCode4(colValue);
                            continue;
                        }
                        if (colName.contains("居民身份证号")) {
                            adminPunish.setPersonId(colValue);
                            continue;
                        }
                        if (colName.contains("处罚结果")) {
                            adminPunish.setPunishResult(colValue);
                            continue;
                        }
                        if (colName.contains("处罚决定日期")) {
                            adminPunish.setJudgeDate(colValue);
                            continue;
                        }
                        if (colName.contains("公示期限")) {
                            continue;
                        }
                        if (colName.contains("处罚机关")) {
                            adminPunish.setJudgeAuth(colValue);
                            continue;
                        }
                        if (colName.contains("当前状态")) {
                            adminPunish.setStatus(colValue);
                            continue;
                        }
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
            }
        }
        return null;
    }

    private String getUniqueKey(AdminPunish adminPunish) {
        return MD5Util.encode(adminPunish.getUrl() + adminPunish.getJudgeNo() + adminPunish.getObjectType() + adminPunish.getEnterpriseName() + adminPunish.getPersonName());
    }

    private AdminPunish createAdminPunish() {
        AdminPunish adminPunish = new AdminPunish();
        Date now = new Date();
        adminPunish.setCreatedAt(now);//本条记录创建时间
        adminPunish.setUpdatedAt(now);// 本条记录最后更新时间
        adminPunish.setSource(source);// 数据来源
        adminPunish.setSubject(subject);// 主题
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
}
