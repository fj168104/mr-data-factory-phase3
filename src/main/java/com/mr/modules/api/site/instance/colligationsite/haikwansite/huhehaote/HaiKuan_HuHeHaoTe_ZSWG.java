package com.mr.modules.api.site.instance.colligationsite.haikwansite.huhehaote;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：呼和浩特海关走私违规行政处罚
 * url:http://huhehaote.customs.gov.cn/hhht_customs/566209/566249/566251/566252/1246e2c6-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_huhehaote_zswg")
public class HaiKuan_HuHeHaoTe_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "呼和浩特海关走私违规行政处罚";
        String area = "huhehaote";//区域为：呼和浩特
        String baseUrl = "http://huhehaote.customs.gov.cn";
        String url = "http://huhehaote.customs.gov.cn/hhht_customs/566209/566249/566251/566252/1246e2c6-1.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
