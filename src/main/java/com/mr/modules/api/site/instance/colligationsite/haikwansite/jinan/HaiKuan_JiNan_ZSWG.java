package com.mr.modules.api.site.instance.colligationsite.haikwansite.jinan;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import com.mr.modules.api.site.instance.colligationsite.util.FilenameFilterUtil;
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
 * 主题：济南海关走私违规行政处罚
 * url:http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500367/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_jinan_zswg")
public class HaiKuan_JiNan_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;

    private String source = "济南海关";
    private String subject = "济南海关走私违规行政处罚";
    private String judgeAuth = "济南海关";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
    //    String source = "济南海关走私违规行政处罚";
        String area = "jinan";//区域为：济南
        String baseUrl = "http://jinan.customs.gov.cn";
        String url = "http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500367/index.html";
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
     * 提取网页文本
     * @map Map用户存储，filePath(附件所在路径)，attachmentName(附件名称),publishDate,text(附件文本)，详情网页地址：sourceUrl
     */
    public void extractWebData(Map<String,String> map){
        String judgeNo = "";
        String name = "";
        String objectType = "01";
        String publishDate = map.get("publishDate");
        String url = map.get("sourceUrl");
        String title = map.get("title");
        String text = map.get("text");
        title = title.replace("(","（").replace(")","）");
        if(title.contains("字") && title.contains("号")){
            title = title.replace("(","（").replace(")","）");
            judgeNo = title.substring(title.lastIndexOf("（"),title.lastIndexOf("）"));
        }else{
            judgeNo = text.substring(text.indexOf("决定书")+3,text.indexOf("号")+1);
        }

        AdminPunish adminPunish = new AdminPunish();
        name = getName(text);
        if(name.length()<5){
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
        adminPunish.setPunishReason(text);
        adminPunish.setUrl(url);
        adminPunish.setUniqueKey(url+"@"+name+"@"+publishDate);
        //数据入库
        if(adminPunishMapper.selectByUrl(url,null,null,null,null).size()==0){
            adminPunishMapper.insert(adminPunish);
        }
    }

    /**
     * 提取网页中附件为：img(各种类型的图片)文本
     * @map Map用户存储，filePath(附件所在路径)，attachmentName(附件名称),publishDate,text(附件文本)，详情网页地址：sourceUrl
     */
    public void extractImgData(Map<String,String> map){
        String attchementName = map.get("attachmentName");
        String title = map.get("title");
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

        resultStr = resultStr.replace("」","")
                .replace("当事人:","当事人：")
                .replace("当事人;","当事人：")
                .replace("地址:","地址：")
                .replace("住所:","地址：")
                .replace("公可","公司")
                .replace("有限公书","有限公司")
                .replace("有限公司法定代表","有限公司,法定代表")
                .replace("当事人名称:","当事人：")
        ;
        if(resultStr.contains("号当事") && !resultStr.contains("号当事人：")){
            resultStr = resultStr.replace("号当事","号当事人：");
        }
        /*System.out.println("----resultStr-----");
        System.out.println(resultStr);*/

        if(!resultStr.contains("海关")){
            log.error("OCR识别不到有用信息或源文件已丢失，对应的URL:"+url);
            return;
        }

        if(title.contains("字") && title.contains("号")){
            title = title.replace("(","（").replace(")","）");
            judgeNo = title.substring(title.indexOf("（")+1,title.lastIndexOf("）"));
        }else{
            judgeNo = resultStr.substring(resultStr.indexOf("决定书")+3,resultStr.indexOf("号")+1);
        }
        name = getName(resultStr);

        AdminPunish adminPunish = new AdminPunish();

        if(name.length()<5){
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
        String nameInfo = "";
        String regEx="当事人：(.*?)地址：";
        List<String> list = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regEx);// 匹配的模式
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            int i = 1;
            list.add(m.group(i));
            i++;
        }
        if(list.size()>0){
            StringBuffer name = new StringBuffer();
            for(String string : list){
                string = string.replace(",","，");
                if(string.contains("，"))
                    string = string.substring(0,string.indexOf("，")).trim();
                name = name.append(string).append("，");
            }
            nameInfo = name.toString();
            nameInfo = nameInfo.substring(0,nameInfo.lastIndexOf("，"));
            if(list.size()==1){
                nameInfo = nameInfo.substring(0,nameInfo.indexOf("公司")+2);
            }
        }else{
            str = str.substring(str.indexOf("决定书")+3,str.indexOf(","));
            if(!str.contains("字") && !str.contains("号")){
                nameInfo = str;
            }else{
                nameInfo = str.substring(str.indexOf("号")+1,str.indexOf("公司")+2).replace("当事人：","");
            }
        }
        return nameInfo;
    }
}
