package com.zafu.waichat.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Language {
    @TableId(type = IdType.AUTO)
    private String code;
    private String displayName;
    private String englishName;
    private String chineseName;
}
