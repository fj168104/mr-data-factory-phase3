package com.mr.modules.api.site.instance.colligationsite.mofcomsite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.mr.common.util.JsonUtil;
import com.mr.framework.http.HttpUtil;
import com.mr.modules.api.site.instance.colligationsite.util.MD5Util;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * resultcode://0000:text不能为空；0001：org非法机构号；0002:orgkey机构认证码非法；8888:完成处理；9999：其他异常
 */
@Slf4j
@Component
@Data
public class NLP_Ner_API {
    @Value("${nlp.ner.url}")
    private  String url;
    @Value("${nlp.ner.orgid}")
    private  String orgid;
    @Value("${nlp.ner.orgkey}")
    private  String orgkey;

    /**
     * map.中主要由：text参数用于存储文本
     * @param map
     * @return
     */
    public  String nerAPI(Map<String,Object> map){
        log.info("nlp_ner_url:{}\n+nlp_ner_orgid:{}\n+nlp_ner_orgkey:{}",getUrl(),getOrgid(),getOrgkey());
        if(getUrl()==null||getUrl().equals("")){
            setOrgid( "zt");
        }
        if(getOrgkey()==null||getOrgkey().equals("")){
            setOrgkey(MD5Util.encode("zt"));
        }
        map.put("orgid",getOrgid());
        map.put("orgkey",getOrgkey());

        if(getUrl()==null){
            log.info("获取了默认NER的地址···");
            setUrl("http://api.microrule.com/nlp/ner") ;
            //url = "http://localhost:8080/nlp/ner";
        }
        String str = "";
        if(getUrl()==null||getUrl().equals("")||map.size()<1){
            str = "请检查传入的参数···";
        }else {
            str = HttpUtil.post(getUrl(),map);
        }
        return str;
    }

    public  void extractWebNerDetail( Map<String,Object> map) {
        try {
            JsonNode jsonNode = JsonUtil.getJson(nerAPI(map));
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
            log.error("实体对象提取过程中发生{}异常,请检查···{}",e.getMessage(),e.toString());



        }
    }

    public static void main(String[] args){
        String text ="1北京市，于国铭，北京铁研建设监理有限责任公司，初始提供虚假监理工程师执业资格证书。\n" +
                "2辽宁省，石玉光，辽宁铁诚建设监理有限公司，注销提供虚假注册监理工程师注册执业证书。\n" +
                "3江苏省，吕小忠，江苏国兴建设项目管理有限公司，注销提供虚假注册监理工程师注册执业证书。\n" +
                "4陕西省，党春民，陕西永明项目管理有限公司，初始提供虚假监理工程师执业资格证书。\n" +
                "5陕西省，张少莉，陕西永明项目管理有限公司初始提供虚假监理工程师执业资格证书。";
        Map<String,Object> map = new HashMap<>();
        map.put("text",text);
        new NLP_Ner_API().extractWebNerDetail(map);
    }
}
