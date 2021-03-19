package com.github.wpyuan.onlyofficehelper.api.controller;

import com.github.wpyuan.onlyofficehelper.app.service.TrackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author PeiYuan
 */
@RestController
@Slf4j
@RequestMapping("/callback")
public class CallbackController {

    @Autowired
    private TrackService trackService;

    @PostMapping("/track")
    public ResponseEntity<String> track(String fileName, String userAddress, HttpServletRequest request) {
        return ResponseEntity.ok(trackService.track(fileName, userAddress, request));
    }
}
