package com.xk.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xiaokang on 2017-05-13.
 */
public class PropertiesFileMonitor extends FileMonitor {
    private ConcurrentHashMap<String, Properties> properties_file_content_cache;

    public static PropertiesFileMonitor getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    public PropertiesFileMonitor(long pollingInterval)
    {
        super(pollingInterval);
        this.properties_file_content_cache = new ConcurrentHashMap();
        addListener(new PropertiesFileListener());
    }

    public void addProperties(File file)
    {
        Properties props = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(file));
            props.load(in);
            this.properties_file_content_cache.put(file.getPath(), props);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeProperties(File properites_file)
    {
        if (!this.properties_file_content_cache.containsKey(properites_file.getName())) {
            this.properties_file_content_cache.remove(properites_file.getPath());
        }
    }

    public String getProperty(String fileName, String key)
    {
        return getProperty(fileName, key, "");
    }

    public String getProperty(String fileName, String key, String defaultValue)
    {
        String configuration_path = new File(fileName).getPath();
        Properties pros = (Properties) this.properties_file_content_cache.get(configuration_path);
        if (pros != null) {
            return pros.getProperty(key, defaultValue);
        }

        return defaultValue;
    }

    public void addFile(File properites_file)
    {
        super.addFile(properites_file);
        if (!this.properties_file_content_cache.containsKey(properites_file.getName())) {
            addProperties(properites_file);
        }
    }

    public void removeFile(File properites_file)
    {
        super.removeFile(properites_file);
        if (this.properties_file_content_cache.containsKey(properites_file.getName())) {
            removeProperties(properites_file);
        }
    }



    public class PropertiesFileListener   implements FileListener {
        public PropertiesFileListener()
        {
        }

        public void fileChanged(File properites_file)
        {
            PropertiesFileMonitor.this.addProperties(properites_file);
        }
    }

    private static class LazyHolder {
        private static final PropertiesFileMonitor INSTANCE = new PropertiesFileMonitor(1000L);
    }
}
