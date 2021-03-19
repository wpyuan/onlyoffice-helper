package com.github.wpyuan.onlyofficehelper.infra.helper;

import com.github.wpyuan.onlyofficehelper.config.OnlyofficeProperties;
import com.github.wpyuan.onlyofficehelper.infra.util.ApplicationContextUtil;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author Administrator
 */
@UtilityClass
@Slf4j
public class ConfigManager {
    private static OnlyofficeProperties properties;

    static {
        Init();
    }

    private static void Init() {
        try {
            properties = (OnlyofficeProperties) ApplicationContextUtil.getBean("onlyofficeProperties");
        } catch (Exception ex) {
            properties = null;
        }
    }

    public static <T> T getProperty(String name) {
        if (properties == null) {
            properties = (OnlyofficeProperties) ApplicationContextUtil.getBean("onlyofficeProperties");
        }
        String[] names = name.indexOf(".") == -1 ? new String[]{name} : name.split("\\.");
        Object property = properties;
        for (int i = 0; i < names.length; i++) {
            Field field = ReflectionUtils.findField(property.getClass(), names[i]);
            ReflectionUtils.makeAccessible(field);
            property = ReflectionUtils.getField(field, property);
        }

        return (T) property;
    }
}