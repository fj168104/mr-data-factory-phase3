package com.mr.modules.api.site.instance.colligationsite.haikwansite.shijiazhuang;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：石家庄海关走私违规行政处罚
 * url:http://shijiazhuang.customs.gov.cn/shijiazhuang_customs/456977/457003/457005/457007/3517d7d7-1.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_shijiazhuang_zswg")
public class HaiKuan_ShiJiaZhuang_ZSWG extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "石家庄海关走私违规行政处罚";
        String area = "shijiazhuang";//区域为：石家庄
        String baseUrl = "http://shijiazhuang.customs.gov.cn";
        String url = "http://shijiazhuang.customs.gov.cn/shijiazhuang_customs/456977/457003/457005/457007/3517d7d7-1.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }
    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}