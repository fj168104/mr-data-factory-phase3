package com.mr.modules.api.site.instance.colligationsite.mofcomsite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mr.common.util.JsonUtil;
import com.mr.framework.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
public class NLP_Ner_API {
    @Value("${nlp.ner.url}")
    private static String url;

    /**
     * map.中主要由：text参数用于存储文本
     * @param map
     * @return
     */
    public static String nerAPI(Map<String,Object> map){
        if(url==null){
            log.info("获取了默认NER的地址···");
            url = "http://api.microrule.com/nlp/ner";
        }
        String str = "";
        if(url.equals("")||url==null||map.size()<1){
            str = "请检查传入的参数···";
        }else {
            str = HttpUtil.post(url,map);
        }
        return str;
    }

    public static void extractWebNerDetail( Map<String,Object> map) {
        try {
            JsonNode jsonNode = JsonUtil.getJson(NLP_Ner_API.nerAPI(map));
            ArrayNode person = (ArrayNode)jsonNode.get("person");
            ArrayNode organization = (ArrayNode)jsonNode.get("organization");
            ArrayNode location = (ArrayNode)jsonNode.get("location");
            ArrayNode facility = (ArrayNode)jsonNode.get("facility");
            ArrayNode gpe = (ArrayNode)jsonNode.get("gpe");

            log.info("person："+person);
            log.info("organization："+organization);
            log.info("location："+location);
            log.info("facility："+facility);
            log.info("gpe："+gpe);

        } catch (Exception e) {
            log.info("实体对象提取过程中发生异常，请检查···"+e.getMessage());
        }
    }

    public static void main(String[] args){
        String text = "\"　　发布主题：诚信“红黑榜”公布 3失信企业被曝光　　\n" +
                "发布时间：2015-03-27\n" +
                "　　扬州市发布第二批诚信“红黑榜”名单，其中有3家失信企业被公开曝光，还有7家企业因守信，登上了“诚信红榜”。据了解，去年底我市建成综合征信系统企业库，企业只要凭营业执照，就可以查询到自己所有的诚信失信记录。个人信用查询系统目前正在建设中，下半年市民就可以查询到自己的信用记录，包括交通违章、水电气缴费信息等。\n" +
                "　　“红黑榜”发布\n" +
                "　　3家企业失信被点名曝光\n" +
                "　　昨天，市文明办、市经信委信用办，发布第二批诚信“红黑榜”名单。截至1月底，我市共计通报守信红榜典型事例3类7项，失信黑榜事例8项。\n" +
                "　　失信黑榜事例分内部通报事项和公开曝光失信事项。其中内部通报5项，公开曝光失信事项3项，分别为扬州市蓝天帐篷厂和扬州互利门窗有限公司拖欠增值税、企业所得税数额较大；扬州康源物资贸易有限公司因虚开增值税发票，被国税部门予以行政处罚。\n" +
                "　　这些失信企业一旦被列入非正常户后，如果老板想换地方重新开公司，他将无法正常办理税务登记。另外，企业做账会计也将被列入黑名单，承担连带责任，以后不能担任税务会计。\n" +
                "　　信用平台建设\n" +
                "　　市民将有望查询个人信用\n" +
                "　　从2011年开始，我市就启动了征信平台建设。在企业信用库这块，去年底，我市完成了全市综合征信系统企业库建设，目前企业库已经收录全市企业基础信息120余万条，开通了网上查询申请和政务服务中心窗口查询等渠道，面向社会提供信用信息开放查询。\n" +
                "　　在个人信用库建设这块，这位人士表示，目前扬州正在建，有望今年下半年正式开通。“个人信用这块，目前很多单位也很看重。比如有些单位招聘，或者学校录取，就需要个人的信用记录证明。但目前个人这块仅人行有一个信用平台，但仅局限于信贷这块，不够全面。”将来就是要有一个全面的信用体系平台，不仅仅是信用卡的还款消费诚信记录，还要纳入交通违章、水电气缴费信息等。\n" +
                "　　目前个人信用信息的查询还存在一些障碍，比如涉及隐私、信息收集方式等。目前正与各个部门进行协调，相关信息正在汇总，下半年有望建成。对于个人隐私这块，将出台相关保护措施。\n" +
                "　　失信惩戒措施\n" +
                "　　失信者不能坐飞机、软卧\n" +
                "　　那对于上了黑榜的企业，到底有哪些惩戒措施呢？据悉，市信用办、市文明办联合工商、法院等9部门联合签订了《扬州市“构建诚信、惩戒失信”合作备忘录》。\n" +
                "　　在这份联合签署的“备忘录”中，记者看到了如何让失信者受损的具体范围。“1.禁止乘坐飞机、列车软卧；2.徐忠俊限制在金融机构贷款或办理信用卡；3.失信被执行人为自然人的，不得担任企业的法定代表人、董事、监事、高级管理人员等。”信用办人员说，“无论你是个人失信，还是你的公司有失信行为，都会由你个人承担后果。飞机票买不到，软卧坐不了，贷款贷不到，办信用卡也不行。”\n" +
                "　　对于已有失信记录的企业，可通过信用弥补渠道及时改正，凭信用修补证明，在综合征信系统企业库删除失信记录。\n" +
                "　　黑榜\n" +
                "　　1.扬州市蓝天帐篷厂\n" +
                "　　上榜理由：拖欠增值税、企业所得税数额较大。\n" +
                "　　2.扬州互利门窗有限公司\n" +
                "　　上榜理由：拖欠增值税、企业所得税数额较大。\n" +
                "　　3.扬州康源物资贸易有限公司\n" +
                "　　上榜理由：虚开增值税发票被国税部门行政处罚。\"\n";
        Map<String,Object> map = new HashMap<>();

        map.put("text",text);
        extractWebNerDetail(map);
    }
}
