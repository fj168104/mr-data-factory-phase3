package com.mr.proxy.httpbrowser;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.mr.proxy.IPModel.IPMessage;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.text.Document;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class HtmlUnit66IPResponse {

    public String getHtmlPage(String url){
        String ipStr ="";

        //设置浏览器版本
        WebClient wc = new WebClient(BrowserVersion.CHROME);
        //是否使用不安全的SSL
        wc.getOptions().setUseInsecureSSL(true);
        //启用JS解释器，默认为true
        wc.getOptions().setJavaScriptEnabled(false);
        //禁用CSS
        wc.getOptions().setCssEnabled(false);
        //js运行错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnScriptError(false);
        //状态码错误时，是否抛出异常
        wc.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //是否允许使用ActiveX
        wc.getOptions().setActiveXNative(false);
        //等待js时间
        wc.waitForBackgroundJavaScript(600*1000);
        //设置Ajax异步处理控制器即启用Ajax支持
        wc.setAjaxController(new NicelyResynchronizingAjaxController());
        //不跟踪抓取
        wc.getOptions().setDoNotTrackEnabled(false);
        //
        wc.getOptions().setRedirectEnabled(true);
        //
        wc.getCache().clear();
        //
        wc.getCookieManager().clearCookies();
        //
        wc.setRefreshHandler(new ImmediateRefreshHandler());
        try {
            //模拟浏览器打开一个目标网址
            HtmlPage htmlPage = wc.getPage(url);
            //为了获取js执行的数据 线程开始沉睡等待
            Thread.sleep(1000);//这个线程的等待 因为js加载需要时间的
            //以xml形式获取响应文本
            String xml = htmlPage.asXml();
            org.jsoup.nodes.Document doc = Jsoup.parse(xml);
            Element element = doc.getElementsByTag("body").get(0);
            ipStr = element.text();
            //并转为Document对象return

        }catch (InterruptedException e){
            e.getMessage();
        }catch (FailingHttpStatusCodeException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ipStr;

    }

    /**
     * 准备库
     * @return
     */
    public List<IPMessage> AddIpMessage(){
        //高级匿名IP
        String urlHeight = "http://www.66ip.cn/nmtq.php?getnum=320&isp=0&anonymoustype=0&start=&ports=&export=&ipaddress=&area=0&proxytype=2&api=66ip";
        String urlAll = "http://www.66ip.cn/mo.php?sxb=&tqsl=50000&port=&export=&ktip=&sxa=&submit=%CC%E1++%C8%A1&textarea=http%3A%2F%2Fwww.66ip.cn%2F%3Fsxb%3D%26tqsl%3D5000%26ports%255B%255D2%3D%26ktip%3D%26sxa%3D%26radio%3Dradio%26submit%3D%25CC%25E1%2B%2B%25C8%25A1";
        List<IPMessage> listIPMessage = new ArrayList<>();
        int i = 0;
        String[] ipListHeight = getHtmlPage(urlHeight).split(" ");
        String[] ipListAll = getHtmlPage(urlAll).split(" ");
        for(String ip : ipListHeight){
            IPMessage ipMessage = new IPMessage();
            ip = ip.trim();
            String[] ipProt = ip.split(":");
            if(ipProt.length ==2){
                ipMessage.setIPAddress(ipProt[0]);
                ipMessage.setIPPort(ipProt[1]);
                listIPMessage.add(ipMessage);
            }

        }
        for(String ip : ipListAll){
            IPMessage ipMessage = new IPMessage();
            ip = ip.trim();
            String[] ipProt = ip.split(":");
            if(ipProt.length ==2){
                ipMessage.setIPAddress(ipProt[0]);
                ipMessage.setIPPort(ipProt[1]);
                listIPMessage.add(ipMessage);
            }

        }
        return listIPMessage;
    }
}
