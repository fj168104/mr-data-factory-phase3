package com.mr.modules.api.site.instance.colligationsite.miitsite;

import com.google.common.collect.Maps;
import com.mr.common.IdempotentOperator;
import com.mr.common.OCRUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.lang.Console;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.poi.excel.sax.Excel07SaxReader;
import com.mr.framework.poi.excel.sax.handler.RowHandler;
import com.mr.modules.api.model.ProductionQuality;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import jxl.Sheet;
import jxl.read.biff.BiffException;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

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
			// 页面解析
			for (int i = 0; ; i++) {
				String cListString = "";
				if (i == 0) {
					cListString = getData("http://foodcredit.miit.gov.cn/gongzhongfuwu/baoguangtai/index.html");
				} else {
					cListString =  getData(String.format(baseUrl, i));
				}
				if(StrUtil.isEmpty(cListString)) continue;
				if (cListString.contains("404 Not Found")) break;

				Document document = Jsoup.parse(cListString);
				Elements liElements = document.getElementsByClass("con-r").first().getElementsByClass("fr");
				Elements aElements = document.getElementsByClass("con-r").first().getElementsByTag("a");
				//页面中list解析
				for (int j = 0; j < aElements.size(); j++) {
					String publishDate = liElements.get(j).text().trim();
					String dUrl = nextUrl + aElements.get(j).attr("href").trim().substring(1);
					String title = aElements.get(j).text();
					log.info("dUrl:{}", dUrl);
					//进入明细页面
					String detailstr =  getData(dUrl);
					if(StrUtil.isEmpty(detailstr)) continue;
					Document detailDoc = Jsoup.parse(detailstr);
					Elements aaEles = detailDoc.getElementsByTag("a");
					//明细页面解析
					try {
						for (Element aaEle : aaEles) {
							if (StrUtil.isBlank(aaEle.attr("href"))) continue;
							String href = dUrl.substring(0, dUrl.lastIndexOf("/")) + aaEle.attr("href").substring(1);
							String desp = aaEle.text().trim();
							if ((href.endsWith(".xls") || href.endsWith(".xlsx")) && desp.contains("不合格")) {

								//保存 ScrapyData
								String fileDir = MD5Util.encode(href);
								//下载excel
								String fileName = downLoadFile(href);
								String pFilePath = OCRUtil.DOWNLOAD_DIR
										+ File.separator + "miit"
										+ File.separator + fileDir
										+ File.separator + fileName;
								FileUtil.move(new File(OCRUtil.DOWNLOAD_DIR + File.separator + fileName),
										new File(pFilePath),
										true);
								//创建对象
								ScrapyData scrapyData = new ScrapyData();
								scrapyData.setUrl(href);
								scrapyData.setTitle(desp);
								scrapyData.setSource(source);
								scrapyData.setHashKey(pFilePath);
								scrapyData.setCreatedAt(new Date());
								scrapyData.setHtml(detailDoc.html().replaceAll("\\s*", ""));
								scrapyData.setText(("发布主题：" + title + "　　\n发布时间：" + publishDate + "\n").replaceAll("\\s*", ""));
								scrapyData.setFields(fields);
								scrapyData.setAttachmentType(fileName.substring(fileName.indexOf(".") + 1));
								//入库
								if(saveScrapyDataOne(scrapyData, false)){
									continue;
								}

								List<List<Object>> allList = null;
								if (href.endsWith(".xls")) {
									//excel03
									allList = read03Excel(pFilePath);
								} else {
									//excel07
									allList = read07Excel(pFilePath);
								}
								//解析excel
								int n = 0;
								Map<String, Integer> kMap = Maps.newHashMap();
								outer:
								for (int k = 1; k < allList.size(); k++) {
									Object value01 = allList.get(k).get(0);
									Object value11 = allList.get(k).get(1);
									if (Objects.isNull(value01) && Objects.isNull(value11)) break;

									Object value02 = allList.get(k).get(1);
									Object value12 = allList.get(k).get(2);

									if ((String.valueOf(value01).trim().equals("序号")
											&& String.valueOf(value02).trim().replaceAll("\\s*", "").equals("标称生产企业名称"))
											|| (String.valueOf(value11).trim().equals("序号")
											&& String.valueOf(value12).trim().replaceAll("\\s*", "").equals("标称生产企业名称"))) {
										//获取参与取值的列index
										if (n == 0) {
											n = k + 1;
										}

										for (int m = 0; m < allList.get(k).size(); m++) {
											String label = String.valueOf(allList.get(k).get(m)).trim().replaceAll("\\s*", "");
											if (StrUtil.isEmpty(label)) {
												break outer;
											}
											if (label.equals("被抽样单位名称")) kMap.put("enterprise_name", m);
											if (label.equals("食品名称") || label.contains("样品名称"))
												kMap.put("oper_production", m);
											if (label.contains("检验结果")) kMap.put("oper_result", m);
											if (label.contains("检验机构")) kMap.put("oper_org", m);
										}
									}
								}

								if (n > 0) {
									for (; n < allList.size(); n++) {
										String seq = String.valueOf(allList.get(n).get(0)).trim();
										if (StrUtil.isEmpty(seq)) break;
										ProductionQuality productionQuality = new ProductionQuality();
										productionQuality.setUrl(href);
										productionQuality.setCreatedAt(new Date());
										if (Objects.nonNull(kMap.get("enterprise_name"))) {
											productionQuality.setEnterpriseName(String.valueOf(
													allList.get(n).get(kMap.get("enterprise_name")))
													.trim());
										}
										if (Objects.nonNull(kMap.get("oper_production"))) {
											productionQuality.setOperProduction(String.valueOf(
													allList.get(n).get(kMap.get("oper_production")))
													.trim());
										}
										if (Objects.nonNull(kMap.get("oper_result"))) {
											productionQuality.setOperResult(String.valueOf(
													allList.get(n).get(kMap.get("oper_result")))
													.trim());
										}
										if (Objects.nonNull(kMap.get("oper_org"))) {
											productionQuality.setOperOrg(String.valueOf(
													allList.get(n).get(kMap.get("oper_org")))
													.trim());
										}

										productionQuality.setPublishDate(publishDate);
										productionQuality.setSource(source);
										productionQuality.setTitle(desp);
										saveProductionQualityOne(productionQuality, false);
									}
								}else {
									scrapyDataMapper.deleteAllByUrl(scrapyData.getUrl());
								}

							}
						}
					} catch (Throwable ex) {
						ex.printStackTrace();
						log.error(ex.getMessage() + "| URL:" + dUrl);
					}
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			log.error(e.getMessage());
		} finally {

		}
	}

	protected String getData(String url) {
		String result  ="";
		try{
			result = new IdempotentOperator<String>(new Callable<String>() {
				@Override
				public String call() throws Exception {
					return restTemplate.getForObject(url, String.class);
				}
			}).execute(3);
		}catch (RuntimeException e){
			result = e.getMessage();
		}
		return result;
	}

	/**
	 * 读取2007excel
	 *
	 * @param path
	 * @return
	 */
	private List<List<Object>> read07Excel(String path) {
		List<List<Object>> allList = new LinkedList<>();
		Excel07SaxReader reader = new Excel07SaxReader(createRowHandler(allList));
		reader.read(path, 0);
		return allList;
	}

	private static RowHandler createRowHandler(final List<List<Object>> allList) {
		return (sheetIndex, rowIndex, rowlist) -> {
			Console.log("[{}] [{}] {}", rowlist.size(), rowIndex, rowlist);
			List<Object> newRowlist = new ArrayList<Object>(rowlist);

			allList.add(newRowlist);
		};
	}

	/**
	 * 读取2003excel
	 *
	 * @param path
	 * @return
	 */
	private List<List<Object>> read03Excel(String path) throws IOException, BiffException {
		List<List<Object>> allList = new LinkedList<>();
		jxl.Workbook book = jxl.Workbook.getWorkbook(new File(path));
		//获得第一个工作表对象
//		Sheet sheet = book.getSheet("sheet_one");
		Sheet sheet = book.getSheet(0);
		int rows = sheet.getRows();
		int cols = sheet.getColumns();
		for (int j = 0; j < rows; j++) {
			List<Object> newRowlist = new ArrayList<Object>();
			for (int i = 0; i < cols; i++) {
				newRowlist.add(sheet.getCell(i, j).getContents());
			}
			allList.add(newRowlist);
		}
		return allList;
	}
}
