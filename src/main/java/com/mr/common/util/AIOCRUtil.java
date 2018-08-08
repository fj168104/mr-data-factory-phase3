package com.mr.common.util;

import cn.xsshome.taip.ocr.TAipOcr;
import com.baidu.aip.ocr.AipOcr;
import com.fasterxml.jackson.databind.JsonNode;
import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.lang.ObjectId;
import com.mr.framework.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

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

    /**
     * 百度云 APP_ID
     */
    private static String baipAppId;
    /**
     * 百度云 APP_KEY
     */
    private static String baipAppKey;
    /**
     * 百度云 SECRET_KEY
     */
    private static String baipSecretKey;

    /**
     * 文件的下载目录
     */
    private static String downloadDir;

    /**
     * 从图像URL地址直接读取文本内容（调用腾讯AI开放平台-通用OCR识别服务）
     *
     * @param url url
     * @return 返回单个图片识别结果内容
     */
    public static String getTextFromImageUrl(String url) {
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
                if (getGeneralResult(sText, result)) {
                    break;
                }

            } catch (Exception e) {
                log.error("getTextFromImageUrl error,url={}", url, e);
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
                if (getGeneralResult(sText, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("getTextFromImageFile error,filePath={}", filePath, e);
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
            log.error("getGeneralResult error,result={}", result, e);
        }
        return pResult;
    }

    /**
     * 识别网络图片表格数据到一个List中(调用百度云表格识别接口)
     *
     * @param url         图片URL地址
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageUrl(String url, String[] columnNames) {
        return tableRecognitionImageUrl(createBaiduAipOcrClient(), url, columnNames);
    }

    /**
     * 识别网络图片表格数据到一个List中(调用百度云表格识别接口)
     *
     * @param client      百度云客户端
     * @param url         图片URL地址
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageUrl(AipOcr client, String url, String[] columnNames) {
        if (client == null) {
            client = createBaiduAipOcrClient();
        }
        try {
            byte[] bImage = IOUtils.toByteArray(new URL(url));
            return getTableRecognitionResult(client, bImage, columnNames);
        } catch (IOException e) {
            log.error("tableRecognitionImageUrl error", e);
        }
        return null;
    }

    /**
     * 识别本地图片表格数据到一个List中(调用百度云表格识别接口)
     *
     * @param filePath    图片路径
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageFile(String filePath, String[] columnNames) {
        return tableRecognitionImageFile(createBaiduAipOcrClient(), filePath, columnNames);
    }

    /**
     * 识别本地图片表格数据到一个List中(调用百度云表格识别接口)
     *
     * @param client      百度云客户端
     * @param filePath    图片路径
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageFile(AipOcr client, String filePath, String[] columnNames) {
        if (client == null) {
            client = createBaiduAipOcrClient();
        }
        try {
            byte[] bImage = FileUtil.readBytes(filePath);
            return getTableRecognitionResult(client, bImage, columnNames);
        } catch (Exception e) {
            log.error("tableRecognitionImageFile error", e);
        }
        return null;
    }

    /**
     * 识别图片表格数据到一个List中(调用百度云表格识别接口)
     *
     * @param client      百度云客户端
     * @param bImage      图像二进制数据
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> getTableRecognitionResult(AipOcr client, byte[] bImage, String[] columnNames) {
        if (client == null) {
            client = createBaiduAipOcrClient();
        }
        JSONObject res = null;
        //1、向服务器发起表格异步识别请求
        log.info("send table recognition async request to BaiDuAip");
        for (int i = 0; i < 15; i++) {//最多重试15次
            res = client.tableRecognitionAsync(bImage, null);
            if (res.optInt("error_code", -1) != 18) {//若QPS超限，进行重试
                break;
            }
        }
        JSONArray resultArray = res.optJSONArray("result");// 获取result
        if (resultArray == null || resultArray.length() == 0 || resultArray.optJSONObject(0) == null) {//请求失败，查看error_code
            log.warn("ocr table recognition request failed, res={}", res);
            return null;
        }
        String requestId = resultArray.optJSONObject(0).optString("request_id");// 获取request_id
        if (requestId == null) {
            log.warn("get request_id failed,result={}", resultArray.optJSONObject(0));
            return null;
        }
        log.info("result_id={}", requestId);
        //2、向服务器获取表格异步识别结果(每隔2秒发送一次结果查询请求，最多发送30次结果查询请求，相当于超时时间为1分钟)
        int sleepMillis = 2000;
        for (int i = 0; i < 30; i++) {
            res = client.getTableRecognitionExcelResult(requestId);
            try {
                log.debug(res.toString(2));
                JsonNode resultNode = JsonUtil.getJson(res.toString());
                //识别状态/处理进度(retCode)：1-任务未开始，2-进行中，3-已完成
                int retCode = JsonUtil.getJsonIntValue(resultNode, "result.ret_code", -1);
                if (retCode == 3) {//处理完成
                    String xlsFilePath = downloadDir + ObjectId.next() + ".xls";
                    File xlsFile = new File(xlsFilePath);
                    String fileUrl = JsonUtil.getJsonStringValue(resultNode, "result.result_data");//获取Excel文件地址
                    log.info("file_url={}", fileUrl);
                    if (StrUtil.isEmpty(fileUrl)) {
                        return null;
                    }
                    FileUtils.writeByteArrayToFile(xlsFile, IOUtils.toByteArray(new URL(fileUrl)));//写xls
                    List<Map<String, Object>> list = ExcelUtil.importFromXls(xlsFilePath, columnNames);//识别xls
                    xlsFile.delete();//删除xls
                    return list;
                }
                try {
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                log.error("getTableRecognitionResult error", e);
            }
        }
        return null;
    }


    /**
     * 创建一个腾讯AI开放平台访问客户端对象
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
     * 创建一个百度云访问客户端对象
     *
     * @return
     */
    public static AipOcr createBaiduAipOcrClient() {
        AipOcr client = new AipOcr(baipAppId, baipAppKey, baipSecretKey);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(30000);//默认连接超时时间,30秒
        client.setSocketTimeoutInMillis(300000);//默认读取超时时间,5分钟
        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        //client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
        //client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
        return client;
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

    /**
     * 从配置文件中获取参数：百度云APP_ID
     */
    @Value("${ocr.baip.app_id}")
    public void setBaipAppId(String appId) {
        baipAppId = appId;
    }

    /**
     * 从配置文件中获取参数：百度云APP_KEY
     */
    @Value("${ocr.baip.app_key}")
    public void setBaipAppKey(String appKey) {
        baipAppKey = appKey;
    }

    /**
     * 从配置文件中获取参数：百度云SECRET_ID
     */
    @Value("${ocr.baip.secret_key}")
    public void setBaipSecretKey(String secretKey) {
        baipSecretKey = secretKey;
    }

    /**
     * 从配置文件中获取参数：文件的下载目录
     */
    @Value("${ocr.download_dir}")
    public void setDownloadDir(String dir) {
        if (FileUtil.mkdir(dir) != null) {
            downloadDir = dir;
        } else {
            downloadDir = System.getProperty("java.io.tmpdir");
        }
    }
}