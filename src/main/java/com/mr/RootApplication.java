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

       String systemType = System.getProperty("os.name");

       log.info("程序加载···opencv···中！");
       /**
        * 部署程序，需要将opencv_java341.dll或者os放到相应目录：DOWNLOAD_DIR
        */
       if(systemType.toLowerCase().startsWith("win")){
           System.load(OCRUtil.DOWNLOAD_DIR+File.separator+"opencv_java341.dll");
       }else {
           System.load(OCRUtil.DOWNLOAD_DIR+File.separator+"opencv_java341.os");
       }
       log.info("程序加载···opencv···完成！");
       log.info("****************************程序运行在{} 上*****************************",systemType);
    }
}
