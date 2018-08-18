package com.mr.modules.api.site.instance.colligationsite.haikwansite.beijing;

import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.ocr.OcrUtils;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：北京海关走私违规行政处罚
 * url:http://beijing.customs.gov.cn/beijing_customs/434756/434811/434813/434815/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_beijing_zswg")
public class HaiKuan_BeiJing_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "北京海关走私违规行政处罚";
        String area = "beijing";//区域为：北京
        String baseUrl = "http://beijing.customs.gov.cn";
        String url = "http://beijing.customs.gov.cn/beijing_customs/434756/434811/434813/434815/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if (increaseFlag == null) {
            increaseFlag = "";
        }
        webContext(increaseFlag, baseUrl, url, ip, port, source, area);
//        Map<String, String> map = new HashMap<>();
//        map.put("filePath", "E:\\ChromeDownload");
//        map.put("attachmentName", SiteParams.map.get("keyWord"));
//        extractPdfData(map);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }

    //提取Pdf附件数据
    @Override
    public void extractPdfData(Map<String, String> map) {
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");

        //读取PDF文件成text
        String text = null;
        try {
            OcrUtils ocr = new OcrUtils(filePath);
            File textFile = new File(ocr.readPdf(attachmentName));//解析pdf成txt
            text = FileUtils.readFileToString(textFile, "utf-8");//读取txt
            FileUtils.deleteQuietly(textFile);//删除txt
        } catch (Throwable t) {
            log.warn("读取PDF文件失败", t);
        }
        if (StrUtil.isEmpty(text)) {
            log.warn("读取PDF文件失败，忽略本条记录数据");
            return;
        }
        AdminPunish adminPunish = createAdminPunish();
        adminPunish.setSource("北京海关");// 数据来源
        adminPunish.setSubject("海关走私违规行政处罚");// 主题
        adminPunish.setUrl(map.get("sourceUrl"));// url
        adminPunish.setPublishDate(map.get("publishDate"));// 发布日期
        //解析text
        try {
            adminPunish.setPunishReason(text);
            for (String str : text.replace("\r", "\n").split("\n")) {
                str = str.replace("　", "").replace(" ", "");//去除空格
                if (StrUtil.isEmpty(adminPunish.getJudgeNo()) && !str.contains("：") && (str.contains("不字") || str.contains("查字") || str.contains("违字")) && str.contains("号")) {
                    str = str.replace("〔", "[");
                    str = str.replace("〕", "]");
                    str = str.replace("【", "[");
                    str = str.replace("】", "]");
                    str = str.replace("［", "[");
                    str = str.replace("］", "]");
                    adminPunish.setJudgeNo(str);//处罚决定书文号
                }
                if (StrUtil.isEmpty(adminPunish.getEnterpriseName()) && StrUtil.isEmpty(adminPunish.getPersonName()) && str.contains("当事人：")) {
                    String name = str.substring(str.indexOf("当事人：") + 4).trim();
                    int endIndex = name.replace(",", "，").replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        name = name.substring(0, endIndex);
                    }
                    if (name.length() > 6) {//企业
                        adminPunish.setObjectType("01");
                        adminPunish.setEnterpriseName(name);//企业名称
                    } else {//个人
                        adminPunish.setObjectType("02");
                        adminPunish.setPersonName(name);//姓名
                    }
                }
                if (StrUtil.isEmpty(adminPunish.getEnterpriseCode1()) && str.contains("统一社会信用代码：")) {
                    String code = str.substring(str.indexOf("统一社会信用代码：") + 9).trim();
                    int endIndex = code.replace(",", "，").replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        code = code.substring(0, endIndex);
                    }
                    adminPunish.setEnterpriseCode1(code);//统一社会信用代码
                }
                if (StrUtil.isEmpty(adminPunish.getPersonName()) && (str.contains("法定代表人：") || str.contains("法定代表人为"))) {
                    String personName = str.substring(str.replace("法定代表人为", "法定代表人：").indexOf("法定代表人：") + 6).trim();
                    int endIndex = personName.replace(",", "，").replace("。", "，").indexOf("，");
                    if (endIndex > 0) {
                        personName = personName.substring(0, endIndex);
                    }
                    adminPunish.setPersonName(personName);//企业法定代表人
                }
            }
            // 设置UniqueKey
            adminPunish.setUniqueKey(adminPunish.getUrl() + adminPunish.getJudgeNo() + adminPunish.getEnterpriseName() + adminPunish.getPersonName());
            if (StrUtil.isNotEmpty(adminPunish.getObjectType()) && (StrUtil.isNotEmpty(adminPunish.getPersonName()) || StrUtil.isNotEmpty(adminPunish.getEnterpriseName()))) {
                if (adminPunishMapper.selectCountByUniqueKey(adminPunish.getUniqueKey()) == 0) {//不存在则插入
                    adminPunishMapper.insert(adminPunish);
                }
            } else {
                log.warn("本条数据解析失败，无法入库。数据内容：\n{}", text);
            }
            //log.info(adminPunish.toString());
        } catch (Exception e) {
            log.error("URL={}解析入库失败", map.get("sourceUrl"), e);
        }
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

    public static void main(String[] args) throws IOException {
        String path = "F:\\home\\fengjiang\\Documents\\haikwansite\\beijing\\2bed1ebe374ddb5a9add16730d4b63dd\\中华人民共和国首都机场海关行政处罚决定书（首关缉违字[2017]094号.txt";
        String text = FileUtils.readFileToString(new File(path), "utf-8");
        String content = text.replace("\r\n", "，");
        content = content.replace("\r", "，");
        content = content.replace("\n", "，");
        content = content.replaceAll("当[\\s]+事[\\s]+人", "当事人");
        System.out.println(content);
    }
}
