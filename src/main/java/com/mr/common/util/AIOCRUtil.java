package com.mr.common.util;

import cn.xsshome.taip.ocr.TAipOcr;
import com.fasterxml.jackson.databind.JsonNode;
import com.mr.framework.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.List;

/**
 * 腾讯AI开放平台OCR识别工具
 * <p>
 * 图片格式支持jpg,png,bmp
 * 图片大小限制为1M
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
     * 从图像URL地址直接读取文本内容（调用腾讯AI开放平台-通用OCR识别服务）
     *
     * @param url url
     * @return 返回单个图片识别结果内容
     */
    public static String getTextFromImageUrl(String url) {
        return getTextFromImageUrl(createTengXunAipOcrClient(), url);
    }

    /**
     * 获取一组在线图片的全部文本内容(腾讯AI开放平台-通用OCR识别服务)
     *
     * @param urlList url列表
     * @return 返回全部图片内容拼接后的结果
     */
    public static String getTextFromImageUrlList(List<String> urlList) {
        return getTextFromImageUrlList(urlList, "\n");//分割符为换行符
    }

    /**
     * 获取一组在线图片的全部文本内容(腾讯AI开放平台-通用OCR识别服务)
     *
     * @param urlList
     * @param separator 分隔符
     * @return
     */
    public static String getTextFromImageUrlList(List<String> urlList, String separator) {
        if (urlList == null || urlList.size() == 0) {
            return "";
        }
        StringBuilder sText = new StringBuilder();
        TAipOcr aioOcr = createTengXunAipOcrClient();
        for (String url : urlList) {
            sText.append(getTextFromImageUrl(aioOcr, url)).append(separator);
        }
        return sText.toString();
    }

    /**
     * 从图像URL地址直接读取文本内容（腾讯AI开放平台-通用OCR识别服务）
     *
     * @param aipOcr
     * @param url
     * @return 图片文本内容
     */
    public static String getTextFromImageUrl(TAipOcr aipOcr, String url) {
        return getTextFromImageUrl(aipOcr, url, "\n");//分隔符为换行符
    }

    /**
     * 从图像URL地址直接读取文本内容（腾讯AI开放平台-通用OCR识别服务）
     *
     * @param aipOcr
     * @param url
     * @param separator 分隔符
     * @return 图片文本内容
     */
    public static String getTextFromImageUrl(TAipOcr aipOcr, String url, String separator) {
        if (aipOcr == null) {
            aipOcr = createTengXunAipOcrClient();
        }
        StringBuilder sText = new StringBuilder();
        for (int i = 0; i < 5; i++) {//最多进行5次尝试识别该图片内容
            try {
                byte[] bImg = IOUtils.toByteArray(new URL(url));
                //访问通用OCR识别，获取结果
                String result = aipOcr.generalOcr(bImg);
                log.debug(result);
                if (getGeneralResult(sText, result, separator)) {
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
        return getTextFromImageFile(createTengXunAipOcrClient(), filePath);
    }

    /**
     * 获取一组在线图片的全部文本内容(腾讯AI开放平台-通用OCR识别服务)
     *
     * @param filePathList 图片文件路径集合
     * @return 返回全部图片文件上按顺序拼接的文本内容
     */
    public static String getTextFromImageFileList(List<String> filePathList) {
        return getTextFromImageFileList(filePathList, "\n");
    }

    /**
     * 获取一组在线图片的全部文本内容(腾讯AI开放平台-通用OCR识别服务)
     *
     * @param filePathList
     * @param separator    分隔符
     * @return
     */
    public static String getTextFromImageFileList(List<String> filePathList, String separator) {
        if (filePathList == null || filePathList.size() == 0) {
            return "";
        }
        StringBuilder sText = new StringBuilder();
        TAipOcr aioOcr = createTengXunAipOcrClient();
        for (String filePath : filePathList) {
            sText.append(getTextFromImageFile(aioOcr, filePath)).append(separator);
        }
        return sText.toString();
    }

    /**
     * 识别本地图片上的文本信息
     *
     * @param aipOcr
     * @param filePath 图片路径
     * @return
     */
    public static String getTextFromImageFile(TAipOcr aipOcr, String filePath) {
        return getTextFromImageFile(aipOcr, filePath, "\n");//默认换行分隔符
    }

    /**
     * 识别本地图片上的文本信息
     *
     * @param aipOcr
     * @param filePath  图片路径
     * @param separator 分隔符
     * @return
     */
    public static String getTextFromImageFile(TAipOcr aipOcr, String filePath, String separator) {
        if (aipOcr == null) {
            aipOcr = createTengXunAipOcrClient();
        }
        StringBuilder sText = new StringBuilder();
        for (int i = 0; i < 5; i++) {//最多进行5次尝试识别该图片内容
            try {
                String fPath = filePath.toUpperCase().trim();
                if (!fPath.endsWith(".JPG") && !fPath.endsWith(".PNG") && !fPath.endsWith(".BMP")) {
                    log.warn("[腾讯OCR]不支持的图片格式，目前支持jpg,png,bmp图片格式");
                    break;
                }
                if (!new File(filePath).exists()) {
                    throw new FileNotFoundException("文件" + filePath + "不存在");
                }
                ImageUtil.compressImgTo1M(filePath);//压缩图片至1M以下
                //访问通用OCR识别，获取结果
                String result = aipOcr.generalOcr(filePath);
                log.debug(result);
                if (getGeneralResult(sText, result, separator)) {
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
     * @param sText
     * @param result    腾讯返回的JSON应答串
     * @param separator 分隔符
     * @return
     */
    private static boolean getGeneralResult(StringBuilder sText, String result, String separator) {
        boolean pResult = false;
        try {
            JsonNode jResult = JsonUtil.getJson(result);
            if (JsonUtil.getJsonIntValue(jResult, "ret", -1) == 0) {//成功
                JsonNode jItemList = JsonUtil.queryJsonArrayForce(jResult, "data.item_list");
                for (JsonNode jItem : jItemList) {
                    sText.append(JsonUtil.getJsonStringValue(jItem, "itemstring")).append(separator);
                }
                pResult = true;
            }
        } catch (Exception e) {
            log.error("getGeneralResult error,result={}", result, e);
        }
        return pResult;
    }

    /**
     * 解析PDF中的图片成文本
     *
     * @param filePath
     * @param attachmentName
     * @return
     */
    public static String getTextStrFromPDFImg(String filePath, String attachmentName) {
        return getTextStrFromPDFImg(filePath, attachmentName, "\n");//默认为换行分隔符
    }

    /**
     * 解析PDF中的图片成文本
     *
     * @return
     * @author pxu 2018-08-22
     */
    public static String getTextStrFromPDFImg(String filePath, String attachmentName, String separator) {
        if (!attachmentName.toLowerCase().endsWith(".pdf")) {
            log.warn("{}/{} is not pdf file，can not convert to image！", filePath, attachmentName);
            return "";
        }
        //将pdf转换为图片
        List<File> pngList = BaiduOCRUtil.pdf2image(filePath, attachmentName, false);
        //解析image
        StringBuilder sbs = new StringBuilder();
        for (File f : pngList) {
            ImageUtil.compressImgTo1M(f.getPath());//压缩图片至1M以下
            sbs.append(getTextFromImageFile(f.getPath())).append(separator);//调用OCR
            //FileUtil.del(f);//删除png文件
        }
        return sbs.toString();
    }

    /**
     * 解析TIF
     *
     * @return
     */
    public static String getTextStrFromTIFFile(String filePath, String attachmentName) {
        return getTextStrFromTIFFile(filePath, attachmentName, "\n");//默认为换行分隔符
    }

    /**
     * 解析TIF
     *
     * @return
     * @throws Exception
     */
    public static String getTextStrFromTIFFile(String filePath, String attachmentName, String separator) {
        //将tif转换为jpg图片
        String entirePathName = filePath + File.separator + attachmentName;
        String[] dirs = attachmentName.split("\\.");
        File dirFile = new File(filePath + File.separator + dirs[0]);
        FileUtil.mkdir(dirFile);
        FileUtil.copy(entirePathName, dirFile + File.separator + attachmentName, true);
        List<String> jpgList = ImageForematConvert.tif2Jpg(dirFile + File.separator + attachmentName);
        FileUtil.del(dirFile + File.separator + attachmentName);

        StringBuilder sbs = new StringBuilder();
        for (String jpg : jpgList) {
            ImageUtil.compressImgTo1M(jpg);//压缩图片至1M以下
            sbs.append(getTextFromImageFile(createTengXunAipOcrClient(), jpg, separator));
        }
        //删除文件夹 dirName
        FileUtil.del(dirFile);

        return sbs.toString();
    }

    /**
     * 创建一个腾讯AI开放平台访问客户端对象
     *
     * @return
     */
    public static TAipOcr createTengXunAipOcrClient() {
        return createTengXunAipOcrClient(taipAppId, taipAppKey);
    }

    /**
     * 创建一个腾讯AI开放平台访问客户端对象，appid和appkey可外部传入
     *
     * @param taipAppId
     * @param taipAppKey
     * @return
     */
    public static TAipOcr createTengXunAipOcrClient(String taipAppId, String taipAppKey) {
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