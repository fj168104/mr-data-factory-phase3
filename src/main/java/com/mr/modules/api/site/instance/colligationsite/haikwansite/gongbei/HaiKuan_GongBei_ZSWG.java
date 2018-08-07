package com.mr.modules.api.site.instance.colligationsite.haikwansite.gongbei;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：拱北海关走私违规行政处罚
 * url:http://gongbei.customs.gov.cn/gongbei_customs/374280/374316/374324/374326/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_gongbei_zswg")
public class HaiKuan_GongBei_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "拱北海关走私违规行政处罚";
        String area = "gongbei";//区域为：拱北
        String baseUrl = "http://gongbei.customs.gov.cn";
        String url = "http://gongbei.customs.gov.cn/gongbei_customs/374280/374316/374324/374326/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}