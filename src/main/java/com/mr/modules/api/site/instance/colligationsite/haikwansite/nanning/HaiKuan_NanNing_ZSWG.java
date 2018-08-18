package com.mr.modules.api.site.instance.colligationsite.haikwansite.nanning;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：南宁海关走私违规行政处罚
 * url:http://nanning.customs.gov.cn/nanning_customs/600333/600362/600364/600366/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_nanning_zswg")
public class HaiKuan_NanNing_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
	@Autowired
	SiteParams siteParams;

	@Override
	protected String execute() throws Throwable {
		String ip = "";
		String port = "";
		String source = "南宁海关走私违规行政处罚";
		String area = "nanning";//区域为：南宁
		String baseUrl = "http://nanning.customs.gov.cn";
		String url = "http://nanning.customs.gov.cn/nanning_customs/600333/600362/600364/600366/index.html";
		String increaseFlag = siteParams.map.get("increaseFlag");
		if (increaseFlag == null) {
			increaseFlag = "";
		}
		webContext(increaseFlag, baseUrl, url, ip, port, source, area);
		return null;
	}

	@Override
	public void extractXlsData(Map<String, String> map) {
		log.info("xls parse>>>");
		String sourceUrl = map.get("sourceUrl");
		String filePath = map.get("filePath");
		String publishDate = map.get("publishDate");
		String attachmentName = map.get("attachmentName");
		try {
			jxl.Workbook book = jxl.Workbook.getWorkbook(new File(filePath + File.separator + attachmentName));
			Sheet sheet = book.getSheet(0);
			AdminPunish adminPunish = new AdminPunish();
			adminPunish.setUrl(sourceUrl);
			adminPunish.setPublishDate(publishDate);
			adminPunish.setUpdatedAt(new Date());
			adminPunish.setCreatedAt(new Date());
			adminPunish.setSubject("南宁海关走私违规行政处罚");
			adminPunish.setSource("南宁海关");

			int i = 0;
			if(sheet.getCell(0, 1).getContents().contains("行政处罚决定书")){
				i = 0;
			}else{
				i = 1;
			}

			adminPunish.setJudgeNo(sheet.getCell(1, 1 + i).getContents());
			adminPunish.setPunishAccording(sheet.getCell(1, 2 + i).getContents().replaceAll("\\s*", ""));
			String holdString = sheet.getCell(1, 4 + i).getContents();
			holdString = holdString.replace("\n", "，");
			holdString = holdString.replace("；", "，");
			holdString = holdString.replace(",", "，");
			holdString = holdString.replace("        ", "，");
			String[] textArr = holdString.split("，");
			for (String str : textArr) {
				if(StrUtil.isBlank(str)) continue;
				if(str.contains("公司")){
					if (str.contains("：")) {
						String[] strArr = str.split("：");
						adminPunish.setEnterpriseName(strArr[1]);
					}else {
						adminPunish.setEnterpriseName(str.trim());

					}
				}
				adminPunish.setObjectType("01");
				if(str.contains("法定代表人")){
					if (str.contains("：")) {
						String[] strArr = str.split("：");
						adminPunish.setPersonName(strArr[1].trim());
					}
				}

				if(str.contains("信用代码")){
					if (str.contains("：")) {
						String[] strArr = str.split("：");
						adminPunish.setEnterpriseCode1(strArr[1].trim());
					}
				}
			}

			adminPunish.setPunishReason(sheet.getCell(1, 5 + i).getContents().replaceAll("\\s*", ""));
			adminPunish.setPunishResult(sheet.getCell(1, 6 + i).getContents().replaceAll("\\s*", ""));
			adminPunish.setJudgeAuth("中华人民共和国南宁海关");
			adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
			saveAdminPunishOne(adminPunish,false);

		} catch (Exception e) {
			e.printStackTrace();
		}
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
			if(attachmentName.endsWith("tif")){
				bodyText = BaiduOCRUtil.getTextStrFromTIFFile(filePath, attachmentName, "$");
			}else {
				bodyText = BaiduOCRUtil.getTextStrFromImageFile(filePath + File.separator + attachmentName, "$");
			}

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
		adminPunish.setSubject("南宁海关走私违规行政处罚");
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

		String[] textArr = text.split("$");
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
