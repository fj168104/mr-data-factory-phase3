package com.mr.modules.api.site.instance.colligationsite.haikwansite.chongqing;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：重庆海关走私违规行政处罚
 * url:http://chongqing.customs.gov.cn/chongqing_customs/515860/515878/515880/515882/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_chongqing_zswg")
public class HaiKuan_ChongQing_ZSWG  extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "重庆海关走私违规行政处罚";
        String area = "chongqing";//区域为：重庆
        String baseUrl = "http://chongqing.customs.gov.cn";
        String url = "http://chongqing.customs.gov.cn/chongqing_customs/515860/515878/515880/515882/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
