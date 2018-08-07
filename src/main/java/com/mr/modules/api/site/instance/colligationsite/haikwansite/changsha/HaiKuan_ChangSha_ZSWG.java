package com.mr.modules.api.site.instance.colligationsite.haikwansite.changsha;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：长沙海关走私违规行政处罚
 * url:http://changsha.customs.gov.cn/changsha_customs/508922/508939/508941/508943/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_changsha_zswg")
public class HaiKuan_ChangSha_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "长沙海关走私违规行政处罚";
        String area = "changsha";//区域为：长沙
        String baseUrl = "http://changsha.customs.gov.cn";
        String url = "http://changsha.customs.gov.cn/changsha_customs/508922/508939/508941/508943/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
