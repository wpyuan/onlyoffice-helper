package com.github.wpyuan.onlyofficehelper.api.controller;

import com.github.wpyuan.onlyofficehelper.api.dto.IndexDto;
import com.github.wpyuan.onlyofficehelper.app.service.InitDataService;
import com.github.wpyuan.onlyofficehelper.app.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
    public ResponseEntity<Boolean> upload(@RequestParam("file") MultipartFile file, @RequestParam("uid") String uid, @RequestParam("uname") String uname, HttpServletRequest request) throws Exception {
        uploadService.uploadLocal(uid, uname, file, request);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/initData")
    public ResponseEntity<IndexDto> initData(HttpServletRequest request) throws UnsupportedEncodingException {

        return ResponseEntity.ok(initDataService.init(request));
    }
}
