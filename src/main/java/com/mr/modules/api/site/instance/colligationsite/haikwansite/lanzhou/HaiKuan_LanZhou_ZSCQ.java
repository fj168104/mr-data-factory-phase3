package com.mr.modules.api.site.instance.colligationsite.haikwansite.lanzhou;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：兰州海关知识产权行政处罚
 * url:http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/index.html
 * ----http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/5a44e505-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_lanzhou_zscq")
public class HaiKuan_LanZhou_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan{

    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "兰州海关知识产权行政处罚";
        String area = "lanzhou";
        String baseUrl = "http://lanzhou.customs.gov.cn";
        String url = "http://lanzhou.customs.gov.cn/lanzhou_customs/553124/553146/553148/553150/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
