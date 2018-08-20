package com.mr.modules.api.site.instance.colligationsite.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @Author: zqzhou
 * @Description:
 * @Date: Created in 2018/8/16 17:23
 */
public class FilenameFilterUtil implements FilenameFilter {

    public String tail;

    public FilenameFilterUtil(String tail) {
        tail = tail.toLowerCase();
        this.tail = tail;
    }


    @Override
    public boolean accept(File dir, String name) {
        // 创建返回值
        boolean flag = false;
        // 定义筛选条件
        //endWith(String str);判断是否是以指定格式结尾的  .jpg  .txt .doc
        if (name.toLowerCase().endsWith(tail)) {
            flag = true;
        }
        // 返回定义的返回值
        //当返回true时,表示传入的文件满足条件
        return flag;
    }

}
