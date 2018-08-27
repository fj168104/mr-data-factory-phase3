package com.mr.common.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;

/**
 * 图片工具
 *
 * @author pxu 2018/8/27 14:07
 */
@Slf4j
public class ImageUtil {

    /**
     * 压缩图片至1M以下
     *
     * @param imgPath
     */
    public static void compressImgTo1M(String imgPath) {
        compressImg(1024 * 1024, imgPath);
    }

    /**
     * 压缩图片
     *
     * @param targetLength 目标大小
     * @param imgPath      图片路径
     */
    public static void compressImg(long targetLength, String imgPath) {
        File f = new File(imgPath);
        int iTime = 0;//记录压缩次数
        while (f.length() > targetLength) {//图片大于1M，进行压缩
            log.info("第{}次压缩{}，当前图片大小{},目标大小{}", ++iTime, imgPath, f.length(), targetLength);
            try {
                //每次按照如下参数进行压缩
                Thumbnails.of(f).scale(0.8f).outputQuality(0.6f).toFile(f.getPath());
            } catch (IOException e) {
                log.warn("压缩图片异常", e);
            }
            if (iTime == 10) {//最多进行10次压缩
                break;
            }
        }
    }

}
