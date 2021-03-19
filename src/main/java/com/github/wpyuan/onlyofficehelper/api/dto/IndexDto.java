package com.github.wpyuan.onlyofficehelper.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author PeiYuan
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class IndexDto {
    private String uid;
    private String uname;
    private String language;
    private List<FileDto> fileList;
}
