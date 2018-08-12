package com.mr.modules.api.site.instance.colligationsite.haikwansite.nanjing;

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
 * 主题：南京海关走私违规行政处罚
 * url:http://nanjing.customs.gov.cn/nanjing_customs/589281/589321/589323/589324/830382a1-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_nanjing_zswg")
public class HaiKuan_NanJing_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Autowired
    SiteParams siteParams;
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "南京海关走私违规行政处罚";
        String area = "nanjing";//区域为：南京
        String baseUrl = "http://nanjing.customs.gov.cn";
        String url = "http://nanjing.customs.gov.cn/nanjing_customs/589281/589321/589323/589324/830382a1-1.html";
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
