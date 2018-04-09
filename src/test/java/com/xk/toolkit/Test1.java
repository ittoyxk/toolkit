package com.xk.toolkit;

import com.xk.httpclient.common.HttpConfig;
import com.xk.httpclient.common.util.PropertiesUtil;

/**
 * Created by xiaokang on 2017-05-12.
 */
public class Test1 {
    public static void main(String[] args)
    {
        HttpConfig hc=HttpConfig.custom();

        try {
            //System.out.println(HttpClientUtil.get(hc.url("http://www.baidu.com")));
          //  String value=FileFunc.getProValue(PathKit.getRootClassPath()+ File.separator+"log4j.properties","log4j.appender.stdout.layout");
            String value= PropertiesUtil.getProperty("application.properties","jdbc_datasource");
            System.out.print(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
