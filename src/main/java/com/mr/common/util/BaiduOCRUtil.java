package com.mr.common.util;

import com.baidu.aip.ocr.AipOcr;
import com.fasterxml.jackson.databind.JsonNode;
import com.mr.common.OCRUtil;
import com.mr.framework.core.io.FileUtil;
import com.mr.framework.core.lang.ObjectId;
import com.mr.framework.core.util.StrUtil;
import com.mr.framework.ocr.OcrUtils;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度云OCR工具类
 *
 * @author pxu 2018/8/13 18:58
 */
@Slf4j
@Getter
@Component
public class BaiduOCRUtil {
    /**
     * 百度云 APP_ID
     */
    private static String appId;
    /**
     * 百度云 APP_KEY
     */
    private static String appKey;
    /**
     * 百度云 SECRET_KEY
     */
    private static String secretKey;

    /**
     * 文件的下载目录
     */
    @Value("${ocr.download_dir}")
    private static String downloadDir;

    /**
     * 从图像URL地址直接读取文本内容（通用OCR识别服务）
     *
     * @param url url
     * @return 返回单个图片识别结果内容
     */
    public static String getTextStrFromImageUrl(String url) {
        List<String> list = getTextFromImageUrl(url);
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 从图像URL地址直接读取文本内容（通用OCR识别服务）
     *
     * @param url url
     * @return 返回单个图片识别结果内容[每行内容为LIST中的一个元素]
     */
    public static List<String> getTextFromImageUrl(String url) {
        return getTextFromImageUrl(createBaiduAipOcrClient(), url);
    }

    /**
     * 获取一组在线图片的全部文本内容(通用OCR识别服务)
     *
     * @param urlList url列表
     * @return 返回全部图片内容拼接后的结果
     */
    public static String getTextStrFromImageUrlList(List<String> urlList) {
        if (urlList == null || urlList.size() == 0) {
            log.warn("urlList is empty");
            return "";
        }
        StringBuilder sb = new StringBuilder();
        List<String> list = getTextFromImageUrlList(urlList);
        for (String s : list) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 获取一组在线图片的全部文本内容(通用OCR识别服务)
     *
     * @param urlList url列表
     * @return 返回全部图片内容拼接后的结果[每行内容为LIST中的一个元素]
     */
    public static List<String> getTextFromImageUrlList(List<String> urlList) {
        List<String> resultList = new ArrayList<>();
        if (urlList == null || urlList.size() == 0) {
            log.warn("urlList is empty");
            return resultList;
        }
        AipOcr aipOcr = createBaiduAipOcrClient();
        for (String url : urlList) {
            resultList.addAll(getTextFromImageUrl(aipOcr, url));
        }
        return resultList;
    }

    /**
     * 从图像URL地址直接读取文本内容（通用OCR识别服务）
     *
     * @return 图片文本内容
     */
    public static List<String> getTextFromImageUrl(AipOcr aipOcr, String url) {
        if (aipOcr == null) {
            aipOcr = createBaiduAipOcrClient();
        }
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<String, String>();
        options.put("detect_direction", "true");//是否检测图像朝向
        options.put("detect_language", "true");//是否检测语言
        options.put("probability", "true");//是否返回识别结果中每一行的置信度
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {//最多进行5次尝试识别该图片内容
            try {
                //访问通用OCR识别，获取结果
                String result = aipOcr.basicGeneralUrl(url, options).toString(2);
                log.debug(result);
                if (getGeneralResult(resultList, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("getTextFromImageUrl error,url={}", url, e);
            }
        }
        return resultList;
    }

    /**
     * 识别本地图片文件上的文本内容（通用OCR识别服务）
     *
     * @param filePath filePath 图片文件路径
     * @param separator 解析行分隔符
     * @return 返回单个图片识别结果内容
     */
    public static String getTextStrFromImageFile(String filePath, String separator) {
        List<String> list = getTextFromImageFile(createBaiduAipOcrClient(), filePath);
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s + separator);
        }
        return sb.toString();
    }

    /**
     * 识别本地图片文件上的文本内容（通用OCR识别服务）
     *
     * @param filePath filePath 图片文件路径
     * @return 返回单个图片识别结果内容
     */
    public static String getTextStrFromImageFile(String filePath) {
        return getTextStrFromImageFile(filePath, " ");
    }


    /**
     * 识别本地图片文件上的文本内容（通用OCR识别服务）
     *
     * @param filePath 图片文件路径
     * @return 返回单个图片识别结果内容[每行文本是list中的一个元素]
     */
    public static List<String> getTextFromImageFile(String filePath) {
        return getTextFromImageFile(createBaiduAipOcrClient(), filePath);
    }

    /**
     * 识别本地图片文件上的文本内容（通用OCR识别服务）
     *
     * @param filePathList 图片文件路径
     * @return 返回单个图片识别结果内容
     */
    public static String getTextStrFromImageFileList(List<String> filePathList) {
        if (filePathList == null || filePathList.size() == 0) {
            log.warn("filePathList is empty");
            return "";
        }
        List<String> list = getTextFromImageFileList(filePathList);
        StringBuilder sb = new StringBuilder();
        for (String s : list) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * 获取一组在线图片的全部文本内容(通用OCR识别服务)
     *
     * @param filePathList 图片文件路径集合
     * @return 返回全部图片识别结果内容[每行文本是list中的一个元素]
     */
    public static List<String> getTextFromImageFileList(List<String> filePathList) {
        List<String> resultList = new ArrayList<>();
        if (filePathList == null || filePathList.size() == 0) {
            log.warn("filePathList is empty");
            return resultList;
        }
        if (filePathList == null || filePathList.size() == 0) {
            return resultList;
        }
        AipOcr aioOcr = createBaiduAipOcrClient();
        for (String filePath : filePathList) {
            resultList.addAll(getTextFromImageFile(aioOcr, filePath));
        }
        return resultList;
    }

    /**
     * 识别本地图片上的文本信息
     *
     * @param aipOcr
     * @param filePath
     * @return
     */
    public static List<String> getTextFromImageFile(AipOcr aipOcr, String filePath) {
        if (aipOcr == null) {
            aipOcr = createBaiduAipOcrClient();
        }
        // 传入可选参数调用接口
        HashMap<String, String> options = new HashMap<>();
        options.put("detect_direction", "true");//是否检测图像朝向
        options.put("detect_language", "true");//是否检测语言
        options.put("probability", "true");//是否返回识别结果中每一行的置信度
        List<String> resultList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {//最多进行5次尝试识别该图片内容
            try {
                //访问通用OCR识别，获取结果
                String result = aipOcr.basicGeneral(filePath, options).toString(2);
                log.debug(result);
                if (getGeneralResult(resultList, result)) {
                    break;
                }
            } catch (Exception e) {
                log.error("getTextFromImageFile error,filePath={}", filePath, e);
            }
        }
        return resultList;
    }

    /**
     * 获取通用OCR识别结果
     *
     * @param resultList
     * @param result
     * @return
     */
    private static boolean getGeneralResult(List<String> resultList, String result) {
        boolean pResult = false;
        try {
            JsonNode jResult = JsonUtil.getJson(result);
            JsonNode jNode = JsonUtil.queryJson(jResult, "words_result");
            if (jNode == null) {
                return false;
            }
            for (JsonNode jItem : jNode) {
                resultList.add(JsonUtil.getJsonStringValue(jItem, "words"));
            }
            pResult = true;
        } catch (Exception e) {
            log.error("getGeneralResult error,result={}", result, e);
        }
        return pResult;
    }

    /**
     * 识别网络图片表格数据到一个List中
     *
     * @param url         图片URL地址
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageUrl(String url, String[] columnNames) {
        return tableRecognitionImageUrl(createBaiduAipOcrClient(), url, columnNames);
    }

    /**
     * 识别网络图片表格数据到一个List中
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
     * 识别本地图片表格数据到一个List中
     *
     * @param filePath    图片路径
     * @param columnNames 表格列名数组
     * @return
     */
    public static List<Map<String, Object>> tableRecognitionImageFile(String filePath, String[] columnNames) {
        return tableRecognitionImageFile(createBaiduAipOcrClient(), filePath, columnNames);
    }

    /**
     * 识别本地图片表格数据到一个List中
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
     * 识别图片表格数据到一个List中
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
     * 解析PDF
     * @return
     * @throws Exception
     */
    public static String getTextStrFromPDFFile(String filePath, String attachmentName) throws Exception {
        return getTextStrFromPDFFile(filePath, attachmentName, " ");
    }

    /**
     * 解析PDF
     * @return
     * @throws Exception
     */
    public static String getTextStrFromPDFFile(String filePath, String attachmentName, String separator) throws Exception {
        //将pdf转换为图片
        OcrUtils ocrUtil = new OcrUtils(filePath);
        String dirFile =ocrUtil.image2Dir(attachmentName, false);

        File testDataDir = new File(dirFile);
        //listFiles()方法是返回某个目录下所有文件和目录的绝对路径，返回的是File数组
        File[] files = testDataDir.listFiles();
        int imgCount = files.length;
//		log.info("tessdata目录下共有 " + imgCount + " 个文件/文件夹");
        //解析image
        StringBuilder sbs = new StringBuilder();
        for (int i = imgCount - 1; i >= 0; i--) {
            sbs.append(getTextStrFromImageFile(files[i].getAbsolutePath(), separator));
        }
        //删除文件夹 dirName
        FileUtil.del(dirFile);

        return sbs.toString();
    }

    /**
     * 创建一个百度云访问客户端对象
     *
     * @return
     */
    public static AipOcr createBaiduAipOcrClient() {
        AipOcr client = new AipOcr(appId, appKey, secretKey);
        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(30000);//默认连接超时时间,30秒
        client.setSocketTimeoutInMillis(300000);//默认读取超时时间,5分钟
        // 可选：设置代理服务器地址, http和socket二选一，或者均不设置
        //client.setHttpProxy("proxy_host", proxy_port);  // 设置http代理
        //client.setSocketProxy("proxy_host", proxy_port);  // 设置socket代理
        return client;
    }

    @Value("${ocr.baip.app_id}")
    public void setAppId(String appId) {
        BaiduOCRUtil.appId = appId;
    }

    @Value("${ocr.baip.app_key}")
    public void setAppKey(String appKey) {
        BaiduOCRUtil.appKey = appKey;
    }

    @Value("${ocr.baip.secret_key}")
    public void setSecretKey(String secretKey) {
        BaiduOCRUtil.secretKey = secretKey;
    }

    /**
     * 从配置文件中获取参数：文件的下载目录
     */
    @Value("${ocr.download_dir}")
    public void setDownloadDir(String dir) {
        if (FileUtil.mkdir(dir) != null) {
            BaiduOCRUtil.downloadDir = dir;
        } else {
            BaiduOCRUtil.downloadDir = System.getProperty("java.io.tmpdir");
        }
    }
}
