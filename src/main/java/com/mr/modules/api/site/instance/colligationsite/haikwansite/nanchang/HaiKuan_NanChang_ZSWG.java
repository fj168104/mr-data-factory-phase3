package com.mr.modules.api.site.instance.colligationsite.haikwansite.nanchang;

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
 * 主题：南昌海关走私违规行政处罚
 * url:http://nanchang.customs.gov.cn/nanchang_customs/496836/496864/496866/496868/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_nanchang_zswg")
public class HaiKuan_NanChang_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "南昌海关走私违规行政处罚";
        String area = "nanchang";//区域为：南昌
        String baseUrl = "http://nanchang.customs.gov.cn";
        String url = "http://nanchang.customs.gov.cn/nanchang_customs/496836/496864/496866/496868/index.html";
        String increaseFlag = siteParams.map.get("increaseFlag");
        if(increaseFlag==null){
            increaseFlag = "";
        }
        List<Map<String,String>> listMap = webContext(increaseFlag,baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
