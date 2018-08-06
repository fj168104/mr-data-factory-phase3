package com.mr.common.util;

import cn.xsshome.taip.ocr.TAipOcr;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;

/**
 * OCR识别工具
 *
 * @author pxu 2018/8/6 16:21
 */
@Slf4j
@Component("AI_OCR")
public class AIOCRUtil {
    /**
     * 腾讯AI开放平台应用ID
     */
    private static String taipAppId;
    /**
     * 腾讯AI开放平台应用KEY
     */
    private static String taipAppKey;

//    public static void main(String[] args) {
//        System.out.println(getTextFromImageUrl(null, "http://huhehaote.customs.gov.cn/hhht_customs/566209/566249/566251/566253/1942397/2018072417310497310.jpg"));
//    }

    /**
     * 从图像URL地址直接读取文本内容（调用腾讯AI开放平台-通用OCR识别服务）
     *
     * @param url url
     * @return 返回单个图片识别结果内容
     */
    public static String getTextFromImageUrl(String url) {
        System.out.println("====" + taipAppId);
        System.out.println("====" + taipAppKey);
        return getTextFromImageUrl(createTengXunAipOcrClient(), url);
    }

    /**
     * 获取一组在线图片的全部文本内容(调用腾讯AI开放平台-通用OCR识别服务)
     *
     * @param urlList url列表
     * @return 返回全部图片内容拼接后的结果
     */
    public static String getTextFromImageUrlList(List<String> urlList) {
        if (urlList == null || urlList.size() == 0) {
            return "";
        }
        StringBuilder sText = new StringBuilder();
        TAipOcr aioOcr = createTengXunAipOcrClient();
        for (String url : urlList) {
            sText.append(getTextFromImageUrl(aioOcr, url));
        }
        return sText.toString();
    }

    /**
     * 从图像URL地址直接读取文本内容（调用腾讯AI开放平台-通用OCR识别服务）
     *
     * @return 图片文本内容
     */
    public static String getTextFromImageUrl(TAipOcr aipOcr, String url) {
        if (aipOcr == null) {
            aipOcr = createTengXunAipOcrClient();
        }
        StringBuilder sText = new StringBuilder();
        for (int i = 0; i < 3; i++) {//最多进行3次尝试识别该图片内容
            try {
                byte[] bImg = IOUtils.toByteArray(new URL(url));
                //访问通用OCR识别，获取结果
                String result = aipOcr.generalOcr(bImg);
                //System.out.println(result);
                if (getGeneralResult(sText, result)) {
                    break;
                }

            } catch (Exception e) {
                log.error("获取URL={}的图片文本内容失败", url, e);
            }
        }
        return sText.toString();
    }

    /**
     * 识别本地图片文件上的文本内容（调用腾讯AI开放平台-通用OCR识别服务）
     *
     * @param filePath 图片文件路径
     * @return 返回单个图片识别结果内容
     */
    public static String getTextFromImageFile(String filePath) {
        return getTextFromImageUrl(createTengXunAipOcrClient(), filePath);
    }

    /**
     * 获取一组在线图片的全部文本内容(调用腾讯AI开放平台-通用OCR识别服务)
     *
     * @param filePathList 图片文件路径集合
     * @return 返回全部图片文件上按顺序拼接的文本内容
     */
    public static String getTextFromImageFileList(List<String> filePathList) {
        if (filePathList == null || filePathList.size() == 0) {
            return "";
        }
        StringBuilder sText = new StringBuilder();
        TAipOcr aioOcr = createTengXunAipOcrClient();
        for (String filePath : filePathList) {
            sText.append(getTextFromImageFile(aioOcr, filePath));
        }
        return sText.toString();
    }

    /**
     * 识别本地图片上的文本信息
     *
     * @param aipOcr
     * @param filePath
     * @return
     */
    public static String getTextFromImageFile(TAipOcr aipOcr, String filePath) {
        if (aipOcr == null) {
            aipOcr = createTengXunAipOcrClient();
        }
        StringBuilder sText = new StringBuilder();
        for (int i = 0; i < 3; i++) {//最多进行3次尝试识别该图片内容
            try {
                //访问通用OCR识别，获取结果
                String result = aipOcr.generalOcr(filePath);
                //System.out.println(result);
                if (getGeneralResult(sText, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("获取filePath={}的图片文本内容失败", filePath, e);
            }
        }
        return sText.toString();
    }

    /**
     * 获取通用OCR识别结果成文本内容（腾讯AI开放平台）
     *
     * @param result
     * @return
     */
    private static boolean getGeneralResult(StringBuilder sText, String result) {
        boolean pResult = false;
        try {
            JsonNode jResult = JsonUtil.getJson(result);
            if (JsonUtil.getJsonIntValue(jResult, "ret", -1) == 0) {//成功
                JsonNode jItemList = JsonUtil.queryJsonArrayForce(jResult, "data.item_list");
                for (JsonNode jItem : jItemList) {
                    sText.append(JsonUtil.getJsonStringValue(jItem, "itemstring"));
                }
                pResult = true;
            }
        } catch (Exception e) {
            log.error("解析通用OCR识别结果内容失败", result);
        }
        return pResult;
    }

    /**
     * 获取一个腾讯AI开放平台访问客户端对象
     *
     * @return
     */
    public static TAipOcr createTengXunAipOcrClient() {
        // 初始化一个TAipOcr
        TAipOcr aipOcr = new TAipOcr(taipAppId, taipAppKey);
        aipOcr.setConnectionTimeoutInMillis(30000);//默认连接超时时间,30秒
        aipOcr.setSocketTimeoutInMillis(300000);//默认读取超时时间,5分钟
        return aipOcr;
    }

    /**
     * 从配置文件中获取参数：腾讯AI开放平台应用ID
     */
    @Value("${ocr.taip.app_id}")
    public void setTaipAppId(String appId) {
        taipAppId = appId;
    }

    /**
     * 从配置文件中获取参数：腾讯AI开放平台应用KEY
     */
    @Value("${ocr.taip.app_key}")
    public void setTaipAppKey(String appKey) {
        taipAppKey = appKey;
    }
}
