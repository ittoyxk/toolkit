package com.xk.io;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaokang on 2017-05-13.
 */
public class FileFunc {
    private static Map<String, String> configFile = new ConcurrentHashMap();

    public static String hexStr2String(String bytestr)
    {
        int ll = bytestr.length();
        if (ll % 2 != 0) return null;
        try
        {
            byte[] bt_file = new byte[ll / 2];
            for (int i = 0; i < ll / 2; i++)
            {
                bt_file[i] = (byte)Integer.parseInt(bytestr.substring(i * 2, i * 2 + 2), 16);
            }
            return new String(bt_file, 0, bt_file.length, "GBK");
        }
        catch (Exception e)
        {
            System.out.println("com.taikang.utils.FileFunc.byteStr2String:" + e);
        }return null;
    }

    public static boolean saveBytes2File(String filename, byte[] bbb)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(filename);

            out.write(bbb);
            out.close();
            return true;
        }
        catch (Exception e)
        {
            System.out.println("com.taikang.utils.FileFunc.saveFile" + e);
        }return false;
    }

    public static void logInfo(String filename, String info)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(filename);
            byte[] bt_prompt = info.getBytes("GBK");
            out.write(bt_prompt);
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("com.taikang.utils.FileFunc.logInfo:" + e);
        }
    }

    public static void logInfo(String filename, String info, boolean append)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(filename, append);
            byte[] bt_prompt = info.getBytes("GBK");
            out.write(bt_prompt);
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("com.taikang.utils.FileFunc.logInfo:" + e);
        }
    }

    public static void logInfo(String filename, String info, boolean append, String encode)
    {
        try
        {
            FileOutputStream out = new FileOutputStream(filename, append);
            byte[] bt_prompt = info.getBytes(encode);
            out.write(bt_prompt);
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("com.taikang.utils.FileFunc.logInfo:" + e);
        }
    }

    public static String getProValue(String path, String keyName)
    {
        PropertiesFileMonitor monitor_agent = PropertiesFileMonitor.getInstance();

        monitor_agent.addFile(new File(path));

        return monitor_agent.getProperty(path, keyName);
    }



    public static boolean clear() {
        try {
            configFile.clear();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }return false;
    }
}
