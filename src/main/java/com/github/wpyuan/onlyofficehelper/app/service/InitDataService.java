package com.github.wpyuan.onlyofficehelper.app.service;

import com.github.wpyuan.onlyofficehelper.api.dto.FileDto;
import com.github.wpyuan.onlyofficehelper.api.dto.IndexDto;
import com.github.wpyuan.onlyofficehelper.infra.helper.DocumentManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author PeiYuan
 */
@Service
@Slf4j
public class InitDataService {

    public IndexDto init(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, Object> map = new HashMap<>(1);
        DocumentManager.init(request, null);
        File[] files = DocumentManager.getStoredFiles(null);
        List<FileDto> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(FileDto.builder()
                    .name(file.getName())
                    .path(file.getAbsolutePath())
                    .size(file.length() + "B")
                    .build());
        }
        return IndexDto.builder().fileList(fileList).build();
    }
}
