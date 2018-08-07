package com.mr.modules.api.site.instance.colligationsite.haikwansite.zhengzhou;

import com.mr.modules.api.site.SiteTaskExtend_CollgationSite_HaiKWan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @Auter zjxu
 * @DateTime 2018-08-05
 * 主题：郑州海关知识产权行政处罚
 * url:http://zhengzhou.customs.gov.cn/zhengzhou_customs/501404/501425/501427/501428/index.html
 * 属性：企业名称, 执行文号, 处罚事由, 处罚依据, 处罚结果, 认定机关, 发布日期
 */
@Slf4j
@Scope("prototype")
@Component("haikuan_zhengzhou_zscq")
public class HaiKuan_ZhengZhou_ZSCQ extends SiteTaskExtend_CollgationSite_HaiKWan {
    @Override
    protected String execute() throws Throwable {
        String ip = "";
        String port = "";
        String source = "郑州海关知识产权行政处罚";
        String area = "zhengzhou";//区域为：郑州
        String baseUrl = "http://zhengzhou.customs.gov.cn";
        String url = "http://zhengzhou.customs.gov.cn/zhengzhou_customs/501404/501425/501427/501428/index.html";
        webContext(baseUrl,url,ip,port,source,area);
        return null;
    }

    @Override
    protected String executeOne() throws Throwable {
        return super.executeOne();
    }
}
