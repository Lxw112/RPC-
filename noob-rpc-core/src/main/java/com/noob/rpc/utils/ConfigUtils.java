package com.noob.rpc.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;
import java.io.InputStream;

/**
 * 配置工具类
 */
public class ConfigUtils {

    /**
     * 加载配置对象
     * @param tClass
     * @param prefix
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        return loadConfig(tClass, prefix, "");
        //return loadConfigYaml(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     * @param tClass
     * @param prefix
     * @param environment
     * @param <T>
     * @return
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = null;
        try {
            props = new Props(configFileBuilder.toString());
        } catch (Exception e) {
           e.printStackTrace();
        }
        return props.toBean(tClass, prefix);
    }
    public static <T> T loadConfigYaml(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".yaml");  // 将 .properties 改为 .yml

        Yaml yaml = new Yaml();
        T config = null;

        try (InputStream inputStream = new ClassPathResource(configFileBuilder.toString()).getStream()) {
            // 加载 YAML 文件并将其转化为 Map
            // 使用 loadAs 来指定返回类型
            Map<String, Object> yamlMap = yaml.loadAs(inputStream, Map.class);

            // 如果有 prefix，提取指定前缀的配置
            if (StrUtil.isNotBlank(prefix) && yamlMap.containsKey(prefix)) {
                Map<String, Object> prefixConfig = (Map<String, Object>) yamlMap.get(prefix);
                // 将 Map 转换为对应的 Java Bean
                config = BeanUtil.mapToBean(prefixConfig, tClass, false);
            } else {
                // 如果没有前缀，直接将整个 Map 转换为 Java Bean
                config = BeanUtil.mapToBean(yamlMap, tClass, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }
}
