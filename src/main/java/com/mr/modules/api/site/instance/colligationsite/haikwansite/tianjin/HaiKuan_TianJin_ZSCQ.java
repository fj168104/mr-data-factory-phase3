package com.mr.modules.api.site.instance.colligationsite.haikwansite.tianjin;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.common.OCRUtil;
import com.mr.common.util.BaiduOCRUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.model.ScrapyData;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.FilenameFilterUtil;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：天津海关知识产权行政处罚
 * url:http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427920/2fa9bddc-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_tianjin_zscq")
public class HaiKuan_TianJin_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {

	private String source = "天津海关";
	private String subject = "天津海关知识产权行政处罚";
	private String judgeAuth = "天津海关";

	@Autowired
	SiteParams siteParams;
	@Override
	protected String execute() throws Throwable {
		String ip = "";
		String port = "";
		//    String source = "天津海关知识产权行政处罚";
		String area = "tianjin";//区域为：天津
		String baseUrl = "http://tianjin.customs.gov.cn";
		String url = "http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427913/b6fd3207-1.html";
		String increaseFlag = siteParams.map.get("increaseFlag");
		if(increaseFlag==null){
			increaseFlag = "";
		}
		webContext(increaseFlag,baseUrl,url,ip,port,source,area);
		return null;
	}

	@Override
	protected String executeOne() throws Throwable {
		return super.executeOne();
	}


	/**
	 * 提取网页中附件为：img(各种类型的图片)文本
	 * @map Map用户存储，filePath(附件所在路径)，attachmentName(附件名称),publishDate,text(附件文本)，详情网页地址：sourceUrl
	 */
	public void extractImgData(Map<String,String> map){
		String attchementName = map.get("attachmentName");
		String tail = map.get("attachmentName").substring(attchementName.indexOf("."));
		FilenameFilterUtil filenameFilterUtil = new FilenameFilterUtil(tail);
		String filePath = map.get("filePath");
		File file = new File(filePath);
		List attchmentList = new ArrayList();
		if(file.isDirectory()){
			File[] files = file.listFiles(filenameFilterUtil);
			for(File attchmentFile : files){
				attchmentList.add(attchmentFile.getPath());
			}
		}
		String resultStr = BaiduOCRUtil.getTextStrFromImageFileList(attchmentList);
		resultStr = resultStr.replace("号事人:","号当事人:").replace("地北:","地址:");
		String entName = "";
		String judgeNo = "";
		String url = map.get("sourceUrl");
		try{
			if(attchementName.contains("关于") && attchementName.contains("侵犯")){
				entName = attchementName.substring(attchementName.indexOf("关于")+2,attchementName.indexOf("侵犯")-2);
			}else{
				String[]  infos = resultStr.split(":");
				entName =infos[1];
				if(entName.contains("公司")){
					entName = entName.substring(0,entName.lastIndexOf("公司")+2);
				}else if(entName.contains("地址")){
					entName = entName.substring(0,entName.lastIndexOf("地址"));
				}
                /*if(resultStr.contains("当事人:") && resultStr.contains("地址:")){
                    entName = resultStr.substring(resultStr.indexOf("当事人:")+4,resultStr.indexOf("地址:"));
                }else{
                    entName = resultStr.substring(resultStr.indexOf("当事人:")+4,resultStr.indexOf("公司")+2);
                }*/
			}
			if(attchementName.contains("津关法知字")){
				judgeNo = attchementName.substring(attchementName.indexOf("津关法知字"),attchementName.lastIndexOf("号")+1);
			}else{
				judgeNo = resultStr.substring(resultStr.indexOf("字"),resultStr.indexOf("号")+1);
				judgeNo = "津关法知"+judgeNo;
			}
		}catch (Exception e){
			log.error("OCR识别有误："+e.getMessage());
			log.info("对应的URL为："+url);
		}

		String publishDate = map.get("publishDate");
		String uniquekey = map.get("sourceUrl")+"@"+entName+"@"+publishDate;
		String objectType = "01";
		String punishReason = resultStr;
		/*log.info("entName:"+entName);
		log.info("judgeNo:"+judgeNo);
		log.info("publishDate:"+publishDate);
		log.info("uniquekey:"+uniquekey);
*/
		AdminPunish adminPunish = new AdminPunish();
		adminPunish.setSource(source);
		adminPunish.setSubject(subject);
		adminPunish.setUniqueKey(uniquekey);
		adminPunish.setUrl(url);
		adminPunish.setObjectType(objectType);
		adminPunish.setEnterpriseName(entName);
		adminPunish.setJudgeAuth(judgeAuth);
		adminPunish.setJudgeNo(judgeNo);
		adminPunish.setPunishReason(punishReason);
		adminPunish.setPublishDate(publishDate);

		//数据入库
		if(adminPunishMapper.selectByUrl(url,entName,null,judgeNo,judgeAuth).size()==0){
			adminPunishMapper.insert(adminPunish);
		}
	}


	/**
	 * 解析详情页面
	 * @param htmlPage
	 * @param detailUrl
	 * @param titleName
	 * @param publishDate
	 */
	public Map parseDetailPage(HtmlPage htmlPage,String baseUrl,String detailUrl,String titleName,String publishDate,String source,String area){
		//原文非附件的文本
		String text  = "";
		//用于存储，attachmentName（附件名称），nextPageFlag（翻页标识）
		Map<String,Object> map = new HashMap();
		boolean nextPageFlag = true;
		String attachmentName = "";
		String attachmentType = "";
		String htmlText = "";
		String hashKeyFilePath = "";
		List<HtmlElement> htmlElements = htmlPage.getByXPath("//div[@class='easysite-news-text']");
		if(htmlElements.size()==0){
			htmlElements = htmlPage.getByXPath("//div[@class='xl_Cont1']");
		}
		if(htmlElements.size()>0){
			HtmlElement htmlElement = htmlElements.get(0);
			//1.获取详情子页面
			Document htmlTextDoc = Jsoup.parse("<p>发布主题："+titleName+"</p>"+"<p>发布时间："+publishDate+"</p>"+htmlElement.asXml());
			htmlText = htmlTextDoc.html();
			text  = htmlTextDoc.text();
			//2.获取附件所在的标签
			List<HtmlElement> htmlElementAList = htmlElement.getElementsByTagName("a");
			List<HtmlElement> htmlElementImgList = htmlElement.getElementsByTagName("img");
			//3.创建存储对象
			ScrapyData scrapyData = new ScrapyData();
			int count = 1;

			if(htmlElementImgList.size()>0){
				for(HtmlElement htmlElementImg : htmlElementImgList){
					WebClient webClient = null;
					try {
						webClient = createWebClient("","");
						webClient.getOptions().setTimeout(50000);
						String[] attachmentTypeStr = htmlElementImg.getAttribute("src").split("\\.");
						//创建路径
						hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
						//下载元素网页
						saveFile(htmlPage,titleName+".html",hashKeyFilePath);
						Page page = webClient.getPage(baseUrl+htmlElementImg.getAttribute("src"));
						//下载附件
						if(attachmentTypeStr.length>1){
							attachmentType =attachmentTypeStr[attachmentTypeStr.length-1];
							attachmentName = titleName+(count++)+"."+attachmentType;
							saveFile(page,attachmentName,hashKeyFilePath);
						}
					} catch (IOException e) {
						log.error("下载附件出现异常，请查验···"+e.getMessage());
					}catch (Exception e){
						log.error("保存附件出现异常，请检验···"+e.getMessage());
					}catch (Throwable throwable){
						log.info("创建浏览器窗体异常，请检查···"+ throwable.getMessage());
					}finally {
						webClient.close();
					}
				}
			}else if(htmlElementAList.size()>0){
				for(HtmlElement htmlElementA : htmlElementAList){
					try {
						String[] attachmentTypeStr = htmlElementA.getAttribute("href").split("\\.");
						//创建路径
						hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
						//下载元素网页
						saveFile(htmlPage,titleName+".html",hashKeyFilePath);
						Page page = htmlElementA.click();
						//下载附件
						if(attachmentTypeStr.length>1){
							attachmentType =attachmentTypeStr[attachmentTypeStr.length-1];
							attachmentName = titleName+(count++)+"."+attachmentType;
							saveFile(page,attachmentName,hashKeyFilePath);
						}
					} catch (IOException e) {
						log.error("下载附件出现异常，请查验···"+e.getMessage());
					}catch (Exception e){
						log.error("保存附件出现异常，请检验···"+e.getMessage());
					}
				}
			}else{
				//创建路径
				hashKeyFilePath = OCRUtil.DOWNLOAD_DIR+ File.separator+"haikwansite"+File.separator+area+File.separator+ MD5Util.encode(detailUrl);
				//下载元素网页
				saveFile(htmlPage,titleName+".html",hashKeyFilePath);

			}

			//准备入库操作
			scrapyData.setHtml(htmlText);
			scrapyData.setText(text);
			scrapyData.setUrl(detailUrl);
			scrapyData.setCreatedAt(new Date());
			scrapyData.setSource(source);
			scrapyData.setFields("source,subject,url,enterprise_name,publish_date/punishDate,judge_no,title");
			scrapyData.setAttachmentType(attachmentType);
			scrapyData.setHashKey(hashKeyFilePath);
			//入库
			nextPageFlag = saveScrapyDataOne(scrapyData,false);
		}
		map.put("text",text);
		map.put("attachmentName",attachmentName);
		map.put("nextPageFlag",nextPageFlag);
		map.put("html",htmlText);
		return map;
	}
}
