package com.mr.modules.api.site.instance.colligationsite.haikwansite.nanning;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
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
 * 主题：南宁海关知识产权行政处罚
 * url:http://nanning.customs.gov.cn/nanning_customs/600333/600362/600364/600365/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_nanning_zscq")
public class HaiKuan_NanNing_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "南宁海关知识产权行政处罚";
        String area = "nanning";//区域为：南宁
        String baseUrl = "http://nanning.customs.gov.cn";
        String url = "http://nanning.customs.gov.cn/nanning_customs/600333/600362/600364/600365/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

	@Override
	public void extractDocData(Map<String, String> map) {
		log.info("doc parse>>>");
		String sourceUrl = map.get("sourceUrl");
		String filePath = map.get("filePath");
		String publishDate = map.get("publishDate");
		String attachmentName = map.get("attachmentName");
		String titleText = map.get("text");
		String bodyText = "";
		try {
			bodyText = BaiduOCRUtil.getTextStrFromPDFFile(filePath, attachmentName);
			if (StrUtil.isBlank(bodyText)) return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String text = titleText + " " + bodyText;
		extractData(sourceUrl, publishDate, text);
	}

	@Override
	public void extractImgData(Map<String, String> map) {
		log.info("img parse>>>");
        String sourceUrl = map.get("sourceUrl");
        String filePath = map.get("filePath");
        String publishDate = map.get("publishDate");
        String attachmentName = map.get("attachmentName");
        String titleText = map.get("text");
        String bodyText = "";
        try {
			if (attachmentName.contains(".tif")) {
				bodyText = BaiduOCRUtil.getTextStrFromTIFFile(filePath, attachmentName);
			} else {
				bodyText = BaiduOCRUtil.getTextStrFromImageFile(filePath + File.separator + attachmentName);
			}
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String text = titleText + " " + bodyText;
        extractData(sourceUrl, publishDate, text);
	}

    @Override
    public void extractPdfData(Map<String, String> map) {
    	log.info("pdf parse>>>");
        String sourceUrl = map.get("sourceUrl");
        String filePath = map.get("filePath");
        String publishDate = map.get("publishDate");
        String attachmentName = map.get("attachmentName");
        String titleText = map.get("text");
        String bodyText = "";
        try {
            bodyText = BaiduOCRUtil.getTextStrFromPDFFile(filePath, attachmentName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String text = titleText + " " + bodyText;
        extractData(sourceUrl, publishDate, text);
    }

    private void extractData(String sourceUrl, String publishDate, String text) {
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setUpdatedAt(new Date());
        adminPunish.setCreatedAt(new Date());
        adminPunish.setSubject("南宁海关知识产权行政处罚");
        adminPunish.setSource("南宁海关");

        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国南宁海关");

        text = text.replace("　"," ");
        text = text.replace(" "," ");
        text = text.replaceAll("([\\s])+：([\\s])+","：");
        text = text.replace("。","，");
        text = text.replace("(","（");
        text = text.replace(")","）");
        text = text.replace("字 [","字[");
        text = text.replace("] 第 ","]第");
        text = text.replace("第 ","]第");
        text = text.replace(" 号","号");
        text = text.replace("当事人名称：","当事人： ");
        text = text.replace("当事人姓名/名称：","当事人： ");
        text = text.replaceAll("地[\\s]+址","地址");
        text = text.replaceAll("当[\\s]+事[\\s]+人","当事人");
        text = text.replaceAll("([\\s])+","，");
        text = text.replaceAll("[，]+","，");
        text = text.replace(";","，");
        text = text.replace("：，","：");
        text = text.replace("，：","：");
        text = text.replace(":","：");
		text = text.replace("，事人","，当事人");
		text = text.replace("，当事、人：","，当事人");
		text = text.replace("，当导人：","，当事人");
		text = text.replace("，当手人：","，当事人");

        String[] textArr = text.split("，");
        adminPunish.setPunishReason(text);
        adminPunish.setJudgeAuth("中华人民共和国南宁海关");
        for(String str : textArr){
            if(str.contains("：")){
                String[] strArr = str.split("：");
                if(strArr.length>=2&&strArr[1].length()>6&& strArr[0].contains("当事人")&& StrUtil.isEmpty(adminPunish.getEnterpriseName())){
                    adminPunish.setEnterpriseName(strArr[1]);
                    adminPunish.setObjectType("01");
                }
                if(strArr.length>=2&&strArr[1].length()<=6&& strArr[0].contains("当事人")&&StrUtil.isEmpty(adminPunish.getPersonName())){
                    adminPunish.setPersonName(strArr[1]);
                    adminPunish.setObjectType("02");
                }
                if(strArr.length>=2&&(strArr[0].contains("证件号码")||strArr[0].contains("信用代码")||strArr[0].contains("营业执照"))){
                    adminPunish.setEnterpriseCode1(strArr[1]);
                }
                if(strArr.length>=2&&strArr[0].contains("代表人")){
                    adminPunish.setPersonName(strArr[1]);
                }
                if (strArr.length >= 2 && StrUtil.isEmpty(adminPunish.getJudgeNo()) && strArr[0].contains("发布主题")
                        && strArr[1].contains("（") && strArr[1].contains("）")){
                    adminPunish.setJudgeNo(strArr[1].substring(strArr[1].indexOf("（") + 1, strArr[1].indexOf("）")));
                }
            }
            if(StrUtil.isEmpty(adminPunish.getJudgeNo()) && !str.contains("：")&&(str.contains("关法")||str.contains("知字")||str.contains("罚字")||str.contains("违字"))&&str.contains("号")){
                adminPunish.setJudgeNo(str);
            }
            if(!str.contains("：")&&(str.contains("信用代码"))){
                adminPunish.setEnterpriseCode1(str.replaceAll(".*信用代码",""));
            }

        }

        adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
        saveAdminPunishOne(adminPunish,false);

    }
}
