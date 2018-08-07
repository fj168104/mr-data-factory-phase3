package com.mr.modules.api.site.instance.colligationsite.haikwansite.shenyang;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：沈阳海关知识产权行政处罚
 * url:http://shenyang.customs.gov.cn/shenyang_customs/462357/462374/462376/953fb896-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shenyang_xzcf")
public class HaiKuan_ShenYang_XZCF extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "沈阳海关知识产权行政处罚";
        String area = "shenyang";//区域为：沈阳
        String baseUrl = "http://shenyang.customs.gov.cn";
        String url = "http://shenyang.customs.gov.cn/shenyang_customs/462357/462374/462376/953fb896-1.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
