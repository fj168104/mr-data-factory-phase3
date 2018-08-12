package com.mr.modules.api.site.instance.colligationsite.haikwansite.fuzhou;

import com.mr.modules.api.SiteParams;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：福州海关知识产权行政处罚
 * url:http://fuzhou.customs.gov.cn/fuzhou_customs/484128/484152/484154/484155/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_fuzhou_zscq")
public class HaiKuan_FuZhou_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "福州海关知识产权行政处罚";
        String area = "fuzhou";//区域为：福州
        String baseUrl = "http://fuzhou.customs.gov.cn";
        String url = "http://fuzhou.customs.gov.cn/fuzhou_customs/484128/484152/484154/484155/index.html";
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
}
