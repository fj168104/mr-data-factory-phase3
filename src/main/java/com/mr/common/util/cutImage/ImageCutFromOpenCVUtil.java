package com.mr.common.util.cutImage;

import com.mr.common.util.AIOCRUtil;
import com.mr.common.util.BaiduOCRUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
@Component
@Slf4j
public class ImageCutFromOpenCVUtil {

    @Value("${system_java_lib_opencv}")
    private  String SYSTEM_JAVA_LIB_OPENCV ;

    public static void main(String[] args){
        //加载系统OpenCV包文件
        /*if(SYSTEM_JAVA_LIB_OPENCV == null){
            log.info("******************{}*******************","找不到opencv_java341.dll文件");
            System.load("F:\\20171010微策战团\\Codelibaray\\mr-data-factory-phase3\\mr-data-factory-phase3\\src\\main\\libs/opencv_java341.dll");
        }else {
            log.info("******************{}*******************",SYSTEM_JAVA_LIB_OPENCV);
            System.load(SYSTEM_JAVA_LIB_OPENCV);
        }*/
        System.load("F:\\20171010微策战团\\Codelibaray\\mr-data-factory-phase3\\mr-data-factory-phase3\\src\\main\\libs/opencv_java341.dll");
        String basePath ="C:\\Users\\Space\\Desktop\\xy\\";
        String fileName = "tmp.jpg";
        String fileTempPath ="C:\\Users\\Space\\Desktop\\xy\\temp\\";


        /*AIOCRUtil aiocrUtil = new AIOCRUtil();
		aiocrUtil.setTaipAppId("2107602349");
		aiocrUtil.setTaipAppKey("ft510AJ3xrB5pfK9");

		StringBuffer stringBuffer = new StringBuffer("");
		for(String strFileName :listFileName){
            stringBuffer.append(AIOCRUtil.getTextFromImageFile(strFileName).replaceAll("\\|","")).append(" ");
        }
        log.info(stringBuffer.toString());*/

        //百度
        BaiduOCRUtil baiduOCRUtil = new BaiduOCRUtil();
        baiduOCRUtil.setAppId("11641746");
        baiduOCRUtil.setAppKey("amGikVFG3DMxS70DorUNUlvR");
        baiduOCRUtil.setSecretKey("1Ze4bta22sW70V5bvlexbnB8b5kbNXqG");

        //切割图片
        String listFileName = new ImageCutFromOpenCVUtil().imgTextExtract(basePath,fileName, fileTempPath);

        log.info(listFileName);


    }

    /**
     * 提取img文本
     * @param basePath
     * @param fileName
     * @param fileTempPath
     * @return
     */
    public String imgTextExtract(String basePath,String fileName,String fileTempPath){
        String fileSourceName = fileName;
        String fileTempSourceName = "tmp.jpg";
        String text = "";
        try {
            copyFileUsingApacheCommonsIO(new File(basePath+fileSourceName),new File(basePath+fileTempSourceName));
        } catch (IOException e) {
            log.info("创建临时源文件IOException，请检查···"+e.getMessage());
        }

        StringBuffer stringBuffer = new StringBuffer("");
        List<String> listFileName = imgCut(basePath,fileTempSourceName, fileTempPath);
        for(String strFileName :listFileName){
            stringBuffer.append(BaiduOCRUtil.getTextStrFromImageFile(strFileName,"")).append(" ");
        }
        log.info("表格识别结果为：{}",stringBuffer.toString());

        if(stringBuffer.toString().trim().equals("")){
            for(String strFileName :listFileName){
                stringBuffer.append(AIOCRUtil.getTextFromImageFile(strFileName)).append(" ");
            }
        }
        text = stringBuffer.toString();
        //清除临时源文件
        File fileDel = new File(basePath+fileTempSourceName);
        if (fileDel.isFile()) {
            fileDel.delete();
        }

        return text.replaceAll("（","(").replaceAll("）",")");
    }

    /**
     * 将表格按单员格切分
     * @param basePath
     * @param fileTempSourceName
     * @param fileTempPath
     * @return 单元格图片目录清单
     */
    public List<String> imgCut(String basePath,String fileTempSourceName,String fileTempPath){
        List<String> fileList = new ArrayList<>();
        //二值化
        binaryzation(basePath,fileTempSourceName);
        log.info("**********************{}*******************","切图开始");
        long startTime = System.currentTimeMillis();

        File dir = new File(fileTempPath);
        if(!dir.exists()){
            dir.mkdir();
        }
       // 清空切割表存放的临时目录
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
        }

        Mat src = Imgcodecs.imread(basePath+fileTempSourceName);
        if (src.empty()) {
            System.out.println("not found file");
            return null;
        }

        Mat gray = new Mat();
        Mat erod = new Mat();
        Mat blur = new Mat();
        int src_height = src.cols(), src_width = src.rows();
        // 先转为灰度 cvtColor(src, gray, COLOR_BGR2GRAY);
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        /**
         * 腐蚀（黑色区域变大） Mat element = getStructuringElement(MORPH_RECT,
         * Size(erodeSize, erodeSize)); erode(gray, erod, element);
         */
        int erodeSize = src_height / 200;
        if (erodeSize % 2 == 0) {
            erodeSize++;
        }
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(erodeSize, erodeSize));
        Imgproc.erode(gray, erod, element);

        // 高斯模糊化
        int blurSize = src_height / 200;
        if (blurSize % 2 == 0) {
            blurSize++;
        }
        Imgproc.GaussianBlur(erod, blur, new Size(blurSize, blurSize), 0, 0);
        // 封装的二值化 adaptiveThreshold(~gray, thresh, 255,
        Mat thresh = gray.clone();
        Mat xx = new Mat();
        Core.bitwise_not(gray, xx);// 反色
        Imgproc.adaptiveThreshold(xx, thresh, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, -2);
		/*
		 * 这部分的思想是将线条从横纵的方向处理后抽取出来，再进行交叉，矩形的点，进而找到矩形区域的过程
		 */
        // Create the images that will use to extract the horizonta and vertical
        // lines
        // 使用二值化后的图像来获取表格横纵的线
        Mat horizontal = thresh.clone();
        Mat vertical = thresh.clone();
        // 这个值越大，检测到的直线越多 TODO
        String parameter = "20";
        if (parameter == null || parameter.equals("")) {
            parameter = "20";
        }
        int scale = Integer.parseInt(parameter); // play with this variable in

        // 使用这个变量来增加/减少待检测的行数

        // Specify size on horizontal axis 指定水平轴上的大小
        int horizontalsize = horizontal.cols() / scale;

        // 为了获取横向的表格线，设置腐蚀和膨胀的操作区域为一个比较大的横向直条
        Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));
        // 先腐蚀再膨胀
        // iterations 最后一个参数，迭代次数，越多，线越多。在页面清晰的情况下1次即可。
        Imgproc.erode(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(horizontal, horizontal, horizontalStructure, new Point(-1, -1), 1);
        // Specify size on vertical axis 同上
        int verticalsize = vertical.rows() / scale;
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));
        Imgproc.erode(vertical, vertical, verticalStructure, new Point(-1, -1), 1);
        Imgproc.dilate(vertical, vertical, verticalStructure, new Point(-1, -1), 1);
		/*
		 * 合并线条 将垂直线，水平线合并为一张图
		 */
        Mat mask = new Mat();
        Core.add(horizontal, vertical, mask);
		/*
		 * 通过 bitwise_and 定位横线、垂直线交汇的点
		 */
        Mat joints = new Mat();
        Core.bitwise_and(horizontal, vertical, joints);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE,
                new Point(0, 0));

        List<MatOfPoint> contours_poly = contours;
        Rect[] boundRect = new Rect[contours.size()];
        List<Mat> tables = new ArrayList<Mat>();
        // my
        List<Rect> haveReacts = new ArrayList();
        Map<String, Map<String, Map<String, Double>>> mappoint = new HashMap<String, Map<String, Map<String, Double>>>();
        // 循环所有找到的轮廓-点
        for (int i = 0; i < contours.size(); i++) {
            // 每个表的点
            MatOfPoint point = contours.get(i);
            MatOfPoint contours_poly_point = contours_poly.get(i);
            double area = Imgproc.contourArea(contours.get(i));
            // 如果小于某个值就忽略，代表是杂线不是表格
            if (area < 100) {
                continue;
            }
            Imgproc.approxPolyDP(new MatOfPoint2f(point.toArray()), new MatOfPoint2f(contours_poly_point.toArray()), 3,
                    true);
            // 为将这片区域转化为矩形，此矩形包含输入的形状
            boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));
            // 找到交汇处的的表区域对象
            Mat table_image = joints.submat(boundRect[i]);
            List<MatOfPoint> table_contours = new ArrayList<MatOfPoint>();
            Mat joint_mat = new Mat();
            Imgproc.findContours(table_image, table_contours, joint_mat, Imgproc.RETR_CCOMP,
                    Imgproc.CHAIN_APPROX_SIMPLE);
            // 从表格的特性看，如果这片区域的点数小于4，那就代表没有一个完整的表格，忽略掉
            if (table_contours.size() < 4) {
                continue;
            }
            // 表格里面的每个点
            Map<String, Double> x_zhis = new HashMap<String, Double>();
            Map<String, Double> y_zhis = new HashMap<String, Double>();
            for (MatOfPoint matOfPoint : table_contours) {
                Point[] array = matOfPoint.toArray();
                for (Point point2 : array) {
                    x_zhis.put("x" + point2.x, point2.x);
                    y_zhis.put("y" + point2.y, point2.y);
                }
            }
            haveReacts.add(boundRect[i]);
            Map<String, Map<String, Double>> x = new HashMap<String, Map<String, Double>>();
            x.put("x", x_zhis);
            x.put("y", y_zhis);
            mappoint.put("key" + (haveReacts.size() - 1), x);
            // 保存图片
            tables.add(src.submat(boundRect[i]).clone());
            // 将矩形画在原图上
            Imgproc.rectangle(src, boundRect[i].tl(), boundRect[i].br(), new Scalar(255, 0, 255), 1, 8, 0);
        }
        // 页面数据
        Map<String, String> jspdata = new HashMap<String, String>();
        for (int i = 0; i < tables.size(); i++) {
            Mat table = tables.get(i);
            Rect rect = haveReacts.get(i);
            int width = rect.width, height = rect.height;
            Map<String, Map<String, Double>> mapdata = mappoint.get("key" + i);
            int[] x_z = maptoint(mapdata.get("x"));
            int[] y_z = maptoint(mapdata.get("y"));
            // 纵切
            String px_biao = "";
            if (px_biao == null || px_biao.equals("")) {
                px_biao = "5";
            }
            int x_len = 0, x_biao = Integer.parseInt(px_biao);
            List<Mat> mats = new ArrayList<Mat>();
            for (int j = 0; j < x_z.length; j++) {
                if (j == 0) {
                    Mat img = new Mat(table, new Rect(0, 0, x_z[j], height));
                    if (img.cols() > x_biao) {
                        mats.add(img);
                        x_len++;
                    }
                } else {
                    Mat img = new Mat(table, new Rect(x_z[j - 1], 0, x_z[j] - x_z[j - 1], height));
                    if (img.cols() > x_biao) {
                        mats.add(img);
                        x_len++;
                    }
                    if (j == x_z.length - 1) {// 最后一个处理
                        Mat img1 = new Mat(table,
                                new Rect(x_z[x_z.length - 1], 0, width - x_z[x_z.length - 1], height));
                        if (img.cols() > x_biao) {
                            mats.add(img1);
                        }
                    }
                }
            }
            /*imshow(fileTemp, table, "table_" + i + ".png");// 当前table图*/
            // 横切保存 TODO
            String py_biao = "";
            if (py_biao == null || py_biao.equals("")) {
                py_biao = "5";
            }
            int y_len = 0, y_biao = Integer.parseInt(py_biao);
            for (int j = 0; j < mats.size(); j++) {
                Mat mat = mats.get(j);
                int tuwidth = mat.cols(), tugao = mat.rows();
                int cy_len = 0;
                for (int k = 0; k < y_z.length; k++) {
                    if (k == 0) {
                        Mat img = new Mat(mat, new Rect(0, 0, tuwidth, y_z[k]));
                        if (img.rows() > y_biao) {

                            imshow(fileTempPath, img, "table_" + i + "_" + j + "_" + cy_len + ".jpg");
                            fileList.add(fileTempPath+"table_" + i + "_" + j + "_" + cy_len + ".jpg");
                            cy_len++;
                        }
                    } else {
                        Mat img = new Mat(mat, new Rect(0, y_z[k - 1], tuwidth, y_z[k] - y_z[k - 1]));
                        if (img.rows() > y_biao) {
                            imshow(fileTempPath, img, "table_" + i + "_" + j + "_" + cy_len + ".jpg");
                            fileList.add(fileTempPath+"table_" + i + "_" + j + "_" + cy_len + ".jpg");
                            cy_len++;
                        }
                        if (k == y_z.length - 1) {// 最后一个处理
                            Mat img1 = new Mat(mat, new Rect(0, y_z[k], tuwidth, tugao - y_z[k]));
                            if (img.rows() > y_biao) {
                                imshow(fileTempPath, img1, "table_" + i + "_" + j + "_" + (cy_len) + ".jpg");
                                fileList.add(fileTempPath+"table_" + i + "_" + j + "_" + cy_len + ".jpg");
                            }
                        }
                    }
                }
                y_len = cy_len;
            }
            // 保存数据信息
            jspdata.put("table_" + i, x_len + "_" + y_len);
        }
        long endTime = System.currentTimeMillis();
        log.info("**********************{}*******************","切图结束");
        log.info("**********************耗时：{}*******************",endTime-startTime);
        return fileList;
    }

    /**
     * 子图片存储
     * @param fileTemp
     * @param dst
     * @param name
     */
    public void imshow(String fileTemp, Mat dst, String name) {
        Imgcodecs.imwrite(fileTemp + name, dst);
    }

    /**
     * 值计算
     * @param x
     * @return
     */
    public int[] maptoint(Map<String, Double> x) {
        int[] vaule = new int[x.size()];
        int num = 0;
        for (Map.Entry<String, Double> m : x.entrySet()) {
            vaule[num] = m.getValue().intValue();
            num++;
        }
        Arrays.sort(vaule);
        return vaule;
    }

    /**
     * 二值化
     * @param basePath
     * @param fileName
     */
    public void binaryzation(String basePath,String fileName) {
        // 这个必须要写,不写报java.lang.UnsatisfiedLinkError
        /*System.loadLibrary(Core.NATIVE_LIBRARY_NAME);*/
        File imgFile = new File(basePath+fileName);
        //先经过一步灰度化
        Mat src = Imgcodecs.imread(imgFile.toString());
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        src = gray;
        //二值化
        binaryzation(src);
        Imgcodecs.imwrite(basePath+ imgFile.getName(), src);
    }

    public Mat binaryzation(Mat mat) {
        int BLACK = 0;
        int WHITE = 255;
        int ucThre = 0, ucThre_new = 127;
        int nBack_count, nData_count;
        int nBack_sum, nData_sum;
        int nValue;
        int i, j;

        int width = mat.width(), height = mat.height();
        //寻找最佳的阙值
        while (ucThre != ucThre_new) {
            nBack_sum = nData_sum = 0;
            nBack_count = nData_count = 0;

            for (j = 0; j < height; ++j) {
                for (i = 0; i < width; i++) {
                    nValue = (int) mat.get(j, i)[0];

                    if (nValue > ucThre_new) {
                        nBack_sum += nValue;
                        nBack_count++;
                    } else {
                        nData_sum += nValue;
                        nData_count++;
                    }
                }
            }

            nBack_sum = nBack_sum / nBack_count;
            nData_sum = nData_sum / nData_count;
            ucThre = ucThre_new;
            ucThre_new = (nBack_sum + nData_sum) / 2;
        }

        //二值化处理
        int nBlack = 0;
        int nWhite = 0;
        for (j = 0; j < height; ++j) {
            for (i = 0; i < width; ++i) {
                nValue = (int) mat.get(j, i)[0];
                if (nValue > ucThre_new) {
                    mat.put(j, i, WHITE);
                    nWhite++;
                } else {
                    mat.put(j, i, BLACK);
                    nBlack++;
                }
            }
        }

        // 确保白底黑字
        if (nBlack > nWhite) {
            for (j = 0; j < height; ++j) {
                for (i = 0; i < width; ++i) {
                    nValue = (int) (mat.get(j, i)[0]);
                    if (nValue == 0) {
                        mat.put(j, i, WHITE);
                    } else {
                        mat.put(j, i, BLACK);
                    }
                }
            }
        }
        return mat;
    }

    private static void copyFileUsingApacheCommonsIO(File source, File dest)
         throws IOException {
             FileUtils.copyFile(source, dest);
    }
}
