package com.mr.modules.api.site.instance.colligationsite;

import com.mr.common.util.SpringUtils;
import com.mr.modules.api.TaskStatus;
import com.mr.modules.api.site.ResourceGroup;
import com.mr.modules.api.site.SiteTaskExtend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 组装任务
 */
@Slf4j
@Component("colligationsite_sitetask_all")
@Scope("prototype")
public class ColligationSite_SiteTask_All extends SiteTaskExtend {

	@Override
	protected String execute() throws Throwable {
		for (String groupIndex : groupIndexs) {
			ResourceGroup task = (ResourceGroup) SpringUtils.getBean(groupIndex);

			log.info(groupIndex + " calling result：" + TaskStatus.getName(task.start()));
			while (!task.isFinish()) {
				Thread.sleep(3000);
			}
			log.info(groupIndex + " executing complete.");
		}

		return null;
	}

	String groupIndexs[] = {
			"bonu_beijing",
			"bonu_chongqing",
			"bonu_fujian",
			"bonu_guangxi",
			"bonu_guiZhou",
			"bonu_hubei",
			"bonu_liaoning",
			"bonu_main",
			"bonu_qinghai",
			"bonu_sichuan",
			"miit_bgt",
			"mofcom_sxbg",
			"mof_cgyzwfsxmd",
			"ncms_bjcftb",
			"ncms_blacklist",
			"ncms_cxbljl",
			"ncms_dfcftb",
			"prccdarc",
			"saoec_xzcf",
			"saws_2016_first_bljlhmd",
			"saws_2016_second_bljlhmd",
			"saws_2017_first_bljlhmd",
			"swas_2017_third_sxhmd",
			"swas_2018_first_sxhmd",
			"haikuan_beijing_zscq",
			"haikuan_beijing_zswg",
			"haikuan_tianjin_zscq",
			"haikuan_tianjin_zswg",
			"haikuan_huhehaote_zscq",
			"haikuan_huhehaote_zswg",
			"haikuan_dalian_zscq",
			"haikuan_dalian_zswg",
			"haikuan_changchun_zscq",
			"haikuan_changchun_zswg",
			"haikuan_nanjing_zscq",
			"haikuan_nanjing_zswg",
			"haikuan_jinan_zscq",
			"haikuan_jinan_zswg",
			"haikuan_wuhan_zscq",
			"haikuan_wuhan_zswg",
			"haikuan_guangzhou_zscq",
			"haikuan_guangzhou_zswg",
			"haikuan_shantou_zswg",
			"haikuan_huangpu_zscq",
			"haikuan_huangpu_zswg",
			"haikuan_zhanjiang_zswg",
			"haikuan_nanning_zscq",
			"haikuan_nanning_zswg",
			"haikuan_haikou_zscq",
			"haikuan_haikou_zswg",
			"haikuan_chengdu_zscq",
			"haikuan_chengdu_zswg",
			"haikuan_guiyang_zscq",
			"haikuan_kunming_zswg",
			"haikuan_xian_zscq",
			"haikuan_xian_zswg",
			"haikuan_lanzhou_zswg",
			"haikuan_xining_zscq",
			"haikuan_xining_zswg",
			"haikuan_yinchuan_zscq",
			"haikuan_yinchuan_zswg",
			"haikuan_wulumuqi_zswg",
			"haikuan_xiamen_zscq",
			"haikuan_qingdao_zscq",
			"haikuan_taiyuan_zswg",
			"haikuan_haerbin_zswg",
			"haikuan_zhengzhou_zswg",
			"haikuan_wulumuqi_zscq",
			"haikuan_chongqing_zscq",
			"haikuan_chongqing_zswg",
			"haikuan_zhanjiang_zscq",
			"haikuan_jiangmen_xzcf",
			"haikuan_gongbei_zscq",
			"haikuan_gongbei_zswg",
			"haikuan_shantou_zscq",
			"haikuan_changsha_zscq",
			"haikuan_changsha_zswg",
			"haikuan_xiamen_zswg",
			"haikuan_nanchang_zscq",
			"haikuan_nanchang_zswg",
			"haikuan_hangzhou_zscq",
			"haikuan_hangzhou_zswg",
			"haikuan_hefei_zscq",
			"haikuan_hefei_zswg",
			"haikuan_fuzhou_zscq",
			"haikuan_fuzhou_zswg",
			"haikuan_zhengzhou_zscq",
			"haikuan_shenyang_xzcf",
			"haikuan_haerbin_zscq",
			"haikuan_shijiazhuang_zscq",
			"haikuan_shijiazhuang_zswg",
			"haikuan_manzhouli_zscq",
			"haikuan_manzhouli_zswg",
			"haikuan_taiyuan_zscq"

	};

}
