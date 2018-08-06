package com.mr.modules.api.site.instance.colligationsite.haikwansite.wuhang;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：武汉海关知识产权行政处罚
 * url:http://wuhan.customs.gov.cn/wuhan_customs/506378/506396/506398/506399/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_wuhan_zscq")
public class HaiKuan_WuHan_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "武汉海关知识产权行政处罚";
        String area = "wuhan";//区域为：武汉
        String baseUrl = "http://wuhan.customs.gov.cn";
        String url = "http://wuhan.customs.gov.cn/wuhan_customs/506378/506396/506398/506399/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
