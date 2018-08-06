package com.mr.modules.api.site.instance.colligationsite.haikwansite.mangzhouli;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：满洲里海关走私违规行政处罚
 * url:http://manzhouli.customs.gov.cn/manzhouli_customs/566020/566039/566041/566043/406ef184-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_manzhouli_zswg")
public class HaiKuan_MangZhouLi_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "满洲里海关走私违规行政处罚";
        String area = "manzhouli";//区域为：满洲里
        String baseUrl = "http://manzhouli.customs.gov.cn";
        String url = "http://manzhouli.customs.gov.cn/manzhouli_customs/566020/566039/566041/566043/406ef184-1.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
