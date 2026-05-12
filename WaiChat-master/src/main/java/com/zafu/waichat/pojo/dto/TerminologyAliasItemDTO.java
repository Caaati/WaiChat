package com.zafu.waichat.pojo.dto;

import lombok.Data;

/**
 * 保存「我的术语」时单条别名：文本 + 可选目标语言（空表示任意语言均可用该条作为译入术语对的候选）。
 */
@Data
public class TerminologyAliasItemDTO {
    private String alias;
    /** 与语言表 code 一致；空或 null 表示不限制目标语言 */
    private String targetLang;
}
