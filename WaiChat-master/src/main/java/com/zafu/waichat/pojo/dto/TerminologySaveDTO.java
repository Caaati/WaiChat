package com.zafu.waichat.pojo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologySaveDTO {
    private String term;
    private String definition;
    private List<TerminologyAliasItemDTO> aliases = new ArrayList<>();
    private Integer sortWeight;
    /** 默认 1 */
    private Integer enabled;
}
