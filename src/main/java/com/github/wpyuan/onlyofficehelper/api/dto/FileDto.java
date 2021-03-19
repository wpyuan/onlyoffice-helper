package com.github.wpyuan.onlyofficehelper.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author PeiYuan
 */
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class FileDto {
    private String name;
    private String path;
    private String size;
}
