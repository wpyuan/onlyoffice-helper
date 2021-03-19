package com.github.wpyuan.onlyofficehelper.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author PeiYuan
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "onlyoffice")
public class OnlyofficeProperties {
    private String schemaHost;
    private String apiJsUrl;
    private String storageFolder;
    private Long filesizeMax;
    private FilesProperties files = new FilesProperties();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public class FilesProperties {
        private DocserviceProperties docservice = new DocserviceProperties();

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public class DocserviceProperties {
            private String header;
            private String viewedDocs;
            private String editedDocs;
            private String convertDocs;
            private Integer timeout;
            private String secret;
            private UrlProperties url = new UrlProperties();

            @NoArgsConstructor
            @AllArgsConstructor
            @Data
            public class UrlProperties {
                private String tempstorage;
                private String converter;
            }
        }
    }
}
