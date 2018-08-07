package com.mr.modules.api.site.instance.colligationsite.haikwansite.jiangmen;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：江门海关行政处罚
 * url:http://jiangmen.customs.gov.cn/jiangmen_customs/536578/536600/536602/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_jiangmen_xzcf")
public class HaiKuan_JiangMen_XZCF extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "江门海关行政处罚";
        String area = "jiangmen";//区域为：江门
        String baseUrl = "http://chongqing.customs.gov.cn";
        String url = "http://jiangmen.customs.gov.cn/jiangmen_customs/536578/536600/536602/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }
    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
