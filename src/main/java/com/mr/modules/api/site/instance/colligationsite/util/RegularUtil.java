package com.mr.modules.api.site.instance.colligationsite.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: zqzhou
 * @Description: 提供正则匹配，截取字段，type说明：0-汉字，1-字母，2-数字，3-字母&数字
 * @Date: Created in 2018/8/20 13:54
 */
public class RegularUtil {



    /**
     * 汇总的入口
     * */
    public static String regEx(String type,String str){
        String result = "";
        if(type.equalsIgnoreCase("0")){
            result = regChn(str);
        }else if(type.equalsIgnoreCase("1")){
            result = regEng(str);
        }else if(type.equalsIgnoreCase("2")){
            result = regNum(str);
        }else if(type.equalsIgnoreCase("3")){
            result = regEngNum(str);
        }
        return result;
    }

    /**
     * 识别汉字
     * */
    public static String regChn(String str){
        String regEx="[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(m.find()){
            sb.append(m.group());
        }
        return sb.toString();
    }

    /**
     * 识别英文字母
     * */
    public static String regEng(String str){
        String regEx="[a-zA-Z]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(m.find()){
            sb.append(m.group());
        }
        return sb.toString();
    }

    /**
     * 识别数字
     * */
    public static String regNum(String str){
        String regEx="[0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(m.find()){
            sb.append(m.group());
        }
        return sb.toString();
    }

    /**
     * 识别字母和数字
     * */
    public static String regEngNum(String str){
        String regEx="[a-zA-Z0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while(m.find()){
            sb.append(m.group());
        }
        return sb.toString();
    }

}
