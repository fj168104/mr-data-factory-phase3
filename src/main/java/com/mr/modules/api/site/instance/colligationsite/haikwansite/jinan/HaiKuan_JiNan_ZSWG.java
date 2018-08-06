package com.mr.modules.api.site.instance.colligationsite.haikwansite.jinan;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：济南海关走私违规行政处罚
 * url:http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500367/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_jinan_zswg")
public class HaiKuan_JiNan_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "济南海关走私违规行政处罚";
        String area = "jinan";//区域为：济南
        String baseUrl = "http://jinan.customs.gov.cn";
        String url = "http://jinan.customs.gov.cn/jinan_customs/500341/500363/500365/500367/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
