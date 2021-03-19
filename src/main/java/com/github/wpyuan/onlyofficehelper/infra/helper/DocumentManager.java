package com.github.wpyuan.onlyofficehelper.infra.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.primeframework.jwt.Signer;
import org.primeframework.jwt.Verifier;
import org.primeframework.jwt.domain.JWT;
import org.primeframework.jwt.hmac.HMACSigner;
import org.primeframework.jwt.hmac.HMACVerifier;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Administrator
 */
@UtilityClass
@Slf4j
public class DocumentManager {
    private static HttpServletRequest request;

    public static void init(HttpServletRequest req, HttpServletResponse resp) {
        request = req;
    }

    public static long getMaxFileSize() {

        long size;

        try {
            size = ConfigManager.getProperty("filesizeMax");
        } catch (Exception ex) {
            size = 0;
        }

        return size > 0 ? size : 5 * 1024 * 1024;
    }

    public static List<String> getFileExts() {
        List<String> res = new ArrayList<>();

        res.addAll(getViewedExts());
        res.addAll(getEditedExts());
        res.addAll(getConvertExts());

        return res;
    }

    public static List<String> getViewedExts() {
        String exts = ConfigManager.getProperty("files.docservice.viewedDocs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> getEditedExts() {
        String exts = ConfigManager.getProperty("files.docservice.editedDocs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static List<String> getConvertExts() {
        String exts = ConfigManager.getProperty("files.docservice.convertDocs");
        return Arrays.asList(exts.split("\\|"));
    }

    public static String getCorrectName(String fileName) {
        String baseName = FileUtility.getFileNameWithoutExtension(fileName);
        String ext = FileUtility.getFileExtension(fileName);
        String name = baseName + ext;

        File file = new File(storagePath(name, null));

        for (int i = 1; file.exists(); i++) {
            name = baseName + " (" + i + ")" + ext;
            file = new File(storagePath(name, null));
        }

        return name;
    }

    public static String storagePath(String fileName, String userAddress) {
        String directory = filesRootPath(userAddress);
        return directory + fileName;
    }

    public static String filesRootPath(String userAddress) {
        String hostAddress = curUserHostAddress(userAddress);
        String serverPath = request.getSession().getServletContext().getRealPath("");
        String storagePath = ConfigManager.getProperty("storageFolder");
        String directory = serverPath + storagePath + File.separator + hostAddress + File.separator;

        File file = new File(directory);

        if (!file.exists()) {
            file.mkdirs();
        }

        return directory;
    }

    public static String curUserHostAddress(String userAddress) {
        if (userAddress == null) {
            try {
                userAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception ex) {
                userAddress = "";
            }
        }

        return userAddress.replaceAll("[^0-9a-zA-Z.=]", "_");
    }

    public static void createMeta(String fileName, String uid, String uname) throws Exception {
        String histDir = historyDir(storagePath(fileName, null));

        File dir = new File(histDir);
        dir.mkdir();

        JSONObject json = new JSONObject();
        json.put("created", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        json.put("id", (uid == null || uid.isEmpty()) ? "uid-1" : uid);
        json.put("name", (uname == null || uname.isEmpty()) ? "John Smith" : uname);

        File meta = new File(histDir + File.separator + "createdInfo.json");
        try (FileWriter writer = new FileWriter(meta)) {
            json.writeJSONString(writer);
        }
    }

    public static String historyDir(String storagePath) {
        return storagePath += "-hist";
    }

    public static File[] getStoredFiles(String userAddress) {
        String directory = filesRootPath(userAddress);

        File file = new File(directory);
        return file.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isFile();
            }
        });
    }

    public static String getServerUrl() {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public static String getFileUri(String fileName) {
        try {
            String serverPath = getServerUrl();
            String storagePath = ConfigManager.getProperty("storageFolder");
            String hostAddress = curUserHostAddress(null);

            String filePath = serverPath + "/" + storagePath + "/" + hostAddress + "/" + URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8.toString()).replace("+", "%20");

            return filePath;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static Boolean tokenEnabled() {
        String secret = getTokenSecret();
        return secret != null && !secret.isEmpty();
    }

    private static String getTokenSecret() {
        return ConfigManager.getProperty("files.docservice.secret");
    }

    public static String createToken(Map<String, Object> payloadClaims) {
        try {
            Signer signer = HMACSigner.newSHA256Signer(getTokenSecret());
            JWT jwt = new JWT();
            for (String key : payloadClaims.keySet()) {
                jwt.addClaim(key, payloadClaims.get(key));
            }
            return JWT.getEncoder().encode(jwt, signer);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getCallback(String fileName) {
        String serverPath = getServerUrl();
        String hostAddress = curUserHostAddress(null);
        try {
            String query = "?fileName=" + URLEncoder.encode(fileName, java.nio.charset.StandardCharsets.UTF_8.toString()) + "&userAddress=" + URLEncoder.encode(hostAddress, java.nio.charset.StandardCharsets.UTF_8.toString());
            return serverPath + "/callback/track" + query;
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static Integer getFileVersion(String historyPath) {
        File dir = new File(historyPath);

        if (!dir.exists()) {
            return 0;
        }

        File[] dirs = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        return dirs.length;
    }

    public static String versionDir(String histPath, Integer version) {
        return histPath + File.separator + Integer.toString(version);
    }

    public static String versionDir(String fileName, String userAddress, Integer version) {
        return versionDir(historyDir(storagePath(fileName, userAddress)), version);
    }

    public static String getPathUri(String path) {
        String serverPath = getServerUrl();
        String storagePath = ConfigManager.getProperty("storageFolder");
        String hostAddress = curUserHostAddress(null);

        String filePath = serverPath + "/" + storagePath + "/" + hostAddress + "/" + path.replace(File.separator, "/").substring(filesRootPath(null).length()).replace(" ", "%20");

        return filePath;
    }

    public static JWT readToken(String token) {
        try {
            Verifier verifier = HMACVerifier.newVerifier(getTokenSecret());
            return JWT.getDecoder().decode(token, verifier);
        } catch (Exception exception) {
            return null;
        }
    }

}