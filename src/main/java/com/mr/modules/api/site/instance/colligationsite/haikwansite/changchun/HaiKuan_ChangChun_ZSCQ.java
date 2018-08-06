package com.mr.modules.api.site.instance.colligationsite.haikwansite.changchun;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：长春海关知识产权行政处罚
 * url:http://changchun.customs.gov.cn/changchun_customs/465846/465861/465863/465864/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_changchun_zscq")
public class HaiKuan_ChangChun_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "长春海关知识产权行政处罚";
        String area = "changchun";//区域为：长春
        String baseUrl = "http://changchun.customs.gov.cn";
        String url = "http://changchun.customs.gov.cn/changchun_customs/465846/465861/465863/465864/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
