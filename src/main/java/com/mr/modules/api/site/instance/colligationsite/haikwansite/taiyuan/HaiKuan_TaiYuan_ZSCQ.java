package com.mr.modules.api.site.instance.colligationsite.haikwansite.taiyuan;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：太原海关知识产权行政处罚
 * url:http://taiyuan.customs.gov.cn/taiyuan_customs/585802/585824/585826/585827/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_taiyuan_zscq")
public class HaiKuan_TaiYuan_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "太原海关知识产权行政处罚";
        String area = "taiyuan";//区域为：太原
        String baseUrl = "http://taiyuan.customs.gov.cn";
        String url = "http://taiyuan.customs.gov.cn/taiyuan_customs/585802/585824/585826/585827/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
