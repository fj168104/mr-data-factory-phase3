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
		log.info("xls parse>>>");
		String sourceUrl = map.get("sourceUrl");
		String filePath = map.get("filePath");
		String publishDate = map.get("publishDate");
		String attachmentName = map.get("attachmentName");
		String titleText = map.get("text");
		String bodyText = "";

	}

}
