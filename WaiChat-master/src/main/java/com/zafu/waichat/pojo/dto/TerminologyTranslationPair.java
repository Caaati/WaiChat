package com.zafu.waichat.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 机器翻译 API（translation_options.terms）用的一条术语：原文片段与期望译法。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TerminologyTranslationPair {
    private String source;
    private String target;
}
