package com.github.wpyuan.onlyofficehelper.infra.helper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * @author Administrator
 */
@Slf4j
public class FileModel {

    public String type = "desktop";
    public String mode = "edit";
    public String documentType;
    public Document document;
    public EditorConfig editorConfig;
    public String token;

    public FileModel(String fileName, String lang, String uid, String uname, String actionData) {
        if (fileName == null) {
            fileName = "";
        }
        fileName = fileName.trim();

        documentType = FileUtility.getFileType(fileName).toString().toLowerCase();

        document = new Document();
        document.title = fileName;
        document.url = DocumentManager.getFileUri(fileName);
        document.fileType = FileUtility.getFileExtension(fileName).replace(".", "");
        document.key = ServiceConverter.generateRevisionId(DocumentManager.curUserHostAddress(null) + "/" + fileName + "/" + Long.toString(new File(DocumentManager.storagePath(fileName, null)).lastModified()));

        editorConfig = new EditorConfig(actionData);
        editorConfig.callbackUrl = DocumentManager.getCallback(fileName);
        if (lang != null) {
            editorConfig.lang = lang;
        }

        if (uid != null) {
            editorConfig.user.id = uid;
        }
        if (uname != null) {
            editorConfig.user.name = uname;
        }

        editorConfig.customization.goback.url = DocumentManager.getServerUrl() + "/view/index.html";

        changeType(mode, type);
    }

    public void changeType(String mode, String type) {
        if (mode != null) {
            this.mode = mode;
        }
        if (type != null) {
            this.type = type;
        }

        Boolean canEdit = DocumentManager.getEditedExts().contains(FileUtility.getFileExtension(document.title));

        editorConfig.mode = canEdit && !"view".equals(this.mode) ? "edit" : "view";

        document.permissions = new Permissions(this.mode, this.type, canEdit);

        if ("embedded".equals(this.type)) {
            initDesktop();
        }
    }

    public void initDesktop() {
        editorConfig.initDesktop(document.url);
    }

    public void buildToken() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("documentType", documentType);
        map.put("document", document);
        map.put("editorConfig", editorConfig);

        token = DocumentManager.createToken(map);
    }

    public String[] getHistory() {
        JSONParser parser = new JSONParser();
        String histDir = DocumentManager.historyDir(DocumentManager.storagePath(document.title, null));
        if (DocumentManager.getFileVersion(histDir) > 0) {
            Integer curVer = DocumentManager.getFileVersion(histDir);

            Set<Object> hist = new HashSet<Object>();
            Map<String, Object> histData = new HashMap<String, Object>();

            for (Integer i = 0; i <= curVer; i++) {
                Map<String, Object> obj = new HashMap<String, Object>();
                Map<String, Object> dataObj = new HashMap<String, Object>();
                String verDir = DocumentManager.versionDir(histDir, i + 1);

                try {
                    String key = null;

                    key = i.equals(curVer) ? document.key : readFileToEnd(new File(verDir + File.separator + "key.txt"));

                    obj.put("key", key);
                    obj.put("version", i);

                    if (i == 0) {
                        String createdInfo = readFileToEnd(new File(histDir + File.separator + "createdInfo.json"));
                        JSONObject json = (JSONObject) parser.parse(createdInfo);

                        obj.put("created", json.get("created"));
                        Map<String, Object> user = new HashMap<String, Object>();
                        user.put("id", json.get("id"));
                        user.put("name", json.get("name"));
                        obj.put("user", user);
                    }

                    dataObj.put("key", key);
                    dataObj.put("url", i.equals(curVer) ? document.url : DocumentManager.getPathUri(verDir + File.separator + "prev" + FileUtility.getFileExtension(document.title)));
                    dataObj.put("version", i);

                    if (i > 0) {
                        JSONObject changes = (JSONObject) parser.parse(readFileToEnd(new File(DocumentManager.versionDir(histDir, i) + File.separator + "changes.json")));
                        JSONObject change = (JSONObject) ((JSONArray) changes.get("changes")).get(0);

                        obj.put("changes", changes.get("changes"));
                        obj.put("serverVersion", changes.get("serverVersion"));
                        obj.put("created", change.get("created"));
                        obj.put("user", change.get("user"));

                        Map<String, Object> prev = (Map<String, Object>) histData.get(Integer.toString(i - 1));
                        Map<String, Object> prevInfo = new HashMap<String, Object>();
                        prevInfo.put("key", prev.get("key"));
                        prevInfo.put("url", prev.get("url"));
                        dataObj.put("previous", prevInfo);
                        dataObj.put("changesUrl", DocumentManager.getPathUri(DocumentManager.versionDir(histDir, i) + File.separator + "diff.zip"));
                    }

                    hist.add(obj);
                    histData.put(Integer.toString(i), dataObj);

                } catch (Exception ex) {
                }
            }

            Map<String, Object> histObj = new HashMap<String, Object>();
            histObj.put("currentVersion", curVer);
            histObj.put("history", hist);

            Gson gson = new Gson();
            return new String[]{gson.toJson(histObj), gson.toJson(histData)};
        }
        return new String[]{"", ""};
    }

    private String readFileToEnd(File file) {
        String output = "";
        try {
            try (FileInputStream is = new FileInputStream(file)) {
                Scanner scanner = new Scanner(is);
                scanner.useDelimiter("\\A");
                while (scanner.hasNext()) {
                    output += scanner.next();
                }
                scanner.close();
            }
        } catch (Exception e) {
        }
        return output;
    }

    public class Document {
        public String title;
        public String url;
        public String fileType;
        public String key;
        public Permissions permissions;
    }

    public class Permissions {
        public Boolean comment;
        public Boolean download;
        public Boolean edit;
        public Boolean fillForms;
        public Boolean modifyFilter;
        public Boolean modifyContentControl;
        public Boolean review;

        public Permissions(String mode, String type, Boolean canEdit) {
            comment = !"view".equals(mode) && !"fillForms".equals(mode) && !"embedded".equals(mode) && !"blockcontent".equals(mode);
            download = true;
            edit = canEdit && ("edit".equals(mode) || "filter".equals(mode) || "blockcontent".equals(mode));
            fillForms = !"view".equals(mode) && !"comment".equals(mode) && !"embedded".equals(mode) && !"blockcontent".equals(mode);
            modifyFilter = !"filter".equals(mode);
            modifyContentControl = !"blockcontent".equals(mode);
            review = "edit".equals(mode) || "review".equals(mode);
        }
    }

    public class EditorConfig {
        public HashMap<String, Object> actionLink = null;
        public String mode = "edit";
        public String callbackUrl;
        public String lang = "en";
        public User user;
        public Customization customization;
        public Embedded embedded;

        public EditorConfig(String actionData) {
            if (actionData != null) {
                Gson gson = new Gson();
                actionLink = gson.fromJson(actionData, new TypeToken<HashMap<String, Object>>() {
                }.getType());
            }
            user = new User();
            customization = new Customization();
        }

        public void initDesktop(String url) {
            embedded = new Embedded();
            embedded.saveUrl = url;
            embedded.embedUrl = url;
            embedded.shareUrl = url;
            embedded.toolbarDocked = "top";
        }

        public class User {
            public String id = "uid-1";
            public String name = "John Smith";
        }

        public class Customization {
            public Goback goback;

            public Customization() {
                goback = new Goback();
            }

            public class Goback {
                public String url;
            }
        }

        public class Embedded {
            public String saveUrl;
            public String embedUrl;
            public String shareUrl;
            public String toolbarDocked;
        }
    }


    public static String serialize(FileModel model) {
        Gson gson = new Gson();
        return gson.toJson(model);
    }
}
