package com.mr.modules.api.site.instance.colligationsite.haikwansite.xian;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：西安海关走私违规行政处罚
 * url:http://xian.customs.gov.cn/xian_customs/527446/527477/527479/527481/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_xian_zswg")
public class HaiKuan_XiAn_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "西安海关走私违规行政处罚";
        String area = "xian";//区域为：西安
        String baseUrl = "http://xian.customs.gov.cn";
        String url = "http://xian.customs.gov.cn/xian_customs/527446/527477/527479/527481/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
