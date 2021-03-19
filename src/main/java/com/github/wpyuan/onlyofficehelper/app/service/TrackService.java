package com.github.wpyuan.onlyofficehelper.app.service;

import com.github.wpyuan.onlyofficehelper.infra.helper.ConfigManager;
import com.github.wpyuan.onlyofficehelper.infra.helper.DocumentManager;
import com.github.wpyuan.onlyofficehelper.infra.helper.FileUtility;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.primeframework.jwt.domain.JWT;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Scanner;

/**
 * @author PeiYuan
 */
@Service
@Slf4j
public class TrackService {

    public String track(String fileName, String userAddress, HttpServletRequest request) {
        final String documentJwtHeader = ConfigManager.getProperty("files.docservice.header");
        DocumentManager.init(request, null);

        String storagePath = DocumentManager.storagePath(fileName, userAddress);
        String body = "";

        try {
            Scanner scanner = new Scanner(request.getInputStream());
            scanner.useDelimiter("\\A");
            body = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        } catch (Exception ex) {
            return "get request.getInputStream error:" + ex.getMessage();
        }

        if (body.isEmpty()) {
            return "empty request.getInputStream";
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObj;

        try {
            Object obj = parser.parse(body);
            jsonObj = (JSONObject) obj;
        } catch (Exception ex) {
            return "JSONParser.parse error:" + ex.getMessage();
        }

        int status;
        String downloadUri;
        String changesUri;
        String key;

        if (DocumentManager.tokenEnabled()) {
            String token = (String) jsonObj.get("token");

            if (token == null) {
                String header = (String) request.getHeader(documentJwtHeader == null || documentJwtHeader.isEmpty() ? "Authorization" : documentJwtHeader);
                if (header != null && !header.isEmpty()) {
                    token = header.startsWith("Bearer ") ? header.substring(7) : header;
                }
            }

            if (token == null || token.isEmpty()) {
                return "{\"error\":1,\"message\":\"JWT expected\"}";
            }

            JWT jwt = DocumentManager.readToken(token);
            if (jwt == null) {
                return "{\"error\":1,\"message\":\"JWT validation failed\"}";
            }

            if (jwt.getObject("payload") != null) {
                try {
                    @SuppressWarnings("unchecked") LinkedHashMap<String, Object> payload =
                            (LinkedHashMap<String, Object>) jwt.getObject("payload");

                    jwt.claims = payload;
                } catch (Exception ex) {
                    return "{\"error\":1,\"message\":\"Wrong payload\"}";
                }
            }

            status = jwt.getInteger("status");
            downloadUri = jwt.getString("url");
            changesUri = jwt.getString("changesurl");
            key = jwt.getString("key");
        } else {
            status = Math.toIntExact((long) jsonObj.get("status"));
            downloadUri = (String) jsonObj.get("url");
            changesUri = (String) jsonObj.get("changesurl");
            key = (String) jsonObj.get("key");
        }

        int saved = 0;
        //MustSave, Corrupted
        if (status == 2 || status == 3) {
            try {
                String histDir = DocumentManager.historyDir(storagePath);
                String versionDir = DocumentManager.versionDir(histDir, DocumentManager.getFileVersion(histDir) + 1);
                File ver = new File(versionDir);
                File toSave = new File(storagePath);

                if (!ver.exists()) {
                    ver.mkdirs();
                }

                toSave.renameTo(new File(versionDir + File.separator + "prev" + FileUtility.getFileExtension(fileName)));

                downloadToFile(downloadUri, toSave);
                downloadToFile(changesUri, new File(versionDir + File.separator + "diff.zip"));

                String history = (String) jsonObj.get("changeshistory");
                if (history == null && jsonObj.containsKey("history")) {
                    history = ((JSONObject) jsonObj.get("history")).toJSONString();
                }
                if (history != null && !history.isEmpty()) {
                    FileWriter fw = new FileWriter(new File(versionDir + File.separator + "changes.json"));
                    fw.write(history);
                    fw.close();
                }

                FileWriter fw = new FileWriter(new File(versionDir + File.separator + "key.txt"));
                fw.write(key);
                fw.close();
            } catch (Exception ex) {
                saved = 1;
            }
        }

        return "{\"error\":" + saved + "}";
    }

    private static void downloadToFile(String url, File file) throws Exception {
        if (url == null || url.isEmpty()) {
            throw new Exception("argument url");
        }
        if (file == null) {
            throw new Exception("argument path");
        }

        URL uri = new URL(url);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) uri.openConnection();
        InputStream stream = connection.getInputStream();

        if (stream == null) {
            throw new Exception("Stream is null");
        }

        try (FileOutputStream out = new FileOutputStream(file)) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = stream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();
        }

        connection.disconnect();
    }
}
