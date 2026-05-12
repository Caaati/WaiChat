package com.zafu.waichat.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("terminology_alias")
public class TerminologyAlias {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer terminologyId;
    private String alias;
    /** 翻译目标语言，与 /ai/translate 的 target 一致；null 表示任意 */
    @TableField("target_lang")
    private String targetLang;
}
