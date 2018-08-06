package com.mr.modules.api.site.instance.colligationsite.miitsite;

import com.google.common.collect.Maps;
import com.mr.common.OCRUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.poi.excel.ExcelReader;
import com.mr.framework.poi.excel.ExcelUtil;
import com.mr.modules.api.model.ProductionQuality;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 站点：工业和信息化部网站
 * url：http://foodcredit.miit.gov.cn/gongzhongfuwu/baoguangtai/
 * 主题：曝光台
 * 属性："企业名称 检查产品  检查结果 检查机关  发布日期"
 * 提取：TODO 公司名称 发布日期  检查机关
 * 存储表：production_quality
 */

@Slf4j
@Component("miit_bgt")
@Scope("prototype")
public class MIIT_BGT extends SiteTaskExtend_CollgationSite {
	private String baseUrl = "http://foodcredit.miit.gov.cn/gongzhongfuwu/baoguangtai/index_%d.html";
	private String nextUrl = "http://foodcredit.miit.gov.cn/gongzhongfuwu/baoguangtai";

	@Override
	protected String execute() throws Throwable {
		webContext();
		return null;
	}

	@Override
	protected String executeOne() throws Throwable {
		return super.executeOne();
	}

	//数据来源
	String source = "工业和信息化部网站";
	//要提取的字段
	String fields = "source,subject,url,enterprise_name,oper_production,oper_result,oper_org,publish_date/punishDate";
	//唯一标识 注：一般为，title/JubgeNo+enterpriseName+publishdate/punishdate
	String unique_key = "";
	ExcelReader reader = null;

	private void webContext() {

		//第一页
		log.info("**********************************第 {} 页*******************************", 1);
		try {
			htmlParse(baseUrl);
		} catch (Throwable throwable) {
			log.error("请查阅错误信息···" + throwable.getMessage());
		}
	}

	private void htmlParse(String baseUrl) throws Throwable {
		log.info("******************************************************当前线程为：" + Thread.currentThread().getName());
		try {
			for (int i = 0; ; i++) {
				String cListString = "";
				if (i == 0) {
					cListString = getData("http://foodcredit.miit.gov.cn/gongzhongfuwu/baoguangtai/index.html");
				} else {
					cListString = getData(String.format(baseUrl, i));
				}
				if (cListString.contains("404 Not Found")) break;
				Document document = Jsoup.parse(cListString);
				Elements liElements = document.getElementsByClass("con-r").first().getElementsByClass("fr");
				Elements aElements = document.getElementsByClass("con-r").first().getElementsByTag("a");
				for (int j = 0; j < aElements.size(); j++) {
					String publishDate = liElements.get(i).text().trim();
					String dUrl = nextUrl + aElements.get(i).attr("href").trim().substring(1);
					String title = aElements.get(i).text();
					log.info("dUrl:{}", dUrl);
					//进入明细页面
					String detailstr = getData(dUrl);
					Document detailDoc = Jsoup.parse(detailstr);
					Elements aaEles = detailDoc.getElementsByTag("a");
					for (Element aaEle : aaEles) {
						String href = dUrl.substring(0, dUrl.lastIndexOf("/")) + aaEle.attr("href").substring(1);
						String desp = aaEle.text().trim();
						if ((href.endsWith(".xls") || href.endsWith(".xlsx")) && desp.contains("不合格")) {

							//保存 ScrapyData
							String fileDir = MD5Util.encode(href);
							//下载excel
							String fileName = downLoadFile(href);
							FileUtil.move(new File(OCRUtil.DOWNLOAD_DIR + File.separator + fileName),
									new File(OCRUtil.DOWNLOAD_DIR + File.separator + fileDir + File.separator + fileName),
									true);
							//创建对象
							ScrapyData scrapyData = new ScrapyData();
							scrapyData.setUrl(href);
							scrapyData.setSource(source);
							scrapyData.setHashKey(fileDir);
							scrapyData.setCreatedAt(new Date());
							scrapyData.setHtml(detailDoc.html());
							scrapyData.setText("　　发布主题：" + title + "　　\n发布时间：" + publishDate + "\n");
							scrapyData.setFields(fields);
							scrapyData.setAttachmentType(fileName.substring(fileName.indexOf(".") + 1));
							//入库
							boolean isFlag = saveScrapyDataOne(scrapyData, false);

							//解析excel
							 reader = ExcelUtil.getReader(new File(OCRUtil.DOWNLOAD_DIR
									+ File.separator +
									fileDir + File.separator
									+ fileName));
							for (int k = 1; ; k++) {
								Object value1 = reader.readCellValue(k, 0);
								if (Objects.isNull(value1)) break;

								Object value2 = reader.readCellValue(k, 1);
								if (String.valueOf(value1).trim().equals("序号") && String.valueOf(value2).trim().equals("标称生产企业名称")) {
									//获取参与取值的列index
									Map<String, Integer> kMap = Maps.newHashMap();
									for (int m = 0; ; m++) {
										String label = String.valueOf(reader.readCellValue(k, m)).trim();
										if (StrUtil.isEmpty(label)) break;
										if (label.equals("标称生产企业名称")) kMap.put("enterprise_name", m);
										if (label.equals("食品名称") || label.contains("样品名称"))
											kMap.put("oper_production", m);
										if (label.contains("检验结果")) kMap.put("oper_result", m);
										if (label.contains("检验机构")) kMap.put("oper_org", m);
									}
									for (int n = k + 1; ; n++){
										String seq = String.valueOf(reader.readCellValue(n, 0)).trim();
										if(StrUtil.isEmpty(seq)) break;
										ProductionQuality productionQuality = new ProductionQuality();
										productionQuality.setUrl(href);
										productionQuality.setCreatedAt(new Date());
										if(Objects.nonNull(kMap.get("enterprise_name"))){
											productionQuality.setEnterpriseName(String.valueOf(
													reader.readCellValue(n, kMap.get("enterprise_name")))
													.trim());
										}
										if(Objects.nonNull(kMap.get("oper_production"))){
											productionQuality.setOperProduction(String.valueOf(
													reader.readCellValue(n, kMap.get("oper_production")))
													.trim());
										}
										if(Objects.nonNull(kMap.get("oper_result"))){
											productionQuality.setOperResult(String.valueOf(
													reader.readCellValue(n, kMap.get("oper_result")))
													.trim());
										}
										if(Objects.nonNull(kMap.get("oper_org"))){
											productionQuality.setOperOrg(String.valueOf(
													reader.readCellValue(n, kMap.get("oper_org")))
													.trim());
										}

										//todo publishDate 应该为 varchar类型
//										productionQuality.setPublishDate(publishDate);
										saveProductionQualityOne(productionQuality, false);
									}
								}
							}
						}
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} finally {
			reader.close();
		}
	}

}
