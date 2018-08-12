package com.mr.modules.api.site.instance.colligationsite.haikwansite.zhanjiang;

import com.mr.common.OCRUtil;
import com.mr.framework.ocr.OcrUtils;
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
 * 主题：湛江海关知识产权行政处罚
 * url:http://zhanjiang.customs.gov.cn/zhanjiang_customs/534855/534876/534878/534879/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_zhanjiang_zscq")
public class HaiKuan_ZhanJiang_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
	@Autowired
	private SiteParams siteParams;
	@Autowired
	private OCRUtil ocrUtil;

	@Override
	protected String execute() throws Throwable {
		String ip = "";
		String port = "";
		String source = "湛江海关知识产权行政处罚";
		String area = "zhanjiang";//区域为：湛江
		String baseUrl = "http://zhanjiang.customs.gov.cn";
		String url = "http://zhanjiang.customs.gov.cn/zhanjiang_customs/534855/534876/534878/534879/index.html";
		String increaseFlag = siteParams.map.get("increaseFlag");
		if (increaseFlag == null) {
			increaseFlag = "";
		}
		webContext(increaseFlag, baseUrl, url, ip, port, source, area);
		return null;
	}

	@Override
	public void extractDocData(Map<String, String> map) {
		String sourceUrl = map.get("sourceUrl");
		String filePath = map.get("filePath");
		String publishDate = map.get("publishDate");
		String attachmentName = map.get("attachmentName");
		String titleText = map.get("text");
		String bodyText = "";
		try {
			bodyText = ocrUtil.getTextFromDocAutoFilePath(filePath, attachmentName);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		String text = titleText + " " + bodyText;
		extractData(sourceUrl, publishDate, text);
	}

	@Override
	public void extractWebData(Map<String, String> map) {
		extractData(map.get("sourceUrl"), map.get("publishDate"), map.get("text"));
	}

	private void extractData(String sourceUrl, String publishDate, String text) {
		AdminPunish adminPunish = new AdminPunish();
		adminPunish.setUrl(sourceUrl);
		adminPunish.setPublishDate(publishDate);
		adminPunish.setUpdatedAt(new Date());
		adminPunish.setCreatedAt(new Date());
		adminPunish.setSubject("湛江海关知识产权行政处罚");
		adminPunish.setSource("湛江海关");

		adminPunish.setPunishReason(text);
		adminPunish.setJudgeAuth("中华人民共和国湛江海关");

		text = text.replace("　", " ");
		text = text.replace(" ", " ");
		text = text.replaceAll("([\\s])+：([\\s])+", "：");
		text = text.replace("。", "，");
		text = text.replace("(", "（");
		text = text.replace(")", "）");
		text = text.replace("字 [", "字[");
		text = text.replace("] 第 ", "]第");
		text = text.replace("第 ", "]第");
		text = text.replace(" 号", "号");
		text = text.replace("编号：", "");
		text = text.replace("当事人姓名/名称：", "当事人： ");
		text = text.replaceAll("当[\\s]+事[\\s]+人", "当事人");
		text = text.replaceAll("([\\s])+", "，");
		text = text.replaceAll("[，]+", "，");
		text = text.replace("当事人：，", "当事人：");


		String[] textArr = text.split("，");

		for (String str : textArr) {
			if (str.contains("：")) {
				String[] strArr = str.split("：");
				if (strArr.length >= 2 && strArr[0].contains("当事人") && !strArr[0].contains("发布主题") && "".equals(adminPunish.getEnterpriseName())) {
					adminPunish.setEnterpriseName(strArr[1]
							.replaceAll("（([0-9a-zA-Z]+)+）", "").replaceAll("（企业代码.*", ""));
					adminPunish.setObjectType("01");
				}

				if (strArr.length >= 2 && (strArr[0].contains("社会信用代码") || strArr[0].contains("营业执照") || strArr[0].contains("企业代码"))) {
					adminPunish.setEnterpriseCode1(strArr[1]);
				}
				if (strArr.length >= 2 && strArr[0].contains("代表人")) {
					adminPunish.setPersonName(strArr[1]);
				}
				if ("".equals(adminPunish.getJudgeNo()) && str.contains("发布主题") && (str.contains("知字") || str.contains("知罚字")) && str.contains("号")) {
					adminPunish.setJudgeNo((strArr[1].replaceAll(".*处罚决定书", "")
							.replace("（", "")
							.replace("）", "")));

				}
			}

		}

		adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl() + adminPunish.getEnterpriseName() + adminPunish.getPersonName() + adminPunish.getPublishDate()));
		saveAdminPunishOne(adminPunish, false);
	}
}
