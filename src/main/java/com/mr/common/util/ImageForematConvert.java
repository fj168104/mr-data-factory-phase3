package com.mr.common.util;



import com.sun.media.jai.codec.*;
import lombok.extern.slf4j.Slf4j;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片格式转换
 * @Auther zjxu
 * @DateTIme 2018-08
 * 主要功能处理：
 * TIFF convert jpg
 * jpg convert tif
 */

@Slf4j
public class ImageForematConvert {
    /**
     * 图片全路径
     * @param fileAbsolutePath
     * @return
     */
    public static List<String>  tif2Jpg(String fileAbsolutePath) {
        List<String> fileAllNameList = new ArrayList<>();
        if (fileAbsolutePath == null || "".equals(fileAbsolutePath.trim())){
            return fileAllNameList;
        }
        if (!new File(fileAbsolutePath).exists()){
            log.info("系统找不到指定文件【"+fileAbsolutePath+"】");
            return fileAllNameList;
        }
        FileSeekableStream fileSeekStream = null;
        try {
            fileSeekStream = new FileSeekableStream(fileAbsolutePath);
            TIFFEncodeParam tiffEncodeParam = new TIFFEncodeParam();
            JPEGEncodeParam jpegEncodeParam = new JPEGEncodeParam();
            ImageDecoder dec = ImageCodec.createImageDecoder("tiff", fileSeekStream, null);
            int count = dec.getNumPages();
            tiffEncodeParam.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4);
            tiffEncodeParam.setLittleEndian(false);
            log.info("该tif文件共有【" + count + "】页");
            String filePathPrefix = fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf("."));
            for (int i = 0; i < count; i++) {
                RenderedImage renderedImage = dec.decodeAsRenderedImage(i);
                File imgFile = new File(filePathPrefix + "_" + i + ".jpg");
                log.info("每页分别保存至： " + imgFile.getCanonicalPath());
                ParameterBlock pb = new ParameterBlock();
                pb.addSource(renderedImage);
                pb.add(imgFile.toString());
                pb.add("JPEG");
                pb.add(jpegEncodeParam);
                RenderedOp renderedOp = JAI.create("filestore",pb);
                renderedOp.dispose();
                fileAllNameList.add(imgFile.getCanonicalPath());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fileSeekStream != null){
                try {
                    fileSeekStream.close();
                } catch (IOException e) {
                }
                fileSeekStream = null;
            }
        }
        return fileAllNameList;
    }

    /**
     *
     * @param fileAbsolutePath 图片全路径
     * @return
     */
    public static String jpg2Tif(String fileAbsolutePath) {
        String fileAllName = "";
        OutputStream outputStream = null;
        try {
            RenderedOp renderOp = JAI.create("fileload", fileAbsolutePath);
            String tifFilePath = fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf("."))+".tif";
            outputStream = new FileOutputStream(tifFilePath);
            TIFFEncodeParam tiffParam = new TIFFEncodeParam();
            ImageEncoder imageEncoder = ImageCodec.createImageEncoder("TIFF", outputStream, tiffParam);
            imageEncoder.encode(renderOp);
            fileAllName = tifFilePath;
            log.info("jpg2Tif 保存至： " + tifFilePath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
                outputStream = null;
            }
        }
        return fileAllName;
    }

    public static void main(String args[]) throws Exception{
        /* tif 转 jpg 格式*/
        tif2Jpg("F:\\20171010微策战团\\other\\江哥\\微策架构.tif");
        /* jpg 转 tif 格式*/
        //imageConvert.jpg2Tif("F:\\20171010微策战团\\other\\江哥\\微策架构.jpg");
    }
}
