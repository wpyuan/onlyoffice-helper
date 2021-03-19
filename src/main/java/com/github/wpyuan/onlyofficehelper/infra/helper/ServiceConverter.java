package com.github.wpyuan.onlyofficehelper.infra.helper;

import com.google.gson.Gson;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@UtilityClass
@Slf4j
public class ServiceConverter {

    private static int convertTimeout = 120000;
    private static final String documentConverterUrl = ConfigManager.getProperty("files.docservice.url.converter");
    private static final String documentJwtHeader = ConfigManager.getProperty("files.docservice.header");

    public static class ConvertBody {
        public String url;
        public String outputtype;
        public String filetype;
        public String title;
        public String key;
        public Boolean async;
        public String token;
    }

    static {
        try {
            int timeout = Integer.parseInt(ConfigManager.getProperty("files.docservice.timeout"));
            if (timeout > 0) {
                convertTimeout = timeout;
            }
        } catch (Exception ex) {
        }
    }

    public static String getConvertedUri(String documentUri, String fromExtension, String toExtension, String documentRevisionId, Boolean isAsync) throws Exception {
        fromExtension = fromExtension == null || fromExtension.isEmpty() ? FileUtility.getFileExtension(documentUri) : fromExtension;

        String title = FileUtility.getFileName(documentUri);
        title = title == null || title.isEmpty() ? UUID.randomUUID().toString() : title;

        documentRevisionId = documentRevisionId == null || documentRevisionId.isEmpty() ? documentUri : documentRevisionId;

        documentRevisionId = generateRevisionId(documentRevisionId);

        ConvertBody body = new ConvertBody();
        body.url = documentUri;
        body.outputtype = toExtension.replace(".", "");
        body.filetype = fromExtension.replace(".", "");
        body.title = title;
        body.key = documentRevisionId;
        if (isAsync) {
            body.async = true;
        }

        String headerToken = "";
        if (DocumentManager.tokenEnabled()) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("url", body.url);
            map.put("outputtype", body.outputtype);
            map.put("filetype", body.filetype);
            map.put("title", body.title);
            map.put("key", body.key);
            if (isAsync) {
                map.put("async", body.async);
            }

            String token = DocumentManager.createToken(map);
            body.token = token;

            Map<String, Object> payloadMap = new HashMap<String, Object>();
            payloadMap.put("payload", map);
            headerToken = DocumentManager.createToken(payloadMap);
        }

        Gson gson = new Gson();
        String bodyString = gson.toJson(body);

        byte[] bodyByte = bodyString.getBytes(StandardCharsets.UTF_8);

        URL url = new URL(documentConverterUrl);
        java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setFixedLengthStreamingMode(bodyByte.length);
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(convertTimeout);

        if (DocumentManager.tokenEnabled()) {
            connection.setRequestProperty(documentJwtHeader == "" ? "Authorization" : documentJwtHeader, "Bearer " + headerToken);
        }

        connection.connect();
        try (OutputStream os = connection.getOutputStream()) {
            os.write(bodyByte);
        }

        InputStream stream = connection.getInputStream();

        if (stream == null) {
            throw new Exception("Could not get an answer");
        }

        String jsonString = convertStreamToString(stream);

        connection.disconnect();

        return getResponseUri(jsonString);
    }

    public static String generateRevisionId(String expectedKey) {
        if (expectedKey.length() > 20) {
            expectedKey = Integer.toString(expectedKey.hashCode());
        }

        String key = expectedKey.replace("[^0-9-.a-zA-Z_=]", "_");

        return key.substring(0, Math.min(key.length(), 20));
    }

    private static void processConvertServiceResponceError(int errorCode) throws Exception {
        String errorMessage = "";
        String errorMessageTemplate = "Error occurred in the ConvertService: ";

        switch (errorCode) {
            case -8:
                errorMessage = errorMessageTemplate + "Error document VKey";
                break;
            case -7:
                errorMessage = errorMessageTemplate + "Error document request";
                break;
            case -6:
                errorMessage = errorMessageTemplate + "Error database";
                break;
            case -5:
                errorMessage = errorMessageTemplate + "Error unexpected guid";
                break;
            case -4:
                errorMessage = errorMessageTemplate + "Error download error";
                break;
            case -3:
                errorMessage = errorMessageTemplate + "Error convertation error";
                break;
            case -2:
                errorMessage = errorMessageTemplate + "Error convertation timeout";
                break;
            case -1:
                errorMessage = errorMessageTemplate + "Error convertation unknown";
                break;
            case 0:
                break;
            default:
                errorMessage = "ErrorCode = " + errorCode;
                break;
        }

        throw new Exception(errorMessage);
    }

    private static String getResponseUri(String jsonString) throws Exception {
        JSONObject jsonObj = convertStringToJSON(jsonString);

        Object error = jsonObj.get("error");
        if (error != null) {
            processConvertServiceResponceError(Math.toIntExact((long) error));
        }

        Boolean isEndConvert = (Boolean) jsonObj.get("endConvert");

        Long resultPercent = 0L;
        String responseUri = null;

        if (isEndConvert) {
            resultPercent = 100L;
            responseUri = (String) jsonObj.get("fileUrl");
        } else {
            resultPercent = (Long) jsonObj.get("percent");
            resultPercent = resultPercent >= 100L ? 99L : resultPercent;
        }

        return resultPercent >= 100L ? responseUri : "";
    }

    private static String convertStreamToString(InputStream stream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = bufferedReader.readLine();

        while (line != null) {
            stringBuilder.append(line);
            line = bufferedReader.readLine();
        }

        String result = stringBuilder.toString();

        return result;
    }

    private static JSONObject convertStringToJSON(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        Object obj = parser.parse(jsonString);
        JSONObject jsonObj = (JSONObject) obj;

        return jsonObj;
    }
}