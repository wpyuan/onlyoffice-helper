package com.github.wpyuan.onlyofficehelper.infra.helper;


import com.github.wpyuan.onlyofficehelper.infra.enums.FileType;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@UtilityClass
@Slf4j
public class FileUtility {

    public static FileType getFileType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();

        if (extsDocument.contains(ext)) {
            return FileType.word;
        }

        if (extsSpreadsheet.contains(ext)) {
            return FileType.cell;
        }

        if (extsPresentation.contains(ext)) {
            return FileType.slide;
        }

        return FileType.word;
    }

    public static List<String> extsDocument = Arrays.asList
            (
                    ".doc", ".docx", ".docm",
                    ".dot", ".dotx", ".dotm",
                    ".odt", ".fodt", ".ott", ".rtf", ".txt",
                    ".html", ".htm", ".mht",
                    ".pdf", ".djvu", ".fb2", ".epub", ".xps"
            );

    public static List<String> extsSpreadsheet = Arrays.asList
            (
                    ".xls", ".xlsx", ".xlsm",
                    ".xlt", ".xltx", ".xltm",
                    ".ods", ".fods", ".ots", ".csv"
            );

    public static List<String> extsPresentation = Arrays.asList
            (
                    ".pps", ".ppsx", ".ppsm",
                    ".ppt", ".pptx", ".pptm",
                    ".pot", ".potx", ".potm",
                    ".odp", ".fodp", ".otp"
            );


    public static String getFileName(String url) {
        if (url == null) {
            return null;
        }

        //for external file url
        String tempstorage = ConfigManager.getProperty("files.docservice.url.tempstorage");
        if (!tempstorage.isEmpty() && url.startsWith(tempstorage)) {
            Map<String, String> params = getUrlParams(url);
            return params == null ? null : params.get("filename");
        }

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());
        return fileName;
    }

    public static String getFileNameWithoutExtension(String url) {
        String fileName = getFileName(url);
        if (fileName == null) {
            return null;
        }
        String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
        return fileNameWithoutExt;
    }

    public static String getFileExtension(String url) {
        String fileName = getFileName(url);
        if (fileName == null) {
            return null;
        }
        String fileExt = fileName.substring(fileName.lastIndexOf("."));
        return fileExt.toLowerCase();
    }

    public static Map<String, String> getUrlParams(String url) {
        try {
            String query = new URL(url).getQuery();
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<>(params.length);
            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
            return map;
        } catch (Exception ex) {
            return null;
        }
    }

}
