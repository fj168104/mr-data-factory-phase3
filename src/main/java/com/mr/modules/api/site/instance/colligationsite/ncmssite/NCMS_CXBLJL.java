package com.mr.modules.api.site.instance.colligationsite.ncmssite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDefinitionDescription;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import com.mr.common.OCRUtil;
import com.mr.common.util.CrawlerUtil;
import com.mr.framework.core.thread.ThreadUtil;
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
 * 站点：全国建筑市场监管公共服务平台
 * url：http://jzsc.mohurd.gov.cn/asite/credit/record/query
 * 主题：个人不良诚信记录/企业不良诚信记录
 * 属性：诚信记录编号、诚信记录主题、处罚内容、处罚日期、处罚事由、处罚部门、处罚部门、出发文号、发布有效日期、统一社会信用代码、法人代表、企业注册地址、企业注册类型、企业经营地址、企业资质资格、注册人员
 * 提取：被处罚人姓名/企业名 公布日期、执法单位,文号
 *
 * @author pxu 2018-08-15
 */
@Slf4j
@Component("ncms_cxbljl")
@Scope("prototype")
public class NCMS_CXBLJL extends SiteTaskExtend_CollgationSite {
    private String indexUrl = "http://jzsc.mohurd.gov.cn/asite/credit/record/query";
    private String source = "全国建筑市场监管公共服务平台";

    @Override
    protected String execute() throws Throwable {
        WebClient webClient = CrawlerUtil.createDefaultWebClient();
        HtmlPage currentPage = null;
        for (int i = 0; i < 5; i++) {//重试访问主页
            currentPage = webClient.getPage(indexUrl);
            if (currentPage.getWebResponse().getStatusCode() == 200) {
                break;
            }
            ThreadUtil.sleep(1000);
            log.debug("列表重试" + i);
        }
        //若传入关键字，则将关键字赋值给诚信记录主体（个人或企业名称）,进行查询
        /*
        String keyWord = SiteParams.map.get("keyWord");
        if (StrUtil.isNotEmpty(keyWord)) {
            //获取查询表单
            HtmlForm form = (HtmlForm) page.getElementById("form");
            HtmlTextInput objInput = form.getInputByName("object_name");//诚信记录主体
            objInput.setValueAttribute(keyWord);
        }
        */
        //插入ScrapyData数据
        if (scrapyDataMapper.selectCountByUrl(indexUrl) == 0) {
            //saveFile(currentPage,"诚信不良记录.html", hashKey);//保存原网页
            ScrapyData scrapyData = createScrapyDataObject();
            scrapyData.setUrl(indexUrl);
            scrapyData.setHashKey(OCRUtil.DOWNLOAD_DIR + File.separator + "ncms_cxbljl" + File.separator + MD5Util.encode(indexUrl));
            //scrapyData.setHtml(currentPage.asXml());
            //scrapyData.setText(currentPage.asText());
            scrapyDataMapper.insert(scrapyData);
        }

        //1、获取列表总页数
        int pageTotal = Integer.parseInt(((HtmlAnchor) currentPage.getByXPath("//a[@class='nxt']").get(0)).getAttribute("dt"));
        //2、遍历每页数据
        for (int i = 1; i <= pageTotal; i++) {
            HtmlTableBody tableBody = (HtmlTableBody) currentPage.getByXPath("//tbody[@class='cursorDefault']").get(0);//获取
            List trList = tableBody.getElementsByTagName("tr");
            //3、遍历每行数据
            for (int j = 0; j < trList.size(); j++) {
                HtmlTableRow tableRow = (HtmlTableRow) trList.get(j);
                if (tableRow.getByXPath("//div[@class='github-fork-ribbon github-padding credit_bad']").size() == 0) {//必须是不良记录
                    continue;
                }
                //4、处理单元格
                List<HtmlTableCell> cellList = tableRow.getCells();
                //cellList.get(0)=诚信记录编号
                String recordNo = cellList.get(0).getElementsByTagName("span").get(0).asText().trim();//诚信记录编号
                String uniqueKey = indexUrl + recordNo;//拼接uniqueKey
                if (adminPunishMapper.selectCountByUniqueKey(uniqueKey) == 0) {//不存在则插入
                    AdminPunish adminPunish = createAdminPunish();
                    adminPunish.setUniqueKey(uniqueKey);//设置UniqueKey
                    //cellList.get(1)=诚信记录主体
                    HtmlTableCell nameCell = cellList.get(1);
                    if (recordNo.contains("RY")) {//个人
                        adminPunish.setObjectType("02");
                        adminPunish.setSubject("个人不良诚信记录");
                        adminPunish.setPersonName(nameCell.asText().trim());
                    } else {//企业
                        adminPunish.setObjectType("01");
                        adminPunish.setSubject("企业不良诚信记录");
                        adminPunish.setEnterpriseName(nameCell.asText().trim());
                    }
                    List anchorList = nameCell.getElementsByTagName("a");//获取名称上面的超链接标签
                    if (anchorList.size() > 0) {
                        HtmlAnchor infoAnchor = (HtmlAnchor) anchorList.get(0);
                        String href = infoAnchor.getHrefAttribute();//超链接地址
                        adminPunish.setUrl("http://jzsc.mohurd.gov.cn" + href);//拼接详情URL
                        //5、点击进入详情，获取证件号码
                        ThreadUtil.sleep(1000);
                        HtmlPage infoPage = infoAnchor.click();
                        if ("01".equals(adminPunish.getObjectType())) {//企业
                            List code = infoPage.getByXPath("//td[@data-header='统一社会信用代码']");
                            if (code.size() > 0) {
                                adminPunish.setEnterpriseCode1(((HtmlTableCell) code.get(0)).asText().trim());
                            }
                            List personName = infoPage.getByXPath("//td[@data-header='企业法定代表人']");
                            if (personName.size() > 0) {
                                adminPunish.setPersonName(((HtmlTableCell) personName.get(0)).asText().trim());
                            }
                        } else {//个人
                            List infoList = infoPage.getByXPath("//dd[@class='query_info_dd2']");
                            if (infoList.size() == 2 && ((HtmlDefinitionDescription) infoList.get(0)).getTextContent().contains("居民身份证")) {
                                adminPunish.setPersonId(((HtmlDefinitionDescription) infoList.get(1)).getTextContent().replace("证件号码：", "").trim());//身份证号码
                            }
                        }
                    } else {
                        adminPunish.setUrl(indexUrl);
                    }
                    //cellList.get(2)=决定内容
                    HtmlTableCell contentCell = cellList.get(2);
                    HtmlAnchor contentAnchor = (HtmlAnchor) contentCell.getByXPath("//a[@class='viewReason formsubmit fra']").get(0);
                    adminPunish.setJudgeNo(contentAnchor.getAttribute("data-no"));//决定文号
                    adminPunish.setPunishReason(contentAnchor.getAttribute("data-text"));//处罚事由
                    adminPunish.setJudgeDate(contentCell.getElementsByTagName("span").get(1).asText().replace("决定日期：", "").trim());//决定日期
                    contentCell.removeChild("div", 0);//去掉td中的div标签
                    adminPunish.setPunishResult(contentCell.asText().trim());//处罚结果
                    //cellList.get(3)=实施部门（文号）
                    HtmlTableCell judgeCell = cellList.get(3);
                    judgeCell.removeChild("div", 0);//去掉td中的div标签
                    adminPunish.setJudgeAuth(judgeCell.asText().trim());//判决机关
                    //cellList.get(4)=发布有效期

                    adminPunishMapper.insert(adminPunish);
                }
            }
            //点击到下一个继续处理
            if (i < pageTotal) {
                HtmlAnchor nextAnchor = (HtmlAnchor) currentPage.getByXPath("//a[@dt='" + (i + 1) + "']").get(0);//获取分页元素
                for (int k = 0; k < 5; k++) {//判断页面状态，进行重试
                    ThreadUtil.sleep(1000);
                    currentPage = nextAnchor.click();
                    if (currentPage.getWebResponse().getStatusCode() == 200) {
                        break;
                    }
                    log.debug("翻页重试" + k);
                }
            }
        }
        return null;
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
        adminPunish.setSubject("");// 主题
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
}
