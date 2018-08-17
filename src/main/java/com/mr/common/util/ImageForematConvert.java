package com.mr.common.util;



import com.sun.media.jai.codec.*;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.awt.image.BufferedImage;
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
     */
    public static List<String>  tif2Jpg(String fileAbsolutePath) {
        List<String> fileAllNameList = new ArrayList<>();
        ImageInputStream input;
        try {
            input = ImageIO.createImageInputStream(new File(fileAbsolutePath));//以图片输入流形式读取到tif
            // Get the reader
            ImageReader reader = ImageIO.getImageReaders(input).next();//获得image阅读器，阅读对象为tif文件转换的流
            String filePath =fileAbsolutePath.substring(0, fileAbsolutePath.lastIndexOf("."));

            try {
                reader.setInput(input);
                // Read page 2 of the TIFF file
                int count = reader.getNumImages(true);//tif文件页数
                //System.out.println(count);
                for(int i = 0; i < count; i++){
                    BufferedImage image = reader.read(i, null);//取得第i页
                    String filePathSub = filePath+"_"+i+".jpg";
                    File f = new File(filePathSub);
                    try {
                        if(!f.exists()){
                            f.getParentFile().mkdirs();
                            f.createNewFile();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ImageIO.write(image, "JPEG", f);//保存图片
                    fileAllNameList.add(filePathSub);
                }
            }
            finally {
                reader.dispose();
                input.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return fileAllNameList;
    }
    /**
     * 此方法存在一个问题，如果文本直接转换为tiff格式的图片，识别不了
     * 图片全路径
     * @param fileAbsolutePath
     * @return
     */
    public static List<String>  tif2JpgOld(String fileAbsolutePath) {
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
        for(String str:tif2Jpg("C:\\Users\\Space\\Desktop\\（凭关缉罚字〔2018〕0061号）凭祥市达星贸易有限公司.tiff")){
            log.info("--------------"+str);
        }
        /* tif 转 jpg 格式*/
        //tif2Jpg("C:\\Users\\Space\\Desktop\\（凭关缉罚字〔2018〕0061号）凭祥市达星贸易有限公司.tiff");
        /* jpg 转 tif 格式*/
        //imageConvert.jpg2Tif("F:\\20171010微策战团\\other\\江哥\\微策架构.jpg");
    }
}
