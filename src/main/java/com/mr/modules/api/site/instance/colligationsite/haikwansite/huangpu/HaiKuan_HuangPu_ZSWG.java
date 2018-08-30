package com.mr.modules.api.site.instance.colligationsite.haikwansite.huangpu;

import com.mr.common.util.ExcelUtil;
import com.mr.framework.core.util.NumberUtil;
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

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：黄埔海关走私违规行政处罚
 * url:http://huangpu.customs.gov.cn/huangpu_customs/536775/536795/xzcf77/hgzswgxzcfajxxgk/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 * <p>
 * modify by pxu 2018-08-24：解析入库
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_huangpu_zswg")
public class HaiKuan_HuangPu_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String baseUrl = "http://huangpu.customs.gov.cn";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "黄埔海关走私违规行政处罚";
        String area = "huangpu";//区域为：黄埔
        String url = "http://huangpu.customs.gov.cn/huangpu_customs/536775/536795/xzcf77/hgzswgxzcfajxxgk/index.html";
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

    /**
     * @param map
     */
    @Override
    public void extractWebData(Map<String, String> map) {
        String html = map.get("html");
        String url = map.get("sourceUrl");
        try {
            Document doc = Jsoup.parse(html);
            Element table = doc.getElementsByTag("table").first();
            if (table != null) {//网页中为表格
                Elements trs = table.getElementsByTag("tr");//获取
                for (Element tr : trs) {
                    Elements tds = tr.getElementsByTag("td");
                    if (tds.size() != 12) {//表格数据行每行有12个单元格
                        continue;
                    }
                    AdminPunish adminPunish = createAdminPunish();
                    adminPunish.setSource("黄埔海关");// 数据来源
                    adminPunish.setSubject("海关走私违规行政处罚");// 主题
                    adminPunish.setUrl(url);// url
                    adminPunish.setPublishDate(map.get("publishDate"));// 发布日期

                    //tds.get(0).text();//序号
                    //tds.get(1).text();//案件名称
                    String name = tds.get(2).text();//当事人-企业或组织名称
                    String code = tds.get(3).text();//当事人-企业或组织代码
                    String personName = tds.get(4).text();//当事人-法定代表人或负责人
                    String reason = tds.get(5).text();//违法事实和理由
                    String result = tds.get(6).text();//处罚结果
                    String according = tds.get(7).text();//处罚依据
                    String judgeNo = tds.get(8).text();//法律文书-种类及编号
                    String judgeDate = tds.get(9).text();//法律文书-生成日期
                    String judgeAuth = tds.get(10).text();//作出决定的海关
                    //tds.get(11).text();//救济渠道

                    if(StrUtil.isBlank(name)){
                        continue;
                    }

                    if (name.length() > 6) {
                        adminPunish.setObjectType("01");//企业
                        adminPunish.setEnterpriseName(name);
                    } else {
                        adminPunish.setObjectType("02");//个人
                        adminPunish.setPersonName(name);
                    }
                    if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && code.contains("统一社会信用代码")) {
                        code.replace("统一社会信用代码:", "");
                        code.replace("统一社会信用代码：","");
                        code.replace("统一社会信用代码为","");
                        code.replace("统一社会信用代码", "");
                        adminPunish.setEnterpriseCode1(code);
                    }
                    if (StrUtil.isBlank(adminPunish.getPersonName())) {
                        adminPunish.setPersonName(personName);//法定代表人或负责人
                    }
                    adminPunish.setPunishReason(reason);
                    adminPunish.setPunishResult(result);
                    adminPunish.setPunishAccording(according);
                    adminPunish.setJudgeNo(judgeNo.replace("行政处罚决定书", "").trim());
                    adminPunish.setJudgeDate(judgeDate);
                    adminPunish.setJudgeAuth(judgeAuth);
                    // 设置UniqueKey
                    adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                    //不存在则插入
                    if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                        adminPunishMapper.insert(adminPunish);
                    } else {
                        log.info("此条记录已存在，不需要入库！");
                    }
                }
            }
        } catch (Throwable t) {
            log.error("解析web页面失败,url={}", url, t);
        }
    }

    /**
     * 解析xls附件
     *
     * @param map
     */
    @Override
    public void extractXlsData(Map<String, String> map) {
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");
        String url = map.get("sourceUrl");
        try {
//            //根据URL查询库中是否存在指定记录
//            if (StrUtil.isNotBlank(url) && adminPunishMapper.selectCountByUrl(url) > 0) {
//                log.info("此条记录已存在，不需要入库！");
//                return;
//            }
            String[] colNames = new String[]{"序号", "案件名称", "企业或组织名称", "企业或组织代码", "法定代表人或负责人", "违法事实和理由", "处罚结果", "处罚依据", "法律文书种类及编号", "法律文书生成日期", "作出处罚决定的海关"};
            List<Map<String, Object>> dataList = ExcelUtil.importFromXls(filePath + File.separator + attachmentName, colNames);
            for (Map<String, Object> dataMap : dataList) {
                Object serialNo = dataMap.get("序号");
                Object name = dataMap.get("企业或组织名称");
                if (serialNo == null || !NumberUtil.isNumber(serialNo.toString().trim())) {//序号不为数字的过滤掉
                    continue;
                }
                if (name == null || StrUtil.isBlank(name.toString())) {//企业名称为空的过滤掉
                    continue;
                }
                name = name.toString().trim();

                AdminPunish adminPunish = createAdminPunish();
                adminPunish.setSource("黄埔海关");// 数据来源
                adminPunish.setSubject("海关走私违规行政处罚");// 主题
                adminPunish.setUrl(url);// url
                adminPunish.setPublishDate(map.get("publishDate"));// 发布日期

                Object personName = dataMap.get("法定代表人或负责人");
                Object reason = dataMap.get("违法事实和理由");
                Object result = dataMap.get("处罚结果");
                Object according = dataMap.get("处罚依据");
                Object judgeNo = dataMap.get("法律文书种类及编号");
                Object judgeDate = dataMap.get("法律文书生成日期");
                Object judgeAuth = dataMap.get("作出处罚决定的海关");

                if (name.toString().length() > 6) {
                    adminPunish.setObjectType("01");//企业
                    adminPunish.setEnterpriseName(name.toString());
                } else {
                    adminPunish.setObjectType("02");//个人
                    adminPunish.setPersonName(name.toString());
                }
                if (StrUtil.isBlank(adminPunish.getPersonName())) {
                    adminPunish.setPersonName(personName == null ? "" : personName.toString().trim());//法定代表人或负责人
                }
                adminPunish.setPunishReason(reason == null ? "" : reason.toString().trim());
                adminPunish.setPunishResult(result == null ? "" : result.toString().trim());
                adminPunish.setPunishAccording(according == null ? "" : according.toString().trim());
                adminPunish.setJudgeNo(judgeNo == null ? "" : judgeNo.toString().replace("行政处罚决定书", "").trim());
                adminPunish.setJudgeDate(judgeDate == null ? "" : judgeDate.toString().trim());
                adminPunish.setJudgeAuth(judgeAuth == null ? "" : judgeAuth.toString().trim());
                // 设置UniqueKey
                adminPunish.setUniqueKey(getUniqueKey(adminPunish));
                //不存在则插入
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getSource(), adminPunish.getSubject(), adminPunish.getUniqueKey()) == 0) {
                    adminPunishMapper.insert(adminPunish);
                } else {
                    log.info("此条记录已存在，不需要入库！");
                }
            }
        } catch (Throwable t) {
            log.error("解析xls失败,url={}", url, t);
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
