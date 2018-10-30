package com.mr.modules.api.site.instance.colligationsite.haikwansite.shantou;

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
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：汕头海关走私违规行政处罚
 * url:http://shantou.customs.gov.cn/shantou_customs/596193/596226/596228/596230/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shantou_zswg")
public class HaiKuan_ShanTou_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {

	private String source = "汕头海关";
	private String subject = "汕头海关走私违规行政处罚";

	@Autowired
	SiteParams siteParams;
	@Override
	protected String execute() throws Throwable {
		String ip = "";
		String port = "";
		String area = "shantou";//区域为：汕头
		String baseUrl = "http://shantou.customs.gov.cn";
		String url = "http://shantou.customs.gov.cn/shantou_customs/596193/596226/596228/596230/index.html";
		String increaseFlag = siteParams.map.get("increaseFlag");
		if(increaseFlag==null){
			increaseFlag = "";
		}
		webContext(increaseFlag,baseUrl,url,ip,port,source,area);
		return null;
	};

	@Override
	public void extractWebData(Map<String, String> map) {
		extractData(map.get("sourceUrl"), map.get("publishDate"), map.get("text"));
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
			bodyText = BaiduOCRUtil.getTextStrFromImageFile(filePath + File.separator + attachmentName);

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
		adminPunish.setSubject(subject);
		adminPunish.setSource(source);

		adminPunish.setPunishReason(text);

		text = text.replace("　"," ");
		text = text.replace(" "," ");
		text = text.replaceAll("([\\s])+：([\\s])+","：");
		text = text.replace("。","，");
		text = text.replace(",","，");
		text = text.replace("(","（");
		text = text.replace(")","）");
		text = text.replace("全业代码","企业代码");
		text = text.replace("字 [","字[");
		text = text.replace("] 第 ","]第");
		text = text.replace("第 ","]第");
		text = text.replace(" 号","号");
		text = text.replace("当事人名称","当事人");
		text = text.replace("当事人姓名/名称","当事人");
		text = text.replaceAll("地[\\s]+址","地址");
		text = text.replaceAll("当[\\s]+事[\\s]+人","当事人");
		text = text.replaceAll("([\\s])+","，");
		text = text.replaceAll("[，]+","，");
		text = text.replace(";","，");
		text = text.replace("：，","：");
		text = text.replace("，：","：");
		text = text.replace(":","：");
		text = text.replace("，事人","，当事人");
		text = text.replace("，当事、人：","，当事人：");
		text = text.replace("，当导人：","，当事人：");
		text = text.replace("，当手人：","，当事人：");
		text = text.replace("当事，","当事人：");
		text = text.replace("当事人，","当事人：");
		text = text.replace("当人：","当事人：");
		text = text.replace("当半人，","当事人：");
		text = text.replace("当申人：","当事人：");
		text = text.replace("当事入：","当事人：");
		text = text.replace("当写人：","当事人：");
		text = text.replace("称：","当事人：");
		text = text.replace("法定代表人为","法定代表人：");
		text = text.replace("〔，","〔");
		text = text.replace("，〕","〕");
		text = text.replace("〕，","〕");

		String[] textArr = text.split("，");
		adminPunish.setPunishReason(text);
		adminPunish.setJudgeAuth("中华人民共和国汕头海关");
		for(String str : textArr){
			if(textArr.length <4){
				log.error("网页错误: sourceUrl = " + sourceUrl);
				return;
			}
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
					adminPunish.setPersonName(strArr[1].replace("处罚款人民币","").trim());
				}
				if(strArr.length>=2 && strArr[0].contains("发布主题")){
					adminPunish.setJudgeAuth(strArr[1].replace("行政处罚决定书", ""));
				}
			}

			if(!str.contains("：")&&str.endsWith("公司")&&StrUtil.isEmpty(adminPunish.getEnterpriseName())){
				adminPunish.setEnterpriseName(str);
			}
			if(!str.contains("：")&&(str.contains("缉违")&&(str.contains("号")))){
				adminPunish.setJudgeNo(str);
			}

		}

		adminPunish.setUniqueKey(MD5Util.encode(adminPunish.getUrl()+adminPunish.getEnterpriseName()+adminPunish.getPersonName()+adminPunish.getPublishDate()));
		saveAdminPunishOne(adminPunish,false);

	}


}
