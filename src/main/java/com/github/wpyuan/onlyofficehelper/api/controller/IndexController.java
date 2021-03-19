package com.github.wpyuan.onlyofficehelper.api.controller;

import com.github.wpyuan.onlyofficehelper.api.dto.IndexDto;
import com.github.wpyuan.onlyofficehelper.app.service.InitDataService;
import com.github.wpyuan.onlyofficehelper.app.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * @author PeiYuan
 */
@RestController
@Slf4j
public class IndexController {

    @Autowired
    private InitDataService initDataService;
    @Autowired
    private UploadService uploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(String uid, String uname, @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        uploadService.uploadLocal(uid, uname, file, request);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/initData")
    public ResponseEntity<IndexDto> initData(HttpServletRequest request) throws UnsupportedEncodingException {

        return ResponseEntity.ok(initDataService.init(request));
    }
}
