package com.mr.modules.api.site.instance.colligationsite.haikwansite.dalian;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.FilenameFilterUtil;
import com.mr.modules.api.site.instance.colligationsite.util.RegularUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：大连海关知识产权行政处罚
 * url:http://dalian.customs.gov.cn/dalian_customs/460678/460697/460699/460700/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_dalian_zscq")
public class HaiKuan_DaLian_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String source = "大连海关";
    private String subject = "大连海关知识产权行政处罚";
    private String judgeAuth = "大连海关";
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "大连海关知识产权行政处罚";
        String area = "dalian";//区域为：大连
        String baseUrl = "http://dalian.customs.gov.cn";
        String url = "http://dalian.customs.gov.cn/dalian_customs/460678/460697/460699/460700/index.html";
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
        String judgeNo = "";
        String objectType = "01";
        String name = "";
        String publishDate = map.get("publishDate");
        String url = map.get("sourceUrl");
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
        resultStr = resultStr.replace("当事人:","当事人：").replace("地址:","地址：");
        if(!resultStr.contains("海关")){
            log.error("OCR识别不到有用信息或源文件已丢失，对应的URL:"+url);
            return;
        }
        attchementName = attchementName.replace("(","（").replace(")","）");
        judgeNo = attchementName.substring(attchementName.indexOf("（")+1,attchementName.indexOf("）"));
        if(attchementName.contains("关于")){
            name = attchementName.substring(attchementName.indexOf("关于")+2,attchementName.lastIndexOf("公司")+2);
        }
        if(resultStr.contains("当事人：") && resultStr.contains("地址：")){
            name = getName(resultStr);
        }

        AdminPunish adminPunish = new AdminPunish();

        if(name.length()<5 && !name.startsWith("丹东")){
            objectType = "02";
            adminPunish.setPersonName(name);
        }else{
            adminPunish.setEnterpriseName(name);
        }
        adminPunish.setPublishDate(publishDate);
        adminPunish.setJudgeNo(judgeNo);
        adminPunish.setObjectType(objectType);
        adminPunish.setSource(source);
        adminPunish.setSubject(subject);
        adminPunish.setJudgeAuth(judgeAuth);
        adminPunish.setPunishReason(resultStr);
        adminPunish.setUrl(url);
        adminPunish.setUniqueKey(url+"@"+name+"@"+publishDate);
        //数据入库
        if(adminPunishMapper.selectByUrl(url,null,null,null,null).size()==0){
            adminPunishMapper.insert(adminPunish);
        }
    }

    private String getName(String str){
        String regEx="当事人：(.*?)地址：";
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regEx);// 匹配的模式
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        StringBuffer name = new StringBuffer();
        for(String string : list){
            string = string.replace(",","，");
            if(string.contains("，"))
                string = string.substring(0,string.indexOf("，")).trim();
            name = name.append(string).append(",");
        }
        String nameInfo = name.toString();
        nameInfo = nameInfo.substring(0,nameInfo.lastIndexOf(","));
        return nameInfo;
    }
}
