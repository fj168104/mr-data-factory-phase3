package com.mr.modules.api.site.instance.creditchinasite.gansusite;

import com.mr.framework.core.collection.CollectionUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.modules.api.mapper.DiscreditBlacklistMapper;
import com.mr.modules.api.model.DiscreditBlacklist;
import com.mr.modules.api.site.SiteTaskExtend_CreditChina;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @auther 1.信用中国（甘肃）
 * 2.url:http://www.gscredit.gov.cn/blackList/94124.jhtml
 */
@Slf4j
@Component("gansu_94124")
@Scope("prototype")
public class Gansu_94124 extends SiteTaskExtend_CreditChina {
	String url = "http://www.gscredit.gov.cn/blackList/94124.jhtml";

	@Autowired
	DiscreditBlacklistMapper discreditBlacklistMapper;

	@Override
	protected String executeOne() throws Throwable {
		return super.executeOne();
	}

	@Override
	protected String execute() throws Throwable {
		try {
			extractContext(url);
		}catch (Exception e){
			writeBizErrorLog(url, e.getMessage());
		}
		return null;
	}

	/**
	 * 获取网页内容
	 * 发布单位投诉电话、新闻发布日期、企业名称、企业所在地、上榜原因、惩戒措施、
	 */
	public void extractContext(String url) {
		DiscreditBlacklist dcbl = null;
		Document document = Jsoup.parse(getData(url));
		Elements elementDivs = document.getElementsByTag("div");
		String subject = "";

		String objectType = "";
		for (Element elementDiv : elementDivs) {

			Elements elelementSpans = elementDiv.getElementsByTag("span");
			if (CollectionUtil.isEmpty(elelementSpans)) continue;

			if (StrUtil.isEmpty(subject)) {
				subject = elelementSpans.first().text().replaceAll("\\s*", "");
				if (subject.contains("嘉峪关市中级人民法院失信被执行人名单")) {
					subject = "市中级人民法院失信被执行人";
					continue;
				}

				subject = "";
				continue;
			}

			//市中级人民法院失信被执行人
			if (subject.equals("市中级人民法院失信被执行人")) {
				String text = elelementSpans.first().text();
				//市中级人民法院失信被执行人 处理结束
				if (text.contains("嘉峪关市市场监督管理局食品药品黑榜名单")) {
					subject = "市市场监督食药监局黑榜";
					continue;
				}

				if(text.contains("法人(")){
					objectType = "01";
					continue;
				}

				if(text.contains("自然人(")){
					objectType = "02";
					continue;
				}

				if(objectType.equals("01")){
					DiscreditBlacklist discreditBlacklist = createDefaultDiscreditBlacklist();
					discreditBlacklist.setSubject(subject);
					discreditBlacklist.setObjectType("01");
					discreditBlacklist.setEnterpriseName(text);
					discreditBlacklistMapper.insert(discreditBlacklist);
					continue;
				}
				if(objectType.equals("02")){
					DiscreditBlacklist discreditBlacklist = createDefaultDiscreditBlacklist();
					discreditBlacklist.setSubject(subject);
					discreditBlacklist.setObjectType("02");
					discreditBlacklist.setPersonName(text.substring(0, 4).replaceAll("\\s*", ""));
					discreditBlacklist.setPersonId(text.substring(4).replaceAll("\\s*", ""));
					discreditBlacklistMapper.insert(discreditBlacklist);
					continue;
				}

			}

			//市市场监督食药监局黑榜
			if (subject.equals("市市场监督食药监局黑榜")) {
				String text = elelementSpans.first().text();
				//市市场监督食药监局黑榜 处理结束
				if (text.contains("嘉峪关市旅游局旅游行业黑榜名单")) {
					subject = "市旅游局旅游行业黑榜";
					continue;
				}
				DiscreditBlacklist discreditBlacklist = createDefaultDiscreditBlacklist();
				discreditBlacklist.setSubject(subject);
				discreditBlacklist.setObjectType("01");
				discreditBlacklist.setEnterpriseName(text);
				discreditBlacklistMapper.insert(discreditBlacklist);
				continue;
			}

			//市旅游局旅游行业黑榜
			if (subject.equals("市旅游局旅游行业黑榜")) {
				String text = elelementSpans.first().text();
				//市旅游局旅游行业黑榜 处理结束
				if (text.contains("相关文章")) {
					subject = "";
					break;
				}
				DiscreditBlacklist discreditBlacklist = createDefaultDiscreditBlacklist();
				discreditBlacklist.setSubject(subject);
				discreditBlacklist.setObjectType("01");
				discreditBlacklist.setEnterpriseName(text);
				discreditBlacklistMapper.insert(discreditBlacklist);
				continue;
			}

		}

	}

	private DiscreditBlacklist createDefaultDiscreditBlacklist() {
		DiscreditBlacklist discreditBlacklist = new DiscreditBlacklist();

		discreditBlacklist.setCreatedAt(new Date());
		discreditBlacklist.setUpdatedAt(new Date());
		discreditBlacklist.setSource("信用中国（甘肃）");
		discreditBlacklist.setUrl(url);
		discreditBlacklist.setObjectType("01");
		discreditBlacklist.setEnterpriseCode1("");
		discreditBlacklist.setEnterpriseCode2("");
		discreditBlacklist.setEnterpriseCode3("");
		discreditBlacklist.setPersonName("");
		discreditBlacklist.setPersonId("");
		discreditBlacklist.setDiscreditType("");
		discreditBlacklist.setDiscreditAction("");
		discreditBlacklist.setJudgeNo("");
		discreditBlacklist.setJudgeDate("");
		discreditBlacklist.setJudgeAuth("");
		discreditBlacklist.setStatus("");
		discreditBlacklist.setPublishDate("2015/10/26");
		return discreditBlacklist;
	}

}
