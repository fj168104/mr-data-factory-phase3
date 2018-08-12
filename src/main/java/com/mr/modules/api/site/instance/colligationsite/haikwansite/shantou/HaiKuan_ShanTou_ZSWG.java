package com.mr.modules.api.site.instance.colligationsite.haikwansite.shantou;

import com.mr.framework.poi.excel.ExcelReader;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
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
	@Autowired
	SiteParams siteParams;

	//提取结构化数据
	public void extractWebData(Map<String,String> map) {

		String sourceUrl =  map.get("sourceUrl");
		String publishDate = map.get("publishDate");
		String text = map.get("text");

		if(!text.contains("关缉违")) {
			log.warn("数据错误 URL:" + sourceUrl);
			return;
		}
		AdminPunish adminPunish = new AdminPunish();
		adminPunish.setUrl(sourceUrl);
		adminPunish.setPublishDate(publishDate);
		adminPunish.setUpdatedAt(new Date());
		adminPunish.setCreatedAt(new Date());

		adminPunish.setObjectType("01");
		adminPunish.setPunishReason(text);
		String titleKey = text.substring(0, text.indexOf("发布时间")).replace("发布主题：", "")
				.replace("中华人民共和国", "")
				.replace("海关行政处罚决定书", "")
				.replaceAll("\\s*", "");

		adminPunish.setJudgeAuth(String.format("中华人民共和国%s海关", titleKey));
		adminPunish.setSubject(String.format("%s海关走私违规行政处罚", titleKey));
		adminPunish.setSource(String.format("%s海关", titleKey));
		String sPunishStrng = "";
		if(titleKey.equals("汕尾")){
			 sPunishStrng = "尾关缉违";
		}else {
			 sPunishStrng = String.format("%s关缉违", titleKey.substring(0, 1));
		}

		//处罚字
		if(!text.contains(sPunishStrng)) {
			log.warn("数据问题：sPunishStrng " + sPunishStrng);
			return;
		}
		String textBody = text.substring(text.indexOf(sPunishStrng));
		String judgeNo = textBody.substring(0, textBody.indexOf("号") + 1).replaceAll("\\s*", "");
		adminPunish.setJudgeNo(judgeNo);

		//当事人
		String body = textBody.substring(textBody.indexOf("号") + 1);
		String body1 = body.replace("。", "，");
		String enterpriseName = body1.substring(0, body1.indexOf("，"))
				.replace("当事人", "")
				.replace("：", "")
				.replace(" ", "")
				.replaceAll("\\s*", "").trim();
		if(enterpriseName.contains("地址")){
			enterpriseName = enterpriseName.substring(0, enterpriseName.indexOf("地址"));
		}
		adminPunish.setEnterpriseName(enterpriseName);

		//法人
		if (body1.contains("法定代表人")) {
			String personName = "";
			String body2 = body1.substring(body1.indexOf("法定代表人"));
			String personString = body2.substring(0, body2.indexOf("，"));
			if (personString.contains("：")) {
				personName = personString.substring(personString.indexOf("：") + 1).replaceAll("\\s*", "");
			} else {
				personName = personString.replace("法定代表人", "").replaceAll("\\s*", "");
			}
			if(personName.contains("住所")){
				personName = personName.substring(0, personName.indexOf("住所"));
			}
			adminPunish.setPersonName(personName);
		}

		adminPunish.setUniqueKey(MD5Util.encode(sourceUrl + adminPunish.getUrl() + adminPunish.getEnterpriseName() + adminPunish.getPersonName() + adminPunish.getPublishDate()));
		saveAdminPunishOne(adminPunish, false);

	}

}
