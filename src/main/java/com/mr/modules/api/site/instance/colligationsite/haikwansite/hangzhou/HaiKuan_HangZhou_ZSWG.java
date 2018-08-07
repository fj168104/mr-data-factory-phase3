package com.mr.modules.api.site.instance.colligationsite.haikwansite.hangzhou;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：杭州海关走私违规行政处罚
 * url:http://hangzhou.customs.gov.cn/hangzhou_customs/575609/575636/575638/575640/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_hangzhou_zswg")
public class HaiKuan_HangZhou_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "杭州海关走私违规行政处罚";
        String area = "hangzhou";//区域为：杭州
        String baseUrl = "http://hangzhou.customs.gov.cn";
        String url = "http://hangzhou.customs.gov.cn/hangzhou_customs/575609/575636/575638/575640/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}