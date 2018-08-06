package com.mr.modules.api.site.instance.colligationsite.haikwansite.lasa;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：拉萨海关走私违规行政处罚
 * url:http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613446/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Component("haikuan_lasa_zswg")
@Scope("prototype")
public class HaiKuan_LaSa_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan{
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "拉萨海关走私违规行政处罚";
        String area = "lasa";//区域为：拉萨
        String baseUrl = "http://lasa.customs.gov.cn";
        String url = "http://lasa.customs.gov.cn/lasa_customs/613421/613442/613444/613446/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
