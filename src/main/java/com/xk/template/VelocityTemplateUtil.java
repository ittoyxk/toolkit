package com.xk.template;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.log.SystemLogChute;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.util.concurrent.ConcurrentHashMap;

public class VelocityTemplateUtil {
    private static ConcurrentHashMap<String, Template> templateCache = new ConcurrentHashMap<>();

    static {
        Velocity.setProperty(Velocity.RESOURCE_LOADER, "string");
        Velocity.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        Velocity.addProperty("string.resource.loader.modificationCheckInterval", "1");
        Velocity.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM_CLASS, SystemLogChute.class.getName());
        Velocity.setProperty(Velocity.OUTPUT_ENCODING, "UTF-8");
        Velocity.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        Velocity.init();
    }

    public static Template getTemplateFromString(String templeName, String tplString) {
        Template result = templateCache.get(templeName);
        if (result != null) {
            return result;
        }
        else {
            StringResourceRepository repo = StringResourceLoader.getRepository();
            repo.putStringResource(templeName, tplString);
            result = Velocity.getTemplate(templeName);
            templateCache.putIfAbsent(templeName, result);
            return result;
        }
    }
}
