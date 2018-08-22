package com.mr.modules.api.site.instance.colligationsite.haikwansite.jinan;

import com.mr.common.util.BaiduOCRUtil;
import com.mr.common.util.WordUtil;
import com.mr.modules.api.SiteParams;
import com.mr.modules.api.model.AdminPunish;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：济南海关知识产权行政处罚
 * url:http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500366/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_jinan_zscq")
public class HaiKuan_JiNan_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    private String source = "济南海关";
    private String subject = "济南海关知识产权行政处罚";
    private String judgeAuth = "济南海关";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
    //    String source = "济南海关知识产权行政处罚";
        String area = "jinan";//区域为：济南
        String baseUrl = "http://jinan.customs.gov.cn";
        String url = "http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500366/index.html";
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
     * 提取网页中附件为：doc文本
     * @map Map用户存储，filePath(附件所在路径)，attachmentName(附件名称),publishDate,text(附件文本)，详情网页地址：sourceUrl
     */
    public void extractDocData(Map<String,String> map){
        String filePath = map.get("filePath");
        String attachmentName = map.get("attachmentName");
        String judgeNo = "";
        String objectType = "01";
        String publishDate = map.get("publishDate");
        String url = map.get("sourceUrl");
        String title = map.get("title");
        String name = "";
        if(!attachmentName.contains("决定书")){
            return;
        }
        //获取doc里的图片
        List<String> list = WordUtil.getImgFromDoc(filePath,attachmentName);
        //解析图片
        String resultStr = BaiduOCRUtil.getTextStrFromImageFileList(list);
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
        if(title.contains("字") && title.contains("号")){
            title = title.replace("(","（").replace(")","）");
            judgeNo = title.substring(title.indexOf("（")+1,title.lastIndexOf("）"));
        }else{
            judgeNo = resultStr.substring(resultStr.indexOf("决定书")+3,resultStr.indexOf("当事人"));
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
