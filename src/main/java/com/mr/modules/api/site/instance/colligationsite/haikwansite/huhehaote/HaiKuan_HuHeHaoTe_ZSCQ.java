package com.mr.modules.api.site.instance.colligationsite.haikwansite.huhehaote;

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
 * 主题：呼和浩特海关知识产权行政处罚
 * url:http://huhehaote.customs.gov.cn/hhht_customs/566209/566249/566251/566253/a5ec72ce-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_huhehaote_zscq")
public class HaiKuan_HuHeHaoTe_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    private String source = "呼和浩特海关";
    private String subject = "呼和浩特海关知识产权行政处罚";
    private String judgeAuth = "呼和浩特海关";

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
    //    String source = "呼和浩特海关知识产权行政处罚";
        String area = "huhehaote";//区域为：呼和浩特
        String baseUrl = "http://huhehaote.customs.gov.cn";
        String url = "http://huhehaote.customs.gov.cn/hhht_customs/566209/566249/566251/566252/1246e2c6-1.html";
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



    public void extractWebData(Map<String,String> map){
        String text = map.get("text");
        String publishDate = map.get("publishDate");
        String sourceUrl = map.get("sourceUrl");
        AdminPunish adminPunish = new AdminPunish();
        adminPunish.setUrl(sourceUrl);
        adminPunish.setPunishReason(text);
        adminPunish.setPublishDate(publishDate);
        adminPunish.setSource(source);
        adminPunish.setSubject(subject);
        adminPunish.setUniqueKey(sourceUrl+"@"+publishDate);
        adminPunish.setObjectType("01");
        adminPunish.setJudgeAuth(judgeAuth);
        //数据入库
        if(adminPunishMapper.selectByUrl(sourceUrl,null,null,null,null).size()==0){
            adminPunishMapper.insert(adminPunish);
        }
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
        if(!resultStr.contains("海关")){
            log.error("OCR识别不到有用信息或源文件已丢失，对应的URL:"+url);
            return;
        }
        AdminPunish adminPunish = new AdminPunish();
        if(attchementName.contains("行政处罚决定书")){

            resultStr = resultStr.replace("有限责任公可","有限责任公司").replace("一社会信代码","统一社会信用代码");
            try{
                if(!resultStr.contains("当事人姓名/名称:")){
                    resultStr = resultStr.replace("当事人姓名/名称","当事人姓名/名称:");
                }
                if(!resultStr.contains("证件名称、证件号码:")){
                    resultStr = resultStr.replace("证件名称、证件号码","证件名称、证件号码:");
                }
                if(!resultStr.contains("法定代表人住址/地址:")){
                    resultStr = resultStr.replace("法定代表人住址/地址","法定代表人住址/地址:");
                }
                if(attchementName.contains("二关知字")){
                    judgeNo = attchementName.substring(attchementName.indexOf("二关知字"),attchementName.lastIndexOf("号")+1);
                }
                String[] infos = resultStr.split(":");
                name = infos[1];
                name = name.substring(0,name.indexOf("证件名称"));
                String code = infos[2];
                code = code.substring(0,code.indexOf("法定"));
                String codeName = "";

                //提取证件名称
                codeName = RegularUtil.regChn(code);

                //提取证件号
                code = RegularUtil.regEngNum(code);

                adminPunish.setEnterpriseName(name);

                if(codeName.contains("社会信用代码")){
                    adminPunish.setEnterpriseCode1(code);
                }else if(codeName.contains("执照注册")){
                    adminPunish.setEnterpriseCode2(code);
                }else if(codeName.contains("组织机构")){
                    adminPunish.setEnterpriseCode3(code);
                }else if(codeName.contains("税务")){
                    adminPunish.setEnterpriseCode4(code);
                }else{
                    adminPunish.setPersonId(code);
                    objectType = "02"; //个人
                    adminPunish.setEnterpriseName("");
                    adminPunish.setPersonName(name);
                }


            }catch (Exception e){
                log.error("OCR识别有误："+e.getMessage());
                log.info("对应的URL为："+url);
            }
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
}
