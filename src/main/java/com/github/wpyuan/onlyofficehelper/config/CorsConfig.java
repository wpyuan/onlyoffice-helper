package com.github.wpyuan.onlyofficehelper.config;

import com.github.wpyuan.onlyofficehelper.infra.helper.ConfigManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author PeiYuan
 */
@Configuration
@DependsOn(value = {"applicationContextUtil"})
public class CorsConfig {

    private CorsConfiguration addOnlyofficeServiceCorsConfig() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin(ConfigManager.getProperty("schemaHost"));
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", addOnlyofficeServiceCorsConfig());
        return new CorsFilter(source);
    }
}
