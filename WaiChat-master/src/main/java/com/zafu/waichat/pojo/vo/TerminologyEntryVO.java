package com.zafu.waichat.pojo.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TerminologyEntryVO {
    private Integer id;
    private Integer ownerUserId;
    private String term;
    private String definition;
    private List<String> aliases = new ArrayList<>();
    private Integer sortWeight;
    private Integer enabled;
    /** 非空表示该条为从系统词库复制到「我的术语」的副本 */
    private Integer clonedFromSystemId;
    /** 所属术语组（系统/用户副本均可带组） */
    private Integer groupId;
    /** 组展示名，列表/分组接口填充 */
    private String groupName;
}
