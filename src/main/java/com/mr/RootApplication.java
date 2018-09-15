package com.mr;

import com.mr.common.OCRUtil;
import com.mr.common.base.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;

/**
 *
 * Created by JK on 2017/1/17.
 */
@SpringBootApplication
@EnableTransactionManagement // 开启注解事务管理，等同于xml配置文件中的 <tx:annotation-driven />
@MapperScan(basePackages = "com.mr.*", markerInterface = BaseMapper.class)
//@EnableAspectJAutoProxy(proxyTargetClass = true)
@Slf4j
public class RootApplication {
   public static void main(String[] args) {

       SpringApplication.run(RootApplication.class, args);

        /**
         * 部署程序，需要将opencv_java341.dll或者linux(so)或者Mac(dylib)放到相应目录：path
         */
       log.info("程序加载···opencv···中！");
       String systemType = System.getProperty("os.name");
       String path = OCRUtil.DOWNLOAD_DIR;
       if(systemType.toLowerCase().startsWith("win")){
           System.load(path+File.separator+"opencv_java341.dll");
       }else if(systemType.toLowerCase().startsWith("mac")) {
           System.load(path+File.separator+"opencv_java341.dylib");
       }else {
           System.load(path+File.separator+"opencv_java341.so");
       }
       log.info("程序加载···opencv···完成！");
       log.info("****************************程序运行在{} 上*****************************",systemType);
    }

}
