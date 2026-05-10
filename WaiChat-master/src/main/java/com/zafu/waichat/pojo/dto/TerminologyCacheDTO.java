package com.zafu.waichat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 快照与内存匹配用的词条结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyCacheDTO {
    private Integer id;
    /** null 表示系统词条 */
    private Integer ownerUserId;
    private String term;
    private String definition;
    private Integer sortWeight;
    private List<String> phrases = new ArrayList<>();
}
