package com.github.wpyuan.onlyofficehelper.app.service;

import com.github.wpyuan.onlyofficehelper.infra.helper.DocumentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * @author PeiYuan
 */
@Service
@Slf4j
public class UploadService {

    public void uploadLocal(String uid, String uname, MultipartFile httpPostedFile, HttpServletRequest request) throws Exception {
        String fileName = httpPostedFile.getOriginalFilename();
        long curSize = httpPostedFile.getSize();
        if (DocumentManager.getMaxFileSize() < curSize || curSize <= 0) {
            throw new RuntimeException("File size is incorrect");
        }
        assert fileName != null;
        String curExt = fileName.substring(fileName.lastIndexOf("."));
        if (!DocumentManager.getFileExts().contains(curExt)) {
            throw new RuntimeException("File type is not supported");
        }
        InputStream fileStream = httpPostedFile.getInputStream();
        DocumentManager.init(request, null);
        fileName = DocumentManager.getCorrectName(fileName);
        String fileStoragePath = DocumentManager.storagePath(fileName, null);
        File file = new File(fileStoragePath);
        try (FileOutputStream out = new FileOutputStream(file)) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = fileStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.flush();
        }

        DocumentManager.createMeta(fileName, uid, uname);

    }
}
