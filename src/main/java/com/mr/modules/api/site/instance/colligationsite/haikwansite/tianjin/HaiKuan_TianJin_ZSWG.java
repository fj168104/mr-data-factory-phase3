package com.mr.modules.api.site.instance.colligationsite.haikwansite.tianjin;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：天津海关走私违规行政处罚
 * url:http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427913/b6fd3207-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_tianjin_zswg")
public class HaiKuan_TianJin_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "天津海关走私违规行政处罚";
        String area = "tianjin";//区域为：天津
        String baseUrl = "http://tianjin.customs.gov.cn";
        String url = "http://tianjin.customs.gov.cn/tianjin_customs/427875/427916/427918/427913/b6fd3207-1.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
