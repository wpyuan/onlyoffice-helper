package com.github.wpyuan.onlyofficehelper.api.controller;

import com.github.wpyuan.onlyofficehelper.api.dto.EditorDto;
import com.github.wpyuan.onlyofficehelper.infra.helper.ConfigManager;
import com.github.wpyuan.onlyofficehelper.infra.helper.DocumentManager;
import com.github.wpyuan.onlyofficehelper.infra.helper.FileModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author PeiYuan
 */
@RestController
@Slf4j
@RequestMapping("/editor")
public class EditorController {

    @GetMapping("/loadData")
    public ResponseEntity<EditorDto> loadFileModel(String fileName, String uid, String uname, String actionLink, String mode, String type, String lang, HttpServletRequest request) {
        DocumentManager.init(request, null);
        FileModel file = new FileModel(fileName, lang, uid, uname, actionLink);
        file.changeType(mode, type);
        if (DocumentManager.tokenEnabled()) {
            file.buildToken();
        }
        String[] his = file.getHistory();
        EditorDto editorDto = EditorDto.builder()
                .apiJsUrl(ConfigManager.getProperty("apiJsUrl"))
                .model(FileModel.serialize(file))
                // 历史数据
                .history(his[0])
                .historyData(his[1])
                .build();
        return ResponseEntity.ok(editorDto);
    }
}
